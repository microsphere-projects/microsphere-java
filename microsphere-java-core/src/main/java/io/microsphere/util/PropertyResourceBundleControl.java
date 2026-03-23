/**
 *
 */
package io.microsphere.util;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.UnsupportedCharsetException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.microsphere.io.IOUtils.close;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.PropertyResourceBundleUtils.DEFAULT_ENCODING;
import static java.nio.charset.Charset.forName;
import static java.security.AccessController.doPrivileged;

/**
 * A {@link ResourceBundle.Control} implementation for loading property-based resource bundles with customizable
 * character encoding.
 *
 * <p>{@link PropertyResourceBundleControl} allows specifying the character encoding used when reading properties files,
 * ensuring correct interpretation of non-ASCII characters. By default, it uses the platform's default encoding unless
 * explicitly configured otherwise.</p>
 *
 * <h3>Example Usage</h3>
 * <pre>
 * // Create a control with UTF-8 encoding
 * ResourceBundle.Control utf8Control = new PropertyResourceBundleControl("UTF-8");
 *
 * // Load a resource bundle using the custom control
 * ResourceBundle bundle = ResourceBundle.getBundle("my.resources.Messages", Locale.US, utf8Control);
 * </pre>
 *
 * <p>This class is thread-safe and can be reused across multiple calls to {@link ResourceBundle#getBundle(String,
 * ResourceBundle.Control)}.</p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertyResourceBundle
 * @see ResourceBundle.Control
 * @since 1.0.0
 */
public class PropertyResourceBundleControl extends ResourceBundle.Control {

    /**
     * The suffix of the properties file
     */
    public static final String SUFFIX = "properties";

    private static final ConcurrentMap<String, ResourceBundle.Control> encodingControlMap = new ConcurrentHashMap<String, ResourceBundle.Control>();

    /**
     * The singleton instance of {@link PropertyResourceBundleControl} as default
     */
    public static final PropertyResourceBundleControl DEFAULT_CONTROL;

    static {
        DEFAULT_CONTROL = new PropertyResourceBundleControl();
        // Add a Control as default
        encodingControlMap.put(DEFAULT_CONTROL.getEncoding(), DEFAULT_CONTROL);
        // Add "UTF-8" Control
        newControl("UTF-8");
    }

    private final String encoding;

    /**
     * Constructs a {@link PropertyResourceBundleControl} with the default encoding.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   PropertyResourceBundleControl control = new PropertyResourceBundleControl();
     *   String encoding = control.getEncoding();
     *   System.out.println("Default encoding: " + encoding);
     * }</pre>
     *
     * @throws UnsupportedCharsetException if the default encoding is not supported
     * @since 1.0.0
     */
    protected PropertyResourceBundleControl() throws UnsupportedCharsetException {
        this(DEFAULT_ENCODING);
    }

    /**
     * @param encoding the encoding
     * @throws UnsupportedCharsetException If <code>encoding</code> is not supported
     */
    protected PropertyResourceBundleControl(final String encoding) throws UnsupportedCharsetException {
        // check encoding
        forName(encoding);
        this.encoding = encoding;
    }

    /**
     * Returns the list of supported formats for the given base name, restricted to properties files only.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   PropertyResourceBundleControl control = PropertyResourceBundleControl.DEFAULT_CONTROL;
     *   List<String> formats = control.getFormats("my.resources.Messages");
     *   System.out.println("Supported formats: " + formats);
     * }</pre>
     *
     * @param baseName the base name of the resource bundle
     * @return a list containing the properties format
     * @throws NullPointerException if {@code baseName} is {@code null}
     * @since 1.0.0
     */
    public final List<String> getFormats(String baseName) {
        if (baseName == null) {
            throw new NullPointerException();
        }
        return FORMAT_PROPERTIES;
    }

    /**
     * Creates a new {@link ResourceBundle} instance for the given parameters, reading the properties
     * file with the configured character encoding.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   PropertyResourceBundleControl control = PropertyResourceBundleControl.DEFAULT_CONTROL;
     *   ResourceBundle bundle = control.newBundle(
     *       "my.resources.Messages", Locale.US, "java.properties",
     *       Thread.currentThread().getContextClassLoader(), false
     *   );
     *   System.out.println(bundle.getString("greeting"));
     * }</pre>
     *
     * @param baseName    the base name of the resource bundle
     * @param locale      the locale for which the resource bundle should be loaded
     * @param format      the resource bundle format to be loaded
     * @param classLoader the class loader to use for loading the resource
     * @param reload      whether to bypass the cache and reload the resource
     * @return a new {@link ResourceBundle} instance, or {@code null} if the resource is not found
     * @throws IOException if an I/O error occurs while reading the resource
     * @since 1.0.0
     */
    public ResourceBundle newBundle(String baseName, Locale locale, String format, final ClassLoader classLoader, final boolean reload) throws IOException {
        String bundleName = super.toBundleName(baseName, locale);
        final String resourceName = super.toResourceName(bundleName, SUFFIX);
        InputStream stream = null;
        Reader reader = null;
        ResourceBundle bundle = null;
        try {
            stream = doPrivileged((PrivilegedExceptionAction<InputStream>) () -> {
                InputStream is = null;
                if (reload) {
                    URL url = classLoader.getResource(resourceName);
                    if (url != null) {
                        URLConnection connection = url.openConnection();
                        // Disable caches to get fresh data for reloading.
                        connection.setUseCaches(false);
                        is = connection.getInputStream();
                    }
                } else {
                    is = classLoader.getResourceAsStream(resourceName);
                }
                if (is == null) {
                    String message = format("The resource[name : '{}' , baseName : '{}' , locale : '{}' , reload : {}] can't be found in the ClassLoader : {}", resourceName, baseName, locale, reload, classLoader);
                    throw new IOException(message);
                }
                return is;
            });
        } catch (PrivilegedActionException e) {
            throw (IOException) e.getException();
        }

        try {
            reader = new InputStreamReader(stream, this.getEncoding());
            bundle = new PropertyResourceBundle(reader);
        } finally {
            close(stream);
            close(reader);
        }

        return bundle;
    }

    /**
     * Sets the encoding of properties file.
     *
     * @return the encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Creates a new instance of {@link PropertyResourceBundleControl} if absent.
     *
     * @param encoding Encoding
     * @return Control
     * @throws UnsupportedCharsetException If <code>encoding</code> is not supported
     */
    public static ResourceBundle.Control newControl(final String encoding) throws UnsupportedCharsetException {
        return encodingControlMap.computeIfAbsent(encoding, PropertyResourceBundleControl::new);
    }

}
