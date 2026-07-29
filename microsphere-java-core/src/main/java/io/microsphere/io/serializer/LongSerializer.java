package io.microsphere.io.serializer;

import static io.microsphere.util.SizeUtils.LONG_BYTES_SIZE;

/**
 * Java {@code long} or {@link Long} type {@link Serializer} and {@link Deserializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public final class LongSerializer extends AbstractSerializer<Long> {

    public static final LongSerializer LONG_SERIALIZER = new LongSerializer();

    @Override
    protected int calcBytesLength() {
        return LONG_BYTES_SIZE;
    }

    @Override
    protected byte[] doSerialize(Long aLong) {
        long longValue = aLong.longValue();
        byte[] bytes = new byte[]{
                (byte) longValue,
                (byte) (longValue >> 8),
                (byte) (longValue >> 16),
                (byte) (longValue >> 24),
                (byte) (longValue >> 32),
                (byte) (longValue >> 40),
                (byte) (longValue >> 48),
                (byte) (longValue >> 56)
        };
        return bytes;
    }

    @Override
    protected Long doDeserialize(byte[] bytes) {
        long longValue = ((long) bytes[7] << 56)
                | ((long) bytes[6] & 0xff) << 48
                | ((long) bytes[5] & 0xff) << 40
                | ((long) bytes[4] & 0xff) << 32
                | ((long) bytes[3] & 0xff) << 24
                | ((long) bytes[2] & 0xff) << 16
                | ((long) bytes[1] & 0xff) << 8
                | ((long) bytes[0] & 0xff);
        return longValue;
    }
}