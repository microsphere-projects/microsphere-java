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

package io.microsphere.annotation.processor;

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.annotation.processor.model.util.ConfigurationPropertyJSONElementVisitor;
import io.microsphere.json.JSONArray;
import io.microsphere.metadata.ConfigurationPropertyJSONGenerator;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static io.microsphere.annotation.processor.model.util.ConfigurationPropertyJSONElementVisitor.CONFIGURATION_PROPERTY_ANNOTATION_CLASS_NAME;
import static io.microsphere.annotation.processor.util.MessagerUtils.printNote;
import static io.microsphere.constants.SymbolConstants.COMMA_CHAR;
import static io.microsphere.constants.SymbolConstants.LEFT_SQUARE_BRACKET_CHAR;
import static io.microsphere.constants.SymbolConstants.RIGHT_SQUARE_BRACKET_CHAR;
import static io.microsphere.metadata.ConfigurationPropertyLoader.loadAll;
import static javax.lang.model.SourceVersion.latestSupported;
import static javax.tools.StandardLocation.CLASS_OUTPUT;

/**
 * The {@link Processor} for the {@link ConfigurationProperty} annotation
 *
 * <p>This class processes the {@link ConfigurationProperty} annotations during the compilation phase,
 * collects metadata about annotated elements, and generates a JSON metadata file that contains
 * configuration property information.
 *
 * <ul>
 *     <li>{@link #init(ProcessingEnvironment)} initializes required utilities such as the Messager and ResourceProcessor.</li>
 *     <li>{@link #process(Set, RoundEnvironment)} handles each processing round:
 *         <ul>
 *             <li>During normal rounds, it resolves metadata from annotated elements.</li>
 *             <li>On the final round, it writes collected metadata into a resource file.</li>
 *         </ul>
 *     </li>
 *     <li>{@link #resolveMetadata(RoundEnvironment)} traverses all root elements to extract configuration property metadata.</li>
 *     <li>{@link #writeMetadata()} writes the generated metadata into a JSON file under
 *         {@value #CONFIGURATION_PROPERTY_METADATA_RESOURCE_NAME} using a writer.</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationProperty
 * @see ConfigurationPropertyJSONElementVisitor
 * @see ConfigurationPropertyJSONGenerator
 * @see ResourceProcessor
 * @see Messager
 * @see ProcessingEnvironment
 * @since 1.0.0
 */
@SupportedAnnotationTypes(value = CONFIGURATION_PROPERTY_ANNOTATION_CLASS_NAME)
public class ConfigurationPropertyAnnotationProcessor extends AbstractProcessor {

    /**
     * The resource name of {@link ConfigurationProperty} metadata
     */
    public static final String CONFIGURATION_PROPERTY_METADATA_RESOURCE_NAME = "META-INF/microsphere/configuration-properties.json";

    private Messager messager;

    private StringBuilder jsonBuilder;

    private ResourceProcessor classPathResourceProcessor;

    private ConfigurationPropertyJSONElementVisitor jsonElementVisitor;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.jsonBuilder = new StringBuilder();
        this.classPathResourceProcessor = new ResourceProcessor(processingEnv, CLASS_OUTPUT);
        this.jsonElementVisitor = new ConfigurationPropertyJSONElementVisitor(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            writeMetadata();
        } else {
            resolveMetadata(roundEnv);
        }
        return false;
    }

    private void resolveMetadata(RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getRootElements();
        if (!elements.isEmpty()) {

            Iterator<? extends Element> iterator = elements.iterator();
            jsonBuilder.append(LEFT_SQUARE_BRACKET_CHAR);
            while (iterator.hasNext()) {
                Element element = iterator.next();
                element.accept(jsonElementVisitor, jsonBuilder);
            }

            // append the JSON content generated by ConfigurationPropertyGenerator SPI
            appendGeneratedConfigurationPropertyJSON(jsonBuilder);

            int lastIndex = jsonBuilder.length() - 1;
            if (COMMA_CHAR == jsonBuilder.charAt(lastIndex)) {
                jsonBuilder.setCharAt(lastIndex, RIGHT_SQUARE_BRACKET_CHAR);
            } else {
                jsonBuilder.append(RIGHT_SQUARE_BRACKET_CHAR);
            }
        }
    }

    private void appendGeneratedConfigurationPropertyJSON(StringBuilder jsonBuilder) {
        List<io.microsphere.beans.ConfigurationProperty> configurationProperties = loadAll();
        int size = configurationProperties.size();
        for (int i = 0; i < size; i++) {
            appendGeneratedConfigurationPropertyJSON(jsonBuilder, configurationProperties.get(i));
        }
    }

    private void appendGeneratedConfigurationPropertyJSON(StringBuilder jsonBuilder, io.microsphere.beans.ConfigurationProperty configurationProperty) {
        ConfigurationPropertyJSONGenerator generator = this.jsonElementVisitor.getGenerator();
        String json = generator.generate(configurationProperty);
        jsonBuilder.append(json)
                .append(COMMA_CHAR);
    }

    private void writeMetadata() {
        classPathResourceProcessor.processInResourceWriter(CONFIGURATION_PROPERTY_METADATA_RESOURCE_NAME, writer -> {
            JSONArray jsonArray = new JSONArray(jsonBuilder.toString());
            String formatedJSON = jsonArray.toString(2);
            writer.write(formatedJSON);
            printNote(this.messager, "The generated metadata JSON of @{} : \n{}", CONFIGURATION_PROPERTY_ANNOTATION_CLASS_NAME, formatedJSON);
        });
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return latestSupported();
    }
}
 