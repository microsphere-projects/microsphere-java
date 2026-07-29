package io.microsphere.io.serializer;

import static io.microsphere.util.SizeUtils.SHORT_BYTES_SIZE;

/**
 * Java {@code boolean} or {@link Boolean} type {@link Serializer} and {@link Deserializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ShortSerializer extends AbstractSerializer<Short> {

    public static final ShortSerializer SHORT_SERIALIZER = new ShortSerializer();

    @Override
    protected int calcBytesLength() {
        return SHORT_BYTES_SIZE;
    }

    @Override
    protected byte[] doSerialize(Short aShort) {
        short shortValue = aShort.shortValue();
        byte[] bytes = new byte[]{
                (byte) (shortValue >>> 8),
                (byte) (shortValue & 0xFF)};
        return bytes;
    }

    @Override
    protected Short doDeserialize(byte[] bytes) {
        return (short) ((bytes[0] << 8) | (bytes[1] & 0xFF));
    }

}