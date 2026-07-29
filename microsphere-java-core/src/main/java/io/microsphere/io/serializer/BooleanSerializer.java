package io.microsphere.io.serializer;

import static io.microsphere.io.IOUtils.BOOLEAN_BYTES_SIZE;

/**
 * Java {@code boolean} or {@link Boolean} type {@link Serializer} and {@link Deserializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractSerializer
 * @since 1.0.0
 */
public final class BooleanSerializer extends AbstractSerializer<Boolean> {

    public static final BooleanSerializer BOOLEAN_SERIALIZER = new BooleanSerializer();

    private static final byte TRUE_VALUE = 1;

    private static final byte FALSE_VALUE = 0;

    @Override
    protected int calcBytesLength() {
        return BOOLEAN_BYTES_SIZE;
    }

    @Override
    protected byte[] doSerialize(Boolean booleanValue) {
        byte byteValue = booleanValue ? TRUE_VALUE : FALSE_VALUE;
        byte[] bytes = new byte[]{byteValue};
        return bytes;
    }

    @Override
    protected Boolean doDeserialize(byte[] bytes) {
        byte byteValue = bytes[0];
        return byteValue == TRUE_VALUE ? true : false;
    }
}