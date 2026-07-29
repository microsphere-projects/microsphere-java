package io.microsphere.io.serializer;

import java.io.IOException;

import static io.microsphere.io.serializer.LongSerializer.LONG_SERIALIZER;
import static io.microsphere.util.SizeUtils.DOUBLE_BYTES_SIZE;
import static java.lang.Double.doubleToLongBits;
import static java.lang.Double.longBitsToDouble;

/**
 * Java {@code double} or {@link Double} type {@link Serializer} and {@link Deserializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public final class DoubleSerializer extends AbstractSerializer<Double> {

    public static final DoubleSerializer DOUBLE_SERIALIZER = new DoubleSerializer();

    @Override
    protected int calcBytesLength() {
        return DOUBLE_BYTES_SIZE;
    }

    @Override
    protected byte[] doSerialize(Double aDouble) throws IOException {
        double doubleValue = aDouble.doubleValue();
        long longValue = doubleToLongBits(doubleValue);
        return LONG_SERIALIZER.serialize(longValue);
    }

    @Override
    protected Double doDeserialize(byte[] bytes) throws IOException {
        long longValue = LONG_SERIALIZER.deserialize(bytes);
        double doubleValue = longBitsToDouble(longValue);
        return doubleValue;
    }
}
