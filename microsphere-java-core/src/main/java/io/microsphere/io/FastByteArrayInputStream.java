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

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import static java.lang.System.arraycopy;
import static java.util.Objects.hash;

/**
 * Fast(No ThreadSafe without synchronization) {@link ByteArrayInputStream}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ByteArrayInputStream
 * @since 1.0.0
 */
public class FastByteArrayInputStream extends ByteArrayInputStream {

    /**
     * Creates a <code>FastByteArrayInputStream</code>
     * so that it  uses <code>buf</code> as its
     * buffer array.
     * The buffer array is not copied.
     * The initial value of <code>pos</code>
     * is <code>0</code> and the initial value
     * of  <code>count</code> is the length of
     * <code>buf</code>.
     *
     * @param buf the input buffer.
     */
    public FastByteArrayInputStream(byte[] buf) {
        super(buf);
    }

    /**
     * Creates <code>FastByteArrayInputStream</code>
     * that uses <code>buf</code> as its
     * buffer array. The initial value of <code>pos</code>
     * is <code>offset</code> and the initial value
     * of <code>count</code> is the minimum of <code>offset+length</code>
     * and <code>buf.length</code>.
     * The buffer array is not copied. The buffer's mark is
     * set to the specified offset.
     *
     * @param buf    the input buffer.
     * @param offset the offset in the buffer of the first byte to read.
     * @param length the maximum number of bytes to read from the buffer.
     */
    public FastByteArrayInputStream(byte[] buf, int offset, int length) {
        super(buf, offset, length);
    }

    @Override
    public int read() {
        return (pos < count) ? (buf[pos++] & 0xff) : -1;
    }

    @Override
    public int read(byte[] b, int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }

        if (pos >= count) {
            return -1;
        }

        int avail = count - pos;
        if (len > avail) {
            len = avail;
        }
        if (len <= 0) {
            return 0;
        }
        arraycopy(buf, pos, b, off, len);
        pos += len;
        return len;
    }

    @Override
    public long skip(long n) {
        long k = count - pos;
        if (n < k) {
            k = n < 0 ? 0 : n;
        }

        pos += k;
        return k;
    }

    @Override
    public int available() {
        return count - pos;
    }

    @Override
    public void reset() {
        pos = mark;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FastByteArrayInputStream)) {
            return false;
        }
        return Arrays.equals(this.buf, ((FastByteArrayInputStream) obj).buf);
    }

    @Override
    public int hashCode() {
        return hash(this.buf);
    }
}
