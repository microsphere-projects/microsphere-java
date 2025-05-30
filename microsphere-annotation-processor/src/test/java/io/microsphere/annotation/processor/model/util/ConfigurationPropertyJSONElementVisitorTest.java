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

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.annotation.processor.AbstractAnnotationProcessingTest;
import io.microsphere.classloading.ManifestArtifactResourceResolver;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.Element;
import java.util.Iterator;
import java.util.Set;

import static io.microsphere.annotation.processor.util.LoggerUtils.trace;
import static io.microsphere.constants.SymbolConstants.COMMA_CHAR;
import static io.microsphere.constants.SymbolConstants.LEFT_SQUARE_BRACKET_CHAR;
import static io.microsphere.constants.SymbolConstants.RIGHT_SQUARE_BRACKET_CHAR;

/**
 * {@link ConfigurationPropertyJSONElementVisitor} for
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ConfigurationProperty
 * @since 1.0.0
 */
public class ConfigurationPropertyJSONElementVisitorTest extends AbstractAnnotationProcessingTest {

    @Override
    protected void addCompiledClasses(Set<Class<?>> compiledClasses) {
        compiledClasses.add(ManifestArtifactResourceResolver.class);
    }

    @Test
    public void test() {
        Set<? extends Element> elements = this.roundEnv.getRootElements();
        StringBuilder jsonBuilder = new StringBuilder();
        ConfigurationPropertyJSONElementVisitor visitor = new ConfigurationPropertyJSONElementVisitor(this.processingEnv);
        Iterator<? extends Element> iterator = elements.iterator();
        jsonBuilder.append(LEFT_SQUARE_BRACKET_CHAR);
        while (iterator.hasNext()) {
            Element element = iterator.next();
            if (element.accept(visitor, jsonBuilder)) {
                jsonBuilder.append(COMMA_CHAR);
            }
        }

        int lastIndex = jsonBuilder.length() - 1;
        if (COMMA_CHAR == jsonBuilder.charAt(lastIndex)) {
            jsonBuilder.setCharAt(lastIndex, RIGHT_SQUARE_BRACKET_CHAR);
        } else {
            jsonBuilder.append(RIGHT_SQUARE_BRACKET_CHAR);
        }

        trace(jsonBuilder.toString());
    }
//
//    @Test
//    public void test2() throws Throwable {
//        Filer filer = processingEnv.getFiler();
//        FileObject fileObject = filer.createResource(CLASS_OUTPUT, "", "META-INF/test.json");
//        try (OutputStream outputStream = fileObject.openOutputStream()) {
//            outputStream.write("{}".getBytes());
//        }
//    }
}
