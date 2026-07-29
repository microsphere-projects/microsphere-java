package io.microsphere.io.serializer;

import java.io.IOException;
import java.lang.reflect.Method;

import static io.microsphere.io.serializer.ShortSerializer.SHORT_SERIALIZER;
import static io.microsphere.reflect.AccessibleObjectUtils.trySetAccessible;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.reflect.MethodUtils.invokeStaticMethod;
import static java.lang.Byte.MAX_VALUE;
import static java.util.Objects.hash;

/**
 * {@link Enum} {@link Serializer} and {@link Deserializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class EnumSerializer<E extends Enum> implements Serializer<E>, Deserializer<E> {

    private static final String VALUES_METHOD_NAME = "values";

    private static final int BYTE_BYTES_LENGTH = 1;

    private static final int SHORT_BYTES_LENGTH = 2;

    private final Class<E> enumType;

    private final E[] enums;

    private final int bytesLength;

    public EnumSerializer(Class<E> enumType) {
        this.enumType = enumType;
        this.enums = invokeValues(getValuesMethod(enumType));
        this.bytesLength = calcBytesLength(enums);
    }

    private Method getValuesMethod(Class<E> enumType) {
        Method valuesMethod = findMethod(enumType, VALUES_METHOD_NAME);
        trySetAccessible(valuesMethod);
        return valuesMethod;
    }

    static <E extends Enum<E>> int calcBytesLength(E[] enums) {
        int enumsLength = enums.length;
        return enumsLength < MAX_VALUE ? BYTE_BYTES_LENGTH : SHORT_BYTES_LENGTH;
    }

    private E[] invokeValues(Method valuesMethod) {
        return invokeStaticMethod(valuesMethod);
    }

    @Override
    public byte[] serialize(Enum e) throws IOException {
        // null compatible case
        if (e == null) {
            return null;
        }

        int ordinal = e.ordinal();
        final byte[] bytes;

        if (bytesLength == BYTE_BYTES_LENGTH) { // Most scenarios match
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

        int ordinal = bytesLength == BYTE_BYTES_LENGTH ? bytes[0] : SHORT_SERIALIZER.deserialize(bytes);
        return enums[ordinal];
    }

    public Class<E> getEnumType() {
        return enumType;
    }

    public int getBytesLength() {
        return bytesLength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EnumSerializer)) return false;
        EnumSerializer that = (EnumSerializer) o;
        return enumType.equals(that.enumType);
    }

    @Override
    public int hashCode() {
        return hash(enumType);
    }

    public Class<E> getTargetType() {
        return enumType;
    }
}