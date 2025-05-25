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
package io.microsphere.annotation.processor.util;

import io.microsphere.annotation.processor.AbstractAnnotationProcessingTest;
import io.microsphere.annotation.processor.model.Model;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.List;

import static io.microsphere.annotation.processor.util.MemberUtils.findAllDeclaredMembers;
import static io.microsphere.annotation.processor.util.MemberUtils.findDeclaredMembers;
import static io.microsphere.annotation.processor.util.MemberUtils.getAllDeclaredMembers;
import static io.microsphere.annotation.processor.util.MemberUtils.getDeclaredMembers;
import static io.microsphere.lang.function.Predicates.alwaysFalse;
import static io.microsphere.lang.function.Predicates.alwaysTrue;
import static javax.lang.model.util.ElementFilter.fieldsIn;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link MemberUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class MemberUtilsTest extends AbstractAnnotationProcessingTest {

    @Test
    public void testGetDeclaredMembers() {
        assertGetDeclaredMembersOfModel();
    }

    @Test
    public void testGetDeclaredMembersOnNul() {
        assertEmptyList(getDeclaredMembers(NULL_TYPE_ELEMENT));
        assertEmptyList(getDeclaredMembers(NULL_TYPE_MIRROR));
    }

    @Test
    public void testGetAllDeclaredMembers() {
        assertGetAllDeclaredMembersOfModel();
    }

    @Test
    public void testGetAllDeclaredMembersOnNul() {
        assertEmptyList(getAllDeclaredMembers(NULL_TYPE_ELEMENT));
        assertEmptyList(getAllDeclaredMembers(NULL_TYPE_MIRROR));
    }

    @Test
    public void testFindDeclaredMembers() {
        assertFindDeclaredMembersOfModel();
    }

    @Test
    public void testFindDeclaredMembersOnNul() {
        assertEmptyList(findDeclaredMembers(NULL_TYPE_ELEMENT, alwaysTrue()));
        assertEmptyList(findDeclaredMembers(NULL_TYPE_ELEMENT, alwaysFalse()));
        assertEmptyList(findDeclaredMembers(NULL_TYPE_MIRROR, alwaysTrue()));
        assertEmptyList(findDeclaredMembers(NULL_TYPE_MIRROR, alwaysFalse()));
    }

    @Test
    public void testFindAllDeclaredMembers() {
        assertFindAllDeclaredMembersOfModel();
    }

    @Test
    public void testFindAllDeclaredMembersOnNul() {
        assertEmptyList(findAllDeclaredMembers(NULL_TYPE_ELEMENT, alwaysTrue()));
        assertEmptyList(findAllDeclaredMembers(NULL_TYPE_ELEMENT, alwaysFalse()));
        assertEmptyList(findAllDeclaredMembers(NULL_TYPE_MIRROR, alwaysTrue()));
        assertEmptyList(findAllDeclaredMembers(NULL_TYPE_MIRROR, alwaysFalse()));
    }

    private void assertFindDeclaredMembersOfModel() {
        TypeElement type = getTypeElement(Model.class);
        assertGetDeclaredMembersOfModel(findDeclaredMembers(type, alwaysTrue(), alwaysTrue()));
        assertGetDeclaredMembersOfModel(findDeclaredMembers(type.asType(), alwaysTrue()));

        assertEmptyList(findDeclaredMembers(type, alwaysFalse()));
        assertEmptyList(findDeclaredMembers(type.asType(), alwaysFalse()));
    }

    private void assertFindAllDeclaredMembersOfModel() {
        TypeElement type = getTypeElement(Model.class);
        assertGetAllDeclaredMembersOfModel(findAllDeclaredMembers(type, alwaysTrue(), alwaysTrue()));
        assertGetAllDeclaredMembersOfModel(findAllDeclaredMembers(type.asType(), alwaysTrue()));

        assertEmptyList(findAllDeclaredMembers(type, alwaysFalse()));
        assertEmptyList(findAllDeclaredMembers(type.asType(), alwaysFalse()));
    }

    private void assertGetDeclaredMembersOfModel() {
        TypeElement type = getTypeElement(Model.class);
        assertGetDeclaredMembersOfModel(getDeclaredMembers(type));
        assertGetDeclaredMembersOfModel(getDeclaredMembers(type.asType()));
    }

    private void assertGetDeclaredMembersOfModel(List<? extends Element> members) {
        List<VariableElement> fields = fieldsIn(members);
        assertEquals(19, members.size());
        assertEquals(6, fields.size());
        assertEquals("f", fields.get(0).getSimpleName().toString());
        assertEquals("d", fields.get(1).getSimpleName().toString());
        assertEquals("tu", fields.get(2).getSimpleName().toString());
        assertEquals("str", fields.get(3).getSimpleName().toString());
        assertEquals("bi", fields.get(4).getSimpleName().toString());
        assertEquals("bd", fields.get(5).getSimpleName().toString());
    }

    private void assertGetAllDeclaredMembersOfModel() {
        TypeElement type = getTypeElement(Model.class);
        assertGetAllDeclaredMembersOfModel(getAllDeclaredMembers(type));
        assertGetAllDeclaredMembersOfModel(getAllDeclaredMembers(type.asType()));
    }

    private void assertGetAllDeclaredMembersOfModel(List<? extends Element> members) {
        List<VariableElement> fields = fieldsIn(members);
        assertEquals(11, fields.size());
        assertEquals("f", fields.get(0).getSimpleName().toString());
        assertEquals("d", fields.get(1).getSimpleName().toString());
        assertEquals("tu", fields.get(2).getSimpleName().toString());
        assertEquals("str", fields.get(3).getSimpleName().toString());
        assertEquals("bi", fields.get(4).getSimpleName().toString());
        assertEquals("bd", fields.get(5).getSimpleName().toString());
        assertEquals("b", fields.get(6).getSimpleName().toString());
        assertEquals("s", fields.get(7).getSimpleName().toString());
        assertEquals("i", fields.get(8).getSimpleName().toString());
        assertEquals("l", fields.get(9).getSimpleName().toString());
        assertEquals("z", fields.get(10).getSimpleName().toString());
    }
}
