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

import java.util.Arrays;

/**
 * Data Model
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class Data {

    private String name;

    private int age;

    private boolean male;

    private double height;

    private float weight;

    private long birth;

    private short index;

    private byte grade;

    private char sex;

    private Object object;

    private String[] names;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isMale() {
        return male;
    }

    public void setMale(boolean male) {
        this.male = male;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public long getBirth() {
        return birth;
    }

    public void setBirth(long birth) {
        this.birth = birth;
    }

    public short getIndex() {
        return index;
    }

    public void setIndex(short index) {
        this.index = index;
    }

    public byte getGrade() {
        return grade;
    }

    public void setGrade(byte grade) {
        this.grade = grade;
    }

    public char getSex() {
        return sex;
    }

    public void setSex(char sex) {
        this.sex = sex;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public String[] getNames() {
        return names;
    }

    public void setNames(String[] names) {
        this.names = names;
    }

    @Override
    public String toString() {
        return "Data{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", male=" + male +
                ", height=" + height +
                ", weight=" + weight +
                ", birth=" + birth +
                ", index=" + index +
                ", grade=" + grade +
                ", sex=" + sex +
                ", object=" + object +
                ", names=" + Arrays.toString(names) +
                '}';
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Data)) return false;

        Data data = (Data) o;
        return age == data.age
                && male == data.male
                && Double.compare(height, data.height) == 0
                && Float.compare(weight, data.weight) == 0
                && birth == data.birth
                && index == data.index
                && grade == data.grade
                && sex == data.sex
                && name.equals(data.name)
                && object.equals(data.object)
                && Arrays.equals(names, data.names);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + age;
        result = 31 * result + Boolean.hashCode(male);
        result = 31 * result + Double.hashCode(height);
        result = 31 * result + Float.hashCode(weight);
        result = 31 * result + Long.hashCode(birth);
        result = 31 * result + index;
        result = 31 * result + grade;
        result = 31 * result + sex;
        result = 31 * result + object.hashCode();
        result = 31 * result + Arrays.hashCode(names);
        return result;
    }
}
