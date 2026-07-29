package io.microsphere.io.serializer;

/**
 * byte[] {@link Serializer} and {@link Deserializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public final class ByteArraySerializer implements Serializer<byte[]>, Deserializer<byte[]> {

    public static final ByteArraySerializer BYTE_ARRAY_SERIALIZER = new ByteArraySerializer();

    @Override
    public byte[] serialize(byte[] bytes) {
        return bytes;
    }

    @Override
    public byte[] deserialize(byte[] bytes) {
        return bytes;
    }

    public Class<byte[]> getTargetType() {
        return byte[].class;
    }
}