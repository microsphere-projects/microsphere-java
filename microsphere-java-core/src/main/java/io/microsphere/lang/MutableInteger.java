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

package io.microsphere.lang;

/**
 * A mutable integer container that provides various atomic operations for integer addition,
 * increment, decrement, and value retrieval. This class extends {@link Number}, allowing it to be used
 * in contexts where a numeric representation is required.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * MutableInteger i = MutableInteger.of(5);
 *
 * // Get and set a new value
 * int oldValue = i.getAndSet(10); // Returns 5, value becomes 10
 *
 * // Increment and get the new value
 * int newValue = i.incrementAndGet(); // Returns 11, value becomes 11
 *
 * // Add a delta and get the updated value
 * int result = i.addAndGet(5); // Returns 16, value becomes 16
 *
 * // Get current value
 * int currentValue = i.get(); // Returns 16
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see java.util.concurrent.atomic.AtomicInteger
 * @since 1.0.0
 */
public class MutableInteger extends Number {

    private int value;

    public MutableInteger(int value) {
        super();
        this.value = value;
    }

    /**
     * Gets the current value stored in this {@link MutableInteger}.
     *
     * @return the current integer value
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MutableInteger i = new MutableInteger(5);
     * int currentValue = i.get(); // retrieves the current value
     * System.out.println(currentValue); // prints 5
     * }</pre>
     */
    public final int get() {
        return value;
    }

    /**
     * Sets the value to the given newValue and returns this instance.
     *
     * @param newValue the new value to set
     * @return this instance of {@link MutableInteger}
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MutableInteger i = new MutableInteger(0);
     * i.set(10); // sets the value to 10
     * System.out.println(i.get()); // prints 10
     * }</pre>
     */
    public final MutableInteger set(int newValue) {
        this.value = newValue;
        return this;
    }

    /**
     * Sets the value to the given {@code newValue} and returns the previous value.
     *
     * @param newValue the new value to set
     * @return the previous value before the update
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MutableInteger i = new MutableInteger(5);
     * int oldValue = i.getAndSet(10); // sets the value to 10, returns 5
     * System.out.println(oldValue); // prints 5
     * System.out.println(i.get()); // prints 10
     * }</pre>
     */
    public final int getAndSet(int newValue) {
        int oldValue = this.value;
        this.value = newValue;
        return oldValue;
    }

    /**
     * increments the current value by 1 and returns the previous value before the increment.
     *
     * <p>This method behaves similarly to a combination of the {@code get()} and {@code incrementAndGet()}
     * methods, where the current value is returned before it is increased by 1.</p>
     *
     * @return the value before incrementing
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MutableInteger i = new MutableInteger(5);
     * int oldValue = i.getAndIncrement(); // increments the value to 6, returns 5
     * System.out.println(oldValue); // prints 5
     * System.out.println(i.get()); // prints 6
     * }</pre>
     */
    public final int getAndIncrement() {
        return value++;
    }

    /**
     * Decrements the current value by 1 and returns the previous value before the decrement.
     *
     * <p>This method behaves similarly to a combination of the {@code get()} and {@code decrementAndGet()}
     * methods, where the current value is returned before it is decreased by 1.</p>
     *
     * @return the value before decrementing
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MutableInteger i = new MutableInteger(5);
     * int oldValue = i.getAndDecrement(); // decrements the value to 4, returns 5
     * System.out.println(oldValue); // prints 5
     * System.out.println(i.get()); // prints 4
     * }</pre>
     */
    public final int getAndDecrement() {
        return value--;
    }

    /**
     * Adds the given {@code delta} to the current value and returns the previous value before the addition.
     *
     * <p>This method ensures that the current value is returned before the addition operation
     * is performed, similar to a combination of the {@code get()} and {@code addAndGet(int)} methods.</p>
     *
     * @param delta the value to add to the current value
     * @return the previous value before the addition
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MutableInteger i = new MutableInteger(5);
     * int oldValue = i.getAndAdd(3); // adds 3 to the current value (5), returns 5
     * System.out.println(oldValue); // prints 5
     * System.out.println(i.get()); // prints 8
     * }</pre>
     */
    public final int getAndAdd(int delta) {
        int oldValue = this.value;
        this.value = oldValue + delta;
        return oldValue;
    }

    /**
     * Increments the current value by 1 and returns the updated value.
     *
     * <p>This method behaves similarly to the {@code ++value} operation,
     * where the value is increased by 1 before it is returned.</p>
     *
     * @return the updated value after incrementing
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MutableInteger i = new MutableInteger(5);
     * int newValue = i.incrementAndGet(); // increments the value to 6, returns 6
     * System.out.println(newValue); // prints 6
     * System.out.println(i.get()); // prints 6
     * }</pre>
     */
    public final int incrementAndGet() {
        return ++value;
    }

    /**
     * Decrements the current value by 1 and returns the updated value.
     *
     * <p>This method behaves similarly to the {@code --value} operation,
     * where the value is decreased by 1 before it is returned.</p>
     *
     * @return the updated value after decrementing
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MutableInteger i = new MutableInteger(5);
     * int newValue = i.decrementAndGet(); // decrements the value to 4, returns 4
     * System.out.println(newValue); // prints 4
     * System.out.println(i.get()); // prints 4
     * }</pre>
     */
    public final int decrementAndGet() {
        return --value;
    }

    /**
     * Adds the given {@code delta} to the current value and returns the updated value.
     *
     * <p>This method ensures that the new value is calculated and stored before it is returned,
     * behaving similarly to the compound addition operation followed by a retrieval.</p>
     *
     * @param delta the value to add to the current value
     * @return the updated value after adding the delta
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MutableInteger i = new MutableInteger(5);
     * int newValue = i.addAndGet(3); // adds 3 to the current value (5), returns 8
     * System.out.println(newValue); // prints 8
     * System.out.println(i.get()); // prints 8
     * }</pre>
     */
    public int addAndGet(int delta) {
        int value = this.value + delta;
        this.value = value;
        return value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    /**
     * Returns the integer value stored in this {@link MutableInteger}.
     *
     * <p>This method provides the implementation for the {@link Number} class's abstract method,
     * allowing this class to be used where a {@code Number} is expected.</p>
     *
     * @return the current integer value
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MutableInteger i = new MutableInteger(7);
     * int value = i.intValue(); // retrieves the current integer value
     * System.out.println(value); // prints 7
     * }</pre>
     */
    @Override
    public int intValue() {
        return get();
    }

    @Override
    /**
     * Returns the current value as a {@code long}.
     *
     * <p>This method provides the implementation for the {@link Number} class's abstract method,
     * allowing this class to be used where a {@code Number} is expected.</p>
     *
     * @return the current value as a {@code long}
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MutableInteger i = new MutableInteger(5);
     * long value = i.longValue(); // retrieves the current value as a long
     * System.out.println(value); // prints 5
     * }</pre>
     */
    public long longValue() {
        return get();
    }

    /**
     * Returns the current value as a {@code float}.
     *
     * <p>This method provides the implementation for the {@link Number} class's abstract method,
     * allowing this class to be used where a {@code Number} is expected.</p>
     *
     * @return the current value as a {@code float}
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MutableInteger i = new MutableInteger(5);
     * float value = i.floatValue(); // retrieves the current value as a float
     * System.out.println(value); // prints 5.0
     * }</pre>
     */
    @Override
    public float floatValue() {
        return get();
    }

    /**
     * Returns the current value as a {@code double}.
     *
     * <p>This method provides the implementation for the {@link Number} class's abstract method,
     * allowing this class to be used where a {@code Number} is expected.</p>
     *
     * @return the current value as a {@code double}
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MutableInteger i = new MutableInteger(5);
     * double value = i.doubleValue(); // retrieves the current value as a double
     * System.out.println(value); // prints 5.0
     * }</pre>
     */
    @Override
    public double doubleValue() {
        return get();
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MutableInteger)) {
            return false;
        }
        return value == ((MutableInteger) obj).value;
    }

    /**
     * Creates a new instance of {@link MutableInteger} initialized with the given value.
     *
     * <p>This static factory method provides a convenient way to create an instance of
     * {@link MutableInteger} with the specified initial value.</p>
     *
     * @param value the initial value for the {@link MutableInteger}
     * @return a new instance of {@link MutableInteger} initialized with the given value
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MutableInteger i = MutableInteger.of(10); // creates an MutableInteger with initial value 10
     * System.out.println(i.get()); // prints 10
     * }</pre>
     */
    public static MutableInteger of(int value) {
        return new MutableInteger(value);
    }
}
