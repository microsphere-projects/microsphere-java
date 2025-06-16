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

package io.microsphere.annotation.processor.model.util;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.AbstractElementVisitor6;
import javax.lang.model.util.ElementKindVisitor6;
import java.util.List;

import static java.lang.Boolean.FALSE;

/**
 * {@link ElementVisitor} to assemble the JSON content
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractElementVisitor6
 * @since 1.0.0
 */
public abstract class JSONElementVisitor extends ElementKindVisitor6<Boolean, StringBuilder> {

    public JSONElementVisitor() {
        super(FALSE);
    }

    @Override
    public final Boolean visitPackage(PackageElement e, StringBuilder jsonBuilder) {
        return supportsPackage(e) && doVisitPackage(e, jsonBuilder);
    }

    @Override
    public final Boolean visitVariable(VariableElement e, StringBuilder stringBuilder) {
        return supportsVariable(e) && super.visitVariable(e, stringBuilder);
    }

    @Override
    public final Boolean visitExecutable(ExecutableElement e, StringBuilder jsonBuilder) {
        return supportsExecutable(e) && super.visitExecutable(e, jsonBuilder);
    }

    @Override
    public final Boolean visitType(TypeElement e, StringBuilder jsonBuilder) {
        boolean appended = false;
        if (supportsType(e) && super.visitType(e, jsonBuilder)) {
            appended = true;
        }

        // The declared members of the type element
        if (visitMembers(e.getEnclosedElements(), jsonBuilder)) {
            appended = true;
        }

        return appended;
    }

    @Override
    public final Boolean visitTypeParameter(TypeParameterElement e, StringBuilder jsonBuilder) {
        if (!supports(e)) {
            return FALSE;
        }

        boolean appended = false;
        if (supportsTypeParameter(e) && doVisitTypeParameter(e, jsonBuilder)) {
            appended = true;
        }

        // The declared members of the type element
        if (visitMembers(e.getEnclosedElements(), jsonBuilder)) {
            appended = true;
        }

        return appended;
    }

    protected boolean visitMembers(List<? extends Element> members, StringBuilder jsonBuilder) {
        boolean appended = false;
        for (Element member : members) {
            if (member.accept(this, jsonBuilder)) {
                appended = true;
            }
        }
        return appended;
    }

    /**
     * Determines whether the specified element is supported for processing.
     *
     * <p>This method can be overridden to provide custom logic for deciding
     * if an element should be processed by this visitor.</p>
     *
     * @param e the element to check
     * @return {@code true} if the element is supported; {@code false} otherwise
     */
    protected boolean supports(Element e) {
        return true;
    }

    /**
     * Determines whether the specified package element is supported for processing.
     *
     * <p>This method can be overridden to provide custom logic for deciding
     * if a package element should be processed by this visitor.</p>
     *
     * @param e the package element to check
     * @return {@code true} if the package element is supported; {@code false} otherwise
     */
    protected boolean supportsPackage(PackageElement e) {
        return supports(e);
    }

    /**
     * Determines whether the specified variable element is supported for processing.
     *
     * <p>This method can be overridden to provide custom logic for deciding
     * if a variable element should be processed by this visitor.</p>
     *
     * @param e the variable element to check
     * @return {@code true} if the variable element is supported; {@code false} otherwise
     */
    protected boolean supportsVariable(VariableElement e) {
        return supports(e);
    }

    /**
     * Determines whether the specified executable element is supported for processing.
     *
     * <p>This method can be overridden to provide custom logic for deciding
     * if an executable element should be processed by this visitor.</p>
     *
     * @param e the executable element to check
     * @return {@code true} if the executable element is supported; {@code false} otherwise
     */
    protected boolean supportsExecutable(ExecutableElement e) {
        return supports(e);
    }

    /**
     * Determines whether the specified type element is supported for processing.
     *
     * <p>This method can be overridden to provide custom logic for deciding
     * if a type element should be processed by this visitor.</p>
     *
     * @param e the type element to check
     * @return {@code true} if the type element is supported; {@code false} otherwise
     */
    protected boolean supportsType(TypeElement e) {
        return supports(e);
    }

    /**
     * Determines whether the specified type parameter element is supported for processing.
     *
     * <p>This method can be overridden to provide custom logic for deciding
     * if a type parameter element should be processed by this visitor.</p>
     *
     * @param e the type parameter element to check
     * @return {@code true} if the type parameter element is supported; {@code false} otherwise
     */
    protected boolean supportsTypeParameter(TypeParameterElement e) {
        return supports(e);
    }

    /**
     * Visits a package element and appends its JSON representation to the builder.
     *
     * @param e           the package element to visit
     * @param jsonBuilder the string builder used to construct the JSON output
     * @return {@code true} if any content was appended; {@code false} otherwise
     */
    protected boolean doVisitPackage(PackageElement e, StringBuilder jsonBuilder) {
        return super.visitPackage(e, jsonBuilder);
    }

    /**
     * Visits a type parameter element and appends its JSON representation to the builder.
     *
     * @param e           the type parameter element to visit
     * @param jsonBuilder the string builder used to construct the JSON output
     * @return {@code true} if any content was appended; {@code false} otherwise
     */
    protected boolean doVisitTypeParameter(TypeParameterElement e, StringBuilder jsonBuilder) {
        return super.visitTypeParameter(e, jsonBuilder);
    }
}
