package io.microsphere.util;

/**
 * The utilities class for {@link CharSequence}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see CharSequence
 * @since 1.0.0
 */
public abstract class CharSequenceUtils implements Utils {


    /**
     * Returns the length of the provided {@link CharSequence}.
     * <p>
     * If the provided value is {@code null}, this method returns {@code 0}.
     * Otherwise, it returns the result of calling the {@link CharSequence#length()} method.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <ul>
     *     <li>{@code length(null)} returns {@code 0}</li>
     *     <li>{@code length("")} returns {@code 0}</li>
     *     <li>{@code length("hello")}} returns {@code 5}</li>
     * </ul>
     *
     * @param value the {@link CharSequence} to get the length from
     * @return the length of the provided value, or {@code 0} if it is {@code null}
     */
    public static int length(CharSequence value) {
        return value == null ? 0 : value.length();
    }

    /**
     * Checks if the provided {@link CharSequence} is empty.
     * <p>
     * A {@link CharSequence} is considered empty if its length is {@code 0}.
     * This method returns {@code true} if the provided value is {@code null}
     * or has a length of {@code 0}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <ul>
     *     <li>{@code isEmpty(null)} returns {@code true}</li>
     *     <li>{@code isEmpty("")} returns {@code true}</li>
     *     <li>{@code isEmpty("hello")}} returns {@code false}</li>
     * </ul>
     *
     * @param value the {@link CharSequence} to check
     * @return {@code true} if the provided {@link CharSequence} is empty or null, otherwise {@code false}
     */
    public static boolean isEmpty(CharSequence value) {
        return length(value) == 0;
    }

    /**
     * Checks if the provided {@link CharSequence} is not empty.
     * <p>
     * A {@link CharSequence} is considered not empty if its length is greater than {@code 0}.
     * This method returns {@code true} if the provided value is not {@code null} and has a length greater than {@code 0}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <ul>
     *     <li>{@code isNotEmpty(null)} returns {@code false}</li>
     *     <li>{@code isNotEmpty("")} returns {@code false}</li>
     *     <li>{@code isNotEmpty("hello")}} returns {@code true}</li>
     * </ul>
     *
     * @param value the {@link CharSequence} to check
     * @return {@code true} if the provided {@link CharSequence} is not empty, otherwise {@code false}
     */
    public static boolean isNotEmpty(CharSequence value) {
        return length(value) > 0;
    }

    private CharSequenceUtils() {
    }
}
