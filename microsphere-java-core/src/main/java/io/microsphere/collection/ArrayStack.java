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

package io.microsphere.collection;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Stack;

/**
 * The {@code Stack} class represents a last-in-first-out
 * (LIFO) stack of objects. It extends class {@code Vector} with five
 * operations that allow a vector to be treated as a stack. The usual
 * {@code push} and {@code pop} operations are provided, as well as a
 * method to {@code peek} at the top item on the stack, a method to test
 * for whether the stack is {@code empty}, and a method to {@code search}
 * the stack for an item and discover how far it is from the top.
 * <p>
 * The non thread-safe version of {@link Stack}.
 *
 * @param <E> Type of component elements
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ArrayList
 * @see Stack
 * @since 1.0.0
 */
public class ArrayStack<E> extends ArrayList<E> {

    private static final long serialVersionUID = -1417040223081465932L;

    /**
     * Creates an empty Stack.
     */
    public ArrayStack() {
        this(0);
    }

    /**
     * Constructs a new, empty stack with the specified initial
     * capacity.
     *
     * @param initialCapacity the initial capacity of the stack.
     * @throws IllegalArgumentException if the specified initial capacity
     *                                  is negative
     */
    public ArrayStack(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Pushes an item onto the top of this stack. This has exactly
     * the same effect as: {@link #add(Object)}
     *
     * @param item the item to be pushed onto this stack.
     * @return the {@code item} argument.
     * @see ArrayList#add(Object)
     */
    public E push(E item) {
        super.add(item);
        return item;
    }

    /**
     * Removes the object at the top of this stack and returns that
     * object as the value of this function.
     *
     * @return The object at the top of this stack (the last item
     * of the {@code ArrayList} object).
     * @throws EmptyStackException if this stack is empty.
     */
    public E pop() {
        E obj;
        int len = super.size();

        obj = this.peek();
        super.remove(len - 1);
        return obj;
    }

    /**
     * Looks at the object at the top of this stack without removing it
     * from the stack.
     *
     * @return the object at the top of this stack (the last item
     * of the {@code ArrayList} object).
     * @throws EmptyStackException if this stack is empty.
     */
    public E peek() throws EmptyStackException {
        int len = super.size();
        if (len == 0)
            throw new EmptyStackException();
        return super.get(len - 1);
    }

    /**
     * Tests if this stack is empty.
     *
     * @return {@code true} if and only if this stack contains
     * no items; {@code false} otherwise.
     */
    public boolean empty() {
        return super.isEmpty();
    }

    /**
     * Returns the 1-based position where an object is on this stack.
     * If the object {@code o} occurs as an item in this stack, this
     * method returns the distance from the top of the stack of the
     * occurrence nearest the top of the stack; the topmost item on the
     * stack is considered to be at distance {@code 1}. The {@code equals}
     * method is used to compare {@code o} to the
     * items in this stack.
     *
     * @param o the desired object.
     * @return the 1-based position from the top of the stack where
     * the object is located; the return value {@code -1}
     * indicates that the object is not on the stack.
     */
    public int search(Object o) {
        int i = super.lastIndexOf(o);

        if (i >= 0) {
            return super.size() - i;
        }
        return -1;
    }
}
