/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import static java.util.Arrays.copyOf;

/**
 * Fast(No ThreadSafe without synchronization) {@link ByteArrayOutputStream}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ByteArrayOutputStream
 * @since 1.0.0
 */
public class FastByteArrayOutputStream extends ByteArrayOutputStream {

    /**
     * {@inheritDoc}
     */
    public FastByteArrayOutputStream() {
        this(32);
    }

    /**
     * {@inheritDoc}
     */
    public FastByteArrayOutputStream(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative initial size: " + size);
        }
        buf = new byte[size];
    }

    /**
     * Increases the capacity if necessary to ensure that it can hold
     * at least the number of elements specified by the minimum
     * capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     * @throws OutOfMemoryError if {@code minCapacity < 0}.  This is
     *                          interpreted as a request for the unsatisfiably large capacity
     *                          {@code (long) Integer.MAX_VALUE + (minCapacity - Integer.MAX_VALUE)}.
     */
    private void ensureCapacity(int minCapacity) {
        // overflow-conscious code
        if (minCapacity - buf.length > 0)
            grow(minCapacity);
    }

    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * Increases the capacity to ensure that it can hold at least the
     * number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = buf.length;
        int newCapacity = oldCapacity << 1;
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0) {
            throw new OutOfMemoryError();
        }
        buf = copyOf(buf, newCapacity);
    }

    /**
     * {@inheritDoc}
     */
    public void write(int b) {
        ensureCapacity(count + 1);
        buf[count] = (byte) b;
        count += 1;
    }

    /**
     * {@inheritDoc}
     */
    public void write(byte[] b, int off, int len) {
        if ((off < 0) || (off > b.length) || (len < 0) ||
                ((off + len) - b.length > 0)) {
            throw new IndexOutOfBoundsException();
        }
        ensureCapacity(count + len);
        System.arraycopy(b, off, buf, count, len);
        count += len;
    }

    /**
     * {@inheritDoc}
     */
    public void writeTo(OutputStream out) throws IOException {
        out.write(buf, 0, count);
    }

    /**
     * {@inheritDoc}
     */
    public void reset() {
        count = 0;
    }

    /**
     * {@inheritDoc}
     */
    public byte[] toByteArray() {
        return copyOf(buf, count);
    }

    /**
     * {@inheritDoc}
     */
    public int size() {
        return count;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return new String(buf, 0, count);
    }

    /**
     * {@inheritDoc}
     */
    public String toString(String charsetName)
            throws UnsupportedEncodingException {
        return new String(buf, 0, count, charsetName);
    }

    /**
     * {@inheritDoc}
     */
    public void close() {
    }
}
