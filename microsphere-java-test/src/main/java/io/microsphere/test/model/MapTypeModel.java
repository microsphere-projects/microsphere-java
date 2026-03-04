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

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * {@link Map} Type model
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class MapTypeModel {

    private Map<String, String> strings; // The composite element is simple type

    private SortedMap<String, Color> colors;     // The composite element is Enum type

    private NavigableMap<Color, PrimitiveTypeModel> primitiveTypeModels;  // The composite element is POJO type

    private HashMap<String, Model> models;  // The composite element is hierarchical POJO type

    private TreeMap<PrimitiveTypeModel, Model[]> modelArrays; // The composite element is hierarchical POJO type

    public Map<String, String> getStrings() {
        return strings;
    }

    public void setStrings(Map<String, String> strings) {
        this.strings = strings;
    }

    public SortedMap<String, Color> getColors() {
        return colors;
    }

    public void setColors(SortedMap<String, Color> colors) {
        this.colors = colors;
    }

    public NavigableMap<Color, PrimitiveTypeModel> getPrimitiveTypeModels() {
        return primitiveTypeModels;
    }

    public void setPrimitiveTypeModels(NavigableMap<Color, PrimitiveTypeModel> primitiveTypeModels) {
        this.primitiveTypeModels = primitiveTypeModels;
    }

    public HashMap<String, Model> getModels() {
        return models;
    }

    public void setModels(HashMap<String, Model> models) {
        this.models = models;
    }

    public TreeMap<PrimitiveTypeModel, Model[]> getModelArrays() {
        return modelArrays;
    }

    public void setModelArrays(TreeMap<PrimitiveTypeModel, Model[]> modelArrays) {
        this.modelArrays = modelArrays;
    }
}