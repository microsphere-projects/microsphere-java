package io.github.microsphere.classloading;

import io.github.microsphere.collection.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import static io.github.microsphere.collection.MapUtils.isEmpty;

/**
 * The customized ClassLoader under Windows operating system to solve the case-insensitive
 * problem of the specified class (list) in the Class Path
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
class WindowsRedefinedClassLoader extends URLClassLoader {

    private static final String WINDOWS_REDEFINED_CLASSES_RESOURCE_NAME = "META-INF/windows-redefined-classes";

    private static final Logger logger = LoggerFactory.getLogger(WindowsRedefinedClassLoader.class);

    private static final Charset charset = Charset.forName("UTF-8");

    /**
     * Class name as key and class resource directory URL as value
     */
    private static final SortedMap<String, RedefinedClassMetadata> redefinedClassMetadata = new TreeMap<>();

    public WindowsRedefinedClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
        loadRedefinedClassMetadata(parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> klass = super.findLoadedClass(name);

        if (klass == null) {
            RedefinedClassMetadata metadata = getRedefinedClassMetadata(name);
            if (metadata != null) {
                klass = this.loadRedefinedClass(metadata, resolve);
            } else {
                klass = super.loadClass(name, resolve);
            }
        }

        return klass;
    }

    private Class<?> loadRedefinedClass(RedefinedClassMetadata metadata, boolean resolve) throws ClassNotFoundException {
        Class<?> result = null;
        String className = metadata.className;
        for (File packageDirectory : metadata.packageDirectories) {
            String classFileName = metadata.simpleClassName + ".class";
            // Because the Windows file system is not sensitive to the file name, the class file is obtained by directory file filtering
            File[] files = packageDirectory.listFiles(file -> classFileName.equals(file.getName()));
            if (files.length == 1) {
                File classFile = files[0];
                logger.debug("Class[name: {}] file [name: {}] found in Package directory [path: {}], about to execute ClassLoader.defineClass",
                        className, classFileName, packageDirectory.getAbsolutePath());
                try (FileInputStream inputStream = new FileInputStream(classFile)) {
                    byte[] byteCodes = IOUtils.toByteArray(inputStream);
                    result = super.defineClass(className, byteCodes, 0, byteCodes.length);
                } catch (IOException e) {
                    logger.error("Class[name: {}] file [path: {}] cannot be read!", className, classFile.getAbsolutePath());
                }
                break;
            }
        }

        if (result == null) {
            // ClassNotFoundException will be thrown by parent ClassLoader if the class cannot be found
            result = super.loadClass(className, resolve);
        }

        return result;
    }


    private void loadRedefinedClassMetadata(ClassLoader classLoader) {
        if (!isEmpty(redefinedClassMetadata)) {
            return;
        }
        SortedSet<String> redefinedClassNames = loadRedefinedClassNames(classLoader);
        for (String redefinedClassName : redefinedClassNames) {
            RedefinedClassMetadata metadata = resolveRedefinedClassMetadata(redefinedClassName, classLoader);
            if (metadata != null) {
                redefinedClassMetadata.putIfAbsent(redefinedClassName, metadata);
            }
        }
    }

    private RedefinedClassMetadata resolveRedefinedClassMetadata(String className, ClassLoader classLoader) {
        int lastDotIndex = className.lastIndexOf('.');
        String packageName = lastDotIndex > 0 ? className.substring(0, lastDotIndex) : "";
        String packageRelativePath = packageName.replace('.', '/');
        List<File> packageDirectories = new LinkedList<>();

        try {
            Enumeration<URL> packageResources = classLoader.getResources(packageRelativePath);
            while (packageResources.hasMoreElements()) {
                URL packageResource = packageResources.nextElement();
                if (!"file".equalsIgnoreCase(packageResource.getProtocol())) {
                    logger.debug("Class [name: {}] is located in a non-file system directory [path: {}], RedefinedClassMetadata does not need to be processed!",
                            className, packageResource);
                    continue;
                }
                File packageDirectory = new File(packageResource.getPath());
                packageDirectories.add(packageDirectory);
            }
        } catch (IOException e) {
            logger.error("The package resource [path: {}] for class [Name: {}] cannot be read by the classloader",
                    className, packageRelativePath);
        }

        if (packageDirectories.isEmpty()) {
            return null;
        }

        String simpleClassName = lastDotIndex > 0 ? className.substring(lastDotIndex + 1) : className;

        RedefinedClassMetadata metadata = new RedefinedClassMetadata();
        metadata.className = className;
        metadata.packageName = packageName;
        metadata.simpleClassName = simpleClassName;
        metadata.packageDirectories = packageDirectories;
        return metadata;
    }

    private static SortedSet<String> loadRedefinedClassNames(ClassLoader classLoader) {
        String resourceName = WINDOWS_REDEFINED_CLASSES_RESOURCE_NAME;
        SortedSet<String> redefinedClassNames = new TreeSet<>();
        try {
            Enumeration<URL> resources = classLoader.getResources(WINDOWS_REDEFINED_CLASSES_RESOURCE_NAME);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                try (InputStream inputStream = resource.openStream()) {
                    String configContent = IOUtils.toString(inputStream, charset);
                    String[] classNames = StringUtils.split(configContent, System.lineSeparator());
                    redefinedClassNames.addAll(Arrays.asList(classNames));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Windows redefinition class manifest file] [- S] read failed!", resourceName), e);
        }
        return redefinedClassNames;
    }

    private static RedefinedClassMetadata getRedefinedClassMetadata(String className) {
        return redefinedClassMetadata.get(className);
    }

    private static class RedefinedClassMetadata {

        private String className;

        private String packageName;

        private String simpleClassName;

        private List<File> packageDirectories; // Multiple Classpaths of the companion package name may exist

    }
}
