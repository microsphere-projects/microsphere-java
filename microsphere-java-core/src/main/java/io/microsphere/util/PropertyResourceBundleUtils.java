/**
 *
 */
package io.microsphere.util;

import io.microsphere.annotation.Nonnull;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import static io.microsphere.util.PropertyResourceBundleControl.newControl;
import static io.microsphere.util.SystemUtils.FILE_ENCODING;
import static java.lang.System.getProperty;
import static java.lang.Thread.currentThread;
import static java.util.Locale.getDefault;

/**
 * {@link PropertyResourceBundle} Utility class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see PropertyResourceBundle
 * @since 1.0.0
 */
public abstract class PropertyResourceBundleUtils implements Utils {

    /**
     * The system property name for specifying the default encoding used by {@link PropertyResourceBundle}.
     * <p>
     * This property is typically used to set the character encoding for reading properties files.
     * If not explicitly set, the value of this property defaults to the platform's file encoding ({@link SystemUtils#FILE_ENCODING}).
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Setting the default encoding to UTF-8 programmatically
     * System.setProperty(PropertyResourceBundleUtils.DEFAULT_ENCODING_PROPERTY_NAME, "UTF-8");
     *
     * // Then using it when loading a bundle
     * ResourceBundle bundle = PropertyResourceBundleUtils.getBundle("my.resources.Messages");
     * }</pre>
     *
     * @see PropertyResourceBundle
     * @see SystemUtils#FILE_ENCODING
     */
    public static final String DEFAULT_ENCODING_PROPERTY_NAME = "java.util.PropertyResourceBundle.encoding";

    /**
     * The default encoding for {@link PropertyResourceBundle}, retrieved from the system property
     * specified by {@link #DEFAULT_ENCODING_PROPERTY_NAME}. If not explicitly set, it falls back to
     * the platform's file encoding ({@link SystemUtils#FILE_ENCODING}).
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Programmatically setting the default encoding to UTF-8
     * System.setProperty(PropertyResourceBundleUtils.DEFAULT_ENCODING_PROPERTY_NAME, "UTF-8");
     *
     * // Getting the default encoding
     * String encoding = PropertyResourceBundleUtils.DEFAULT_ENCODING;
     * }</pre>
     */
    public static final String DEFAULT_ENCODING = getProperty(DEFAULT_ENCODING_PROPERTY_NAME, FILE_ENCODING);

    /**
     * Gets a resource bundle using the specified base name and the default encoding.
     * <p>
     * {@link ResourceBundle#getBundle(String, Locale)} with {@link #DEFAULT_ENCODING default file encoding}
     * encoding and {@link Locale#getDefault() default Locale} under {@link Thread#getContextClassLoader() Thread
     * context ClassLoader}
     * <p>
     * This method is a convenience shortcut for:
     * </p>
     *
     * <pre>{@code
     * getBundle(baseName, PropertyResourceBundleUtils.DEFAULT_ENCODING);
     * }</pre>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ResourceBundle bundle = PropertyResourceBundleUtils.getBundle("my.resources.Messages");
     * }</pre>
     *
     * @param baseName the base name of the resource bundle, a fully qualified class name
     * @return the resource bundle
     * @throws NullPointerException     if <code>baseName</code> is <code>null</code>
     * @throws MissingResourceException if no resource bundle for the specified base name can be found
     * @throws IllegalArgumentException if the given control doesn't perform properly (e.g., <code>control.getCandidateLocales</code>
     *                                  returns null)
     */
    @Nonnull
    public static ResourceBundle getBundle(String baseName) {
        return getBundle(baseName, DEFAULT_ENCODING);
    }

    /**
     * Retrieves a resource bundle using the specified base name and character encoding.
     *
     * <p>This method loads the resource bundle with the given base name and uses the specified
     * encoding for reading the properties file. It uses the default locale ({@link Locale#getDefault()})
     * and the thread context class loader to locate and load the bundle.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Load a ResourceBundle with a specific encoding (e.g., UTF-8)
     * ResourceBundle bundle = PropertyResourceBundleUtils.getBundle("my.resources.Messages", "UTF-8");
     * }</pre>
     *
     * @param baseName the base name of the resource bundle, typically a fully qualified class name
     * @param encoding the character encoding used to read the properties file, such as "UTF-8"
     * @return the loaded resource bundle
     * @throws NullPointerException     if the baseName or encoding is null
     * @throws MissingResourceException if no resource bundle can be found for the specified base name
     * @throws IllegalArgumentException if the encoding is not supported or the control object behaves improperly
     */
    @Nonnull
    public static ResourceBundle getBundle(String baseName, String encoding) {
        return getBundle(baseName, getDefault(), encoding);
    }

    /**
     * Retrieves a resource bundle using the specified base name, locale, and character encoding.
     *
     * <p>This method loads the resource bundle with the given base name and locale,
     * using the specified encoding for reading the properties file. It uses the thread context class loader
     * to locate and load the bundle.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Load a ResourceBundle for a specific locale and encoding
     * ResourceBundle bundle = PropertyResourceBundleUtils.getBundle("my.resources.Messages", Locale.FRENCH, "UTF-8");
     * }</pre>
     *
     * @param baseName the base name of the resource bundle, typically a fully qualified class name
     * @param locale   the locale for which a resource bundle is desired
     * @param encoding the character encoding used to read the properties file, such as "UTF-8"
     * @return the loaded resource bundle
     * @throws NullPointerException     if the baseName, locale, or encoding is null
     * @throws MissingResourceException if no resource bundle can be found for the specified base name and locale
     * @throws IllegalArgumentException if the encoding is not supported or the control object behaves improperly
     */
    @Nonnull
    public static ResourceBundle getBundle(String baseName, Locale locale, String encoding) {
        ClassLoader classLoader = currentThread().getContextClassLoader();
        return getBundle(baseName, locale, classLoader, encoding);
    }

    /**
     * Retrieves a resource bundle using the specified base name, locale, class loader, and character encoding.
     *
     * <p>This method loads the resource bundle with the given base name and locale,
     * using the specified class loader and character encoding for reading the properties file.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Load a ResourceBundle with a specific locale, class loader, and encoding
     * ClassLoader loader = MyClass.class.getClassLoader();
     * ResourceBundle bundle = PropertyResourceBundleUtils.getBundle("my.resources.Messages", Locale.GERMAN, loader, "UTF-8");
     * }</pre>
     *
     * @param baseName    the base name of the resource bundle, typically a fully qualified class name
     * @param locale      the locale for which a resource bundle is desired
     * @param classLoader the class loader to use for loading the bundle
     * @param encoding    the character encoding used to read the properties file, such as "UTF-8"
     * @return the loaded resource bundle
     * @throws NullPointerException     if any of the parameters (baseName, locale, classLoader, encoding) is null
     * @throws MissingResourceException if no resource bundle can be found for the specified base name and locale
     * @throws IllegalArgumentException if the encoding is not supported or the control object behaves improperly
     */
    @Nonnull
    public static ResourceBundle getBundle(String baseName, Locale locale, ClassLoader classLoader, String encoding) {
        ResourceBundle.Control control = newControl(encoding);
        return ResourceBundle.getBundle(baseName, locale, classLoader, control);
    }

    private PropertyResourceBundleUtils() {
    }
}
