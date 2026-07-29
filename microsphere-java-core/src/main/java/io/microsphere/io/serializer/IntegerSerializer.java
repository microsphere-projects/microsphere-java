package io.microsphere.io.serializer;

import static io.microsphere.io.IOUtils.INTEGER_BYTES_SIZE;

/**
 * Java {@code int} or {@link Integer} type {@link Serializer} and {@link Deserializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class IntegerSerializer extends AbstractSerializer<Integer> {

    public static final IntegerSerializer INTEGER_SERIALIZER = new IntegerSerializer();

    @Override
    protected int calcBytesLength() {
        return INTEGER_BYTES_SIZE;
    }

    @Override
    protected byte[] doSerialize(Integer integer) {
        int intValue = integer.intValue();
        byte[] bytes = new byte[]{
                (byte) ((intValue >> 24) & 0xff),
                (byte) ((intValue >> 16) & 0xff),
                (byte) ((intValue >> 8) & 0xff),
                (byte) ((intValue >> 0) & 0xff),
        };
        return bytes;
    }

    @Override
    protected Integer doDeserialize(byte[] bytes) {
        int intValue = (0xff & bytes[0]) << 24 |
                (0xff & bytes[1]) << 16 |
                (0xff & bytes[2]) << 8 |
                (0xff & bytes[3]) << 0;
        return intValue;
    }

}