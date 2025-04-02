package io.microsphere.util;

/**
 * The utilities class for {@link CharSequence}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see CharSequence
 * @since 1.0.0
 */
public abstract class CharSequenceUtils extends BaseUtils {

    public static int length(CharSequence value) {
        return value == null ? 0 : value.length();
    }

    public static boolean isEmpty(CharSequence value) {
        return length(value) == 0;
    }

    public static boolean isNotEmpty(CharSequence value) {
        return length(value) > 0;
    }
}
