package io.microsphere.io.serializer;

import java.io.IOException;

import static io.microsphere.io.serializer.ShortSerializer.SHORT_SERIALIZER;
import static io.microsphere.reflect.MethodUtils.invokeStaticMethod;
import static io.microsphere.util.SizeUtils.BYTE_BYTES_SIZE;
import static io.microsphere.util.SizeUtils.SHORT_BYTES_SIZE;
import static java.lang.Byte.MAX_VALUE;

/**
 * {@link Enum} {@link Serializer} and {@link Deserializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class EnumSerializer<E extends Enum> implements Serializer<E>, Deserializer<E> {

    private static final String VALUES_METHOD_NAME = "values";

    private final Class<E> enumType;

    private final E[] enums;

    private final int bytesLength;

    public EnumSerializer(Class<E> enumType) {
        this.enumType = enumType;
        this.enums = invokeValues();
        this.bytesLength = calcBytesLength(enums);
    }

    static <E extends Enum<E>> int calcBytesLength(E[] enums) {
        int enumsLength = enums.length;
        return enumsLength < MAX_VALUE ? BYTE_BYTES_SIZE : SHORT_BYTES_SIZE;
    }

    private E[] invokeValues() {
        return invokeStaticMethod(true, enumType, VALUES_METHOD_NAME);
    }

    @Override
    public byte[] serialize(Enum e) throws IOException {
        // null compatible case
        if (e == null) {
            return null;
        }

        int ordinal = e.ordinal();
        final byte[] bytes;

        if (bytesLength == BYTE_BYTES_SIZE) { // Most scenarios match
            bytes = new byte[1];
            bytes[0] = (byte) ordinal;
        } else {
            bytes = SHORT_SERIALIZER.serialize((short) ordinal);
        }

        return bytes;
    }

    @Override
    public E deserialize(byte[] bytes) throws IOException {
        // null compatible case
        if (bytes == null) {
            return null;
        }

        int ordinal = bytesLength == BYTE_BYTES_SIZE ? bytes[0] : SHORT_SERIALIZER.deserialize(bytes);
        return enums[ordinal];
    }

    public Class<E> getEnumType() {
        return enumType;
    }

    public int getBytesLength() {
        return bytesLength;
    }

    public Class<E> getTargetType() {
        return enumType;
    }
}