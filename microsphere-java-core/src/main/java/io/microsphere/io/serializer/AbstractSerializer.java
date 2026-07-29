package io.microsphere.io.serializer;

import io.microsphere.annotation.Nonnull;
import io.microsphere.io.IOUtils;
import io.microsphere.reflect.JavaType;

import java.io.IOException;

import static io.microsphere.io.IOUtils.UNBOUND_BYTES_SIZE;
import static io.microsphere.reflect.JavaType.from;

/**
 * Convenience base class for {@link Serializer} and {@liink Deserializer} implementations that handles null-safety
 * and fixed-length byte-array validation, delegating the actual encode/decode logic to
 * {@link #doSerialize(Object)} and {@link #doDeserialize(byte[])}.
 *
 * <p>The expected serialized byte-array length is determined by {@link #calcBytesLength()}.
 * Sub-classes that always produce arrays of a fixed size (e.g. 4 bytes for {@link Integer})
 * should override this method to return that size; those with variable-length output should
 * return {@link IOUtils#UNBOUND_BYTES_SIZE}.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   public class IntegerSerializer extends AbstractSerializer<Integer> {
 *
 *       public static final IntegerSerializer INTEGER_SERIALIZER = new IntegerSerializer();
 *
 *       @Override
 *       protected int calcBytesLength() {
 *           return INTEGER_BYTES_LENGTH; // 4
 *       }
 *
 *       @Override
 *       protected byte[] doSerialize(Integer value) {
 *           ByteBuffer buffer = ByteBuffer.allocate(INTEGER_BYTES_LENGTH);
 *           buffer.putInt(value);
 *           return buffer.array();
 *       }
 *
 *       @Override
 *       protected Integer doDeserialize(byte[] bytes) {
 *           return ByteBuffer.wrap(bytes).getInt();
 *       }
 *   }
 *
 *   byte[] bytes = INTEGER_SERIALIZER.serialize(42);  // 4-byte big-endian int
 *   int value   = INTEGER_SERIALIZER.deserialize(bytes); // 42
 * }</pre>
 *
 * @param <T> Serialized/Deserialized type
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class AbstractSerializer<T> implements Serializer<T>, Deserializer<T> {

    private final Class<T> targetType;

    private final int bytesLength;

    /**
     * Resolves the serializable type parameter {@code T} via reflection and pre-calculates
     * the expected serialized byte-array length.
     */
    public AbstractSerializer() {
        JavaType type = from(getClass()).as(AbstractSerializer.class).getGenericType(0);
        this.targetType = type.toClass();
        this.bytesLength = calcBytesLength();
    }

    @Override
    public final byte[] serialize(T t) throws IOException {
        // null compatible case
        if (t == null) {
            return null;
        }
        return doSerialize(t);
    }

    @Override
    public final T deserialize(byte[] bytes) throws IOException {
        // null compatible case
        if (bytes == null) {
            return null;
        }

        // Compatible byte array fixed case
        if (bytesLength != UNBOUND_BYTES_SIZE && bytesLength != bytes.length) {
            return null;
        }

        return doDeserialize(bytes);
    }

    /**
     * Returns the target type {@code T} that this serializer/deserializer handles.
     *
     * @return the target type class
     */
    public final Class<T> getTargetType() {
        return targetType;
    }

    /**
     * Returns the expected length of the serialized byte array, or {@link IOUtils#UNBOUND_BYTES_SIZE}
     * if variable-length serialization is used.
     *
     * @return the fixed bytes length (e.g. {@code 4} for {@code Integer}), or {@code -1}
     */
    public int getBytesLength() {
        return bytesLength;
    }

    /**
     * Calculates the expected fixed serialized byte-array length.
     * Sub-classes with a fixed-size representation must override this to return the correct size.
     *
     * @return the fixed byte length, or {@link IOUtils#UNBOUND_BYTES_SIZE} ({@code -1}) for variable-length
     */
    protected int calcBytesLength() {
        return UNBOUND_BYTES_SIZE;
    }

    /**
     * Performs the actual serialization of a non-null {@code t} value.
     *
     * @param t the value to serialize; never {@code null}
     * @return the serialized byte array; must not be {@code null}
     * @throws IOException if serialization fails
     */
    protected abstract byte[] doSerialize(@Nonnull T t) throws IOException;

    /**
     * Performs the actual deserialization of a non-null {@code bytes} array.
     *
     * @param bytes the byte array to deserialize; never {@code null}
     * @return the deserialized value
     * @throws IOException if deserialization fails
     */
    protected abstract T doDeserialize(@Nonnull byte[] bytes) throws IOException;
}