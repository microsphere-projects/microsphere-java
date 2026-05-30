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
package io.microsphere.test.model;

/**
 * Array Type Model
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ArrayTypeModel {

    private int[] integers;                           // Primitive type array

    private String[] strings;                         // Simple type array

    private PrimitiveTypeModel[] primitiveTypeModels; // Complex type array

    private Model[] models;                           // Hierarchical Complex type array

    private Color[] colors;                           // Enum type array

    public int[] getIntegers() {
        return integers;
    }

    public void setIntegers(int[] integers) {
        this.integers = integers;
    }

    public String[] getStrings() {
        return strings;
    }

    public void setStrings(String[] strings) {
        this.strings = strings;
    }

    public PrimitiveTypeModel[] getPrimitiveTypeModels() {
        return primitiveTypeModels;
    }

    public void setPrimitiveTypeModels(PrimitiveTypeModel[] primitiveTypeModels) {
        this.primitiveTypeModels = primitiveTypeModels;
    }

    public Model[] getModels() {
        return models;
    }

    public void setModels(Model[] models) {
        this.models = models;
    }

    public Color[] getColors() {
        return colors;
    }

    public void setColors(Color[] colors) {
        this.colors = colors;
    }
}