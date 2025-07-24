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

package io.microsphere.annotation.processor.model.element;


import io.microsphere.annotation.processor.model.util.ResolvableAnnotationValueVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link StringAnnotationValue} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see StringAnnotationValue
 * @since 1.0.0
 */
class StringAnnotationValueTest {

    private StringAnnotationValue value;

    @BeforeEach
    public void setUp() {
        this.value = new StringAnnotationValue("testing");
    }

    @Test
    void testGetValue() {
        assertEquals("testing", value.getValue());
    }

    @Test
    void testAccept() {
        ResolvableAnnotationValueVisitor visitor = new ResolvableAnnotationValueVisitor();
        assertEquals("testing", value.accept(visitor, null));
    }
}