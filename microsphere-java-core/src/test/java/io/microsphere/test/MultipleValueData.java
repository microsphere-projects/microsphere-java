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

package io.microsphere.test;

import io.microsphere.collection.CollectionUtils;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

/**
 * Multi-Value Data
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class MultipleValueData {

    private List<String> stringList;

    private Set<Integer> integerSet;

    private Queue<Data> dataQueue;

    private Enumeration<Class<?>> classEnumeration;

    private Object[] objects;

    public List<String> getStringList() {
        return stringList;
    }

    public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }

    public Set<Integer> getIntegerSet() {
        return integerSet;
    }

    public void setIntegerSet(Set<Integer> integerSet) {
        this.integerSet = integerSet;
    }

    public Queue<Data> getDataQueue() {
        return dataQueue;
    }

    public void setDataQueue(Queue<Data> dataQueue) {
        this.dataQueue = dataQueue;
    }

    public Enumeration<Class<?>> getClassEnumeration() {
        return classEnumeration;
    }

    public void setClassEnumeration(Enumeration<Class<?>> classEnumeration) {
        this.classEnumeration = classEnumeration;
    }

    public Object[] getObjects() {
        return objects;
    }

    public void setObjects(Object[] objects) {
        this.objects = objects;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof MultipleValueData)) return false;

        MultipleValueData that = (MultipleValueData) o;
        return Objects.equals(stringList, that.stringList)
                && Objects.equals(integerSet, that.integerSet)
                && CollectionUtils.equals(dataQueue, that.dataQueue)
                && Objects.equals(classEnumeration, that.classEnumeration)
                && Arrays.equals(objects, that.objects);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(stringList);
        result = 31 * result + Objects.hashCode(integerSet);
        result = 31 * result + Objects.hashCode(dataQueue);
        result = 31 * result + Objects.hashCode(classEnumeration);
        result = 31 * result + Arrays.hashCode(objects);
        return result;
    }

    @Override
    public String toString() {
        return "MultipleValueData{" +
                "stringList=" + stringList +
                ", integerSet=" + integerSet +
                ", dataQueue=" + dataQueue +
                ", classEnumeration=" + classEnumeration +
                ", objects=" + Arrays.toString(objects) +
                '}';
    }
}
