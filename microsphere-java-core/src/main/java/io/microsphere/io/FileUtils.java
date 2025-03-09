/**
 *
 */
package io.microsphere.io;

import io.microsphere.util.BaseUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static io.microsphere.constants.FileConstants.FILE_EXTENSION_CHAR;
import static io.microsphere.constants.SeparatorConstants.FILE_SEPARATOR;
import static io.microsphere.net.URLUtils.normalizePath;
import static io.microsphere.util.StringUtils.isBlank;
import static io.microsphere.util.StringUtils.replace;
import static io.microsphere.util.SystemUtils.IS_OS_WINDOWS;

/**
 * {@link File} Utility
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see FileUtils
 * @since 1.0.0
 */
public abstract class FileUtils extends BaseUtils {

    /**
     * Resolve Relative Path
     *
     * @param parentDirectory Parent Directory
     * @param targetFile      Target File
     * @return If <code>targetFile</code> is a sub-file of <code>parentDirectory</code> , resolve relative path, or
     * <code>null</code>
     * @since 1.0.0
     */
    public static String resolveRelativePath(File parentDirectory, File targetFile) {
        String parentDirectoryPath = parentDirectory.getAbsolutePath();
        String targetFilePath = targetFile.getAbsolutePath();
        if (!targetFilePath.contains(parentDirectoryPath)) {
            return null;
        }
        return normalizePath(replace(targetFilePath, parentDirectoryPath, FILE_SEPARATOR));
    }

    /**
     * Get File Extension
     *
     * @param fileName the name of {@link File}
     * @return the file extension if found
     */
    public static String getFileExtension(String fileName) {
        if (isBlank(fileName)) {
            return null;
        }
        int index = fileName.lastIndexOf(FILE_EXTENSION_CHAR);
        return index > -1 ? fileName.substring(index + 1) : null;
    }

    /**
     * Deletes a directory recursively.
     *
     * @param directory directory to delete
     * @throws IOException in case deletion is unsuccessful
     */
    public static void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        if (!isSymlink(directory)) {
            cleanDirectory(directory);
        }

        if (!directory.delete()) {
            String message = "Unable to delete directory " + directory + ".";
            throw new IOException(message);
        }
    }


    /**
     * Cleans a directory without deleting it.
     *
     * @param directory directory to clean
     * @throws IOException in case cleaning is unsuccessful
     */
    public static void cleanDirectory(File directory) throws IOException {
        IOException exception = null;
        for (File file : listFiles(directory)) {
            try {
                forceDelete(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }

    /**
     * Deletes a file. If file is a directory, delete it and all sub-directories.
     * <p>
     * The difference between File.delete() and this method are:
     * <ul>
     * <li>A directory to be deleted does not have to be empty.</li>
     * <li>You get exceptions when a file or directory cannot be deleted.
     *      (java.io.File methods returns a boolean)</li>
     * </ul>
     *
     * @param file file or directory to delete, must not be {@code null}
     * @throws NullPointerException  if the directory is {@code null}
     * @throws FileNotFoundException if the file was not found
     * @throws IOException           in case deletion is unsuccessful
     */
    public static void forceDelete(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            boolean filePresent = file.exists();
            if (!file.delete()) {
                if (!filePresent) {
                    throw new FileNotFoundException("File does not exist: " + file);
                }
                String message = "Unable to delete file: " + file;
                throw new IOException(message);
            }
        }
    }

    /**
     * Schedules a file to be deleted when JVM exits.
     * If file is directory delete it and all sub-directories.
     *
     * @param file file or directory to delete, must not be {@code null}
     * @throws NullPointerException if the file is {@code null}
     * @throws IOException          in case deletion is unsuccessful
     */
    public static void forceDeleteOnExit(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectoryOnExit(file);
        } else {
            file.deleteOnExit();
        }
    }

    /**
     * Schedules a directory recursively for deletion on JVM exit.
     *
     * @param directory directory to delete, must not be {@code null}
     * @throws NullPointerException if the directory is {@code null}
     * @throws IOException          in case deletion is unsuccessful
     */
    private static void deleteDirectoryOnExit(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        directory.deleteOnExit();
        if (!isSymlink(directory)) {
            cleanDirectoryOnExit(directory);
        }
    }

    /**
     * Cleans a directory without deleting it.
     *
     * @param directory directory to clean, must not be {@code null}
     * @throws NullPointerException if the directory is {@code null}
     * @throws IOException          in case cleaning is unsuccessful
     */
    private static void cleanDirectoryOnExit(File directory) throws IOException {
        IOException exception = null;
        for (File file : listFiles(directory)) {
            try {
                forceDeleteOnExit(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }
        if (null != exception) {
            throw exception;
        }
    }

    private static File[] listFiles(File directory) throws IOException {
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IOException(message);
        }

        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IOException(message);
        }

        File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            throw new IOException("Failed to list contents of " + directory);
        }
        return files;
    }

    public static boolean isSymlink(File file) throws IOException {
        if (file == null) {
            throw new NullPointerException("File must not be null");
        }
        if (IS_OS_WINDOWS) {
            return false;
        }
        File fileInCanonicalDir = null;
        if (file.getParent() == null) {
            fileInCanonicalDir = file;
        } else {
            File canonicalDir = file.getParentFile().getCanonicalFile();
            fileInCanonicalDir = new File(canonicalDir, file.getName());
        }

        if (fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile())) {
            return false;
        } else {
            return true;
        }
    }
}
