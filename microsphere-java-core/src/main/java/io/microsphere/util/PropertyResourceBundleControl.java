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

    public final List<String> getFormats(String baseName) {
        if (baseName == null) {
            throw new NullPointerException();
        }
        return FORMAT_PROPERTIES;
    }

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
                        if (connection != null) {
                            // Disable caches to get fresh data for
                            // reloading.
                            connection.setUseCaches(false);
                            is = connection.getInputStream();
                        }
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

        if (stream != null) {
            try {
                reader = new InputStreamReader(stream, this.getEncoding());
                bundle = new PropertyResourceBundle(reader);
            } finally {
                close(stream);
                close(reader);
            }
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
