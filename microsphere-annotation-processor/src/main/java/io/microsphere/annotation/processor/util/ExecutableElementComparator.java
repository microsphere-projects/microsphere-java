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

import io.microsphere.util.CharSequenceComparator;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.Comparator;
import java.util.List;

/**
 * The Comparator class for {@link ExecutableElement}, the comparison rule :
 * <ol>
 *     <li>Comparing to two {@link ExecutableElement#getSimpleName() element names} {@link String#compareTo(String) lexicographically}.
 *     If equals, go to step 2</li>
 *     <li>Comparing to the count of two parameters. If equals, go to step 3</li>
 *     <li>Comparing to the type names of parameters {@link String#compareTo(String) lexicographically}</li>
 * </ol>
 *
 * <h3>Example:</h3>
 * <pre>
 * class Example {
 *     void methodA() {}
 *     void methodB() {}
 *     void methodB(String param1) {}
 *     void methodB(String param1, int param2) {}
 * }
 * </pre>
 *
 * <p>When comparing methods:</p>
 * <ul>
 *     <li>{@code methodA} vs {@code methodB}: the names are compared lexicographically.</li>
 *     <li>{@code methodB()} vs {@code methodB(String)}: the number of parameters is compared.</li>
 *     <li>{@code methodB(String)} vs {@code methodB(String, int)}: the parameter type names are compared lexicographically.</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ExecutableElementComparator implements Comparator<ExecutableElement> {

    /**
     * The singleton instance
     */
    public static final ExecutableElementComparator INSTANCE = new ExecutableElementComparator();

    private ExecutableElementComparator() {
    }

    @Override
    public int compare(ExecutableElement e1, ExecutableElement e2) {

        if (e1.equals(e2)) {
            return 0;
        }

        // Step 1
        int value = CharSequenceComparator.INSTANCE.compare(e1.getSimpleName(), e2.getSimpleName());

        if (value == 0) { // Step 2

            List<? extends VariableElement> ps1 = e1.getParameters();
            List<? extends VariableElement> ps2 = e2.getParameters();

            value = ps1.size() - ps2.size();

            if (value == 0) { // Step 3
                for (int i = 0; i < ps1.size(); i++) {
                    value = CharSequenceComparator.INSTANCE.compare(ps1.get(i).asType().toString(), ps2.get(i).asType().toString());
                    if (value != 0) {
                        break;
                    }
                }
            }
        }
        return value;
    }
}
