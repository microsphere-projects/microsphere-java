package io.microsphere.text;

import io.microsphere.util.Utils;

import static io.microsphere.util.StringUtils.isBlank;
import static java.lang.String.valueOf;

/**
 * The utility class of text format
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class FormatUtils implements Utils {

    public static final String DEFAULT_PLACEHOLDER = "{}";

    /**
     * Formats the given {@code pattern} by replacing all occurrences of the default placeholder
     * ({@link #DEFAULT_PLACEHOLDER}) with corresponding values from the provided arguments.
     *
     * <p>
     * If there are more placeholders in the pattern than arguments provided, extra placeholders will remain unchanged.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * format("Hello, {}", "World") => "Hello, World"
     * format("{} + {} = 4", 2, 2) => "2 + 2 = 4"
     * format("No replacement here", "extra") => "No replacement here"
     * }</pre>
     *
     * @param pattern the string pattern to be formatted
     * @param args    the arguments to replace the placeholders in the pattern
     * @return the formatted string, with placeholders replaced by corresponding argument values
     */
    public static String format(String pattern, Object... args) {
        return formatWithPlaceholder(pattern, DEFAULT_PLACEHOLDER, args);
    }

    /**
     * Formats the given {@code pattern} by replacing all occurrences of the specified placeholder
     * with corresponding values from the provided arguments.
     *
     * <p>
     * If there are more placeholders in the pattern than arguments provided, extra placeholders will remain unchanged.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * formatWithPlaceholder("Hello, [placeholder]", "[placeholder]", "World") => "Hello, World"
     * formatWithPlaceholder("[placeholder] + [placeholder] = 4", "[placeholder]", 2, 2) => "2 + 2 = 4"
     * formatWithPlaceholder("No replacement here", "[placeholder]", "extra") => "No replacement here"
     * }</pre>
     *
     * @param pattern     the string pattern to be formatted
     * @param placeholder the placeholder string to be replaced
     * @param args        the arguments to replace the placeholders in the pattern
     * @return the formatted string, with placeholders replaced by corresponding argument values
     */
    public static String formatWithPlaceholder(String pattern, String placeholder, Object... args) {
        if (isBlank(pattern)) {
            return pattern;
        }
        int offset = placeholder.length();
        int argsLength = args == null ? 0 : args.length;
        if (argsLength == 0) {
            return pattern;
        }
        StringBuilder stringBuilder = new StringBuilder(pattern);
        int index = -1;
        for (int i = 0; i < argsLength; i++) {
            index = stringBuilder.indexOf(placeholder);
            if (index == -1) {
                break;
            }
            String value = valueOf(args[i]);
            stringBuilder.replace(index, index + offset, value);
        }
        return stringBuilder.toString();
    }

    private FormatUtils() {
    }
}
