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
package io.microsphere.generated;

import java.io.PrintStream;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static io.microsphere.text.FormatUtils.format;

/**
 * System
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class SystemPropertySourceCodeGenerator {

    private static final String SYSTEM_PROPERTIES_LOCATION = "META-INF/System.properties";

    private static final String KEY_TEMPLATE =
            "    /**\n" +
                    "     * The System property key for the {}.\n" +
                    "     */\n" +
                    "    public static final String {} = \"{}\";\n\n";

    private static final String VALUE_TEMPLATE =
            "    /**\n" +
                    "     * The System property for the {}.\n" +
                    "     */\n" +
                    "    public static final String {} = getSystemProperty({});\n\n";


    public void generate(PrintStream out) throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(SYSTEM_PROPERTIES_LOCATION);
        Path resourcePath = Paths.get(resource.toURI());
        List<String> keyLines = new LinkedList<>();
        List<String> valueLines = new LinkedList<>();
        try (Reader reader = Files.newBufferedReader(resourcePath)) {
            Properties properties = new Properties();
            properties.load(reader);
            for (String key : properties.stringPropertyNames()) {
                String comment = properties.getProperty(key);
                String valueFieldName = key.toUpperCase().replace('.', '_');
                String keyFieldName = valueFieldName + "_PROPERTY_KEY";
                String keyLine = format(KEY_TEMPLATE, comment, keyFieldName, key);
                String valueLine = format(VALUE_TEMPLATE, comment, valueFieldName, keyFieldName);
                keyLines.add(keyLine);
                valueLines.add(valueLine);
            }
        }

        print(out, keyLines);

        print(out, valueLines);
    }

    private void print(PrintStream out, List<String> lines) {
        for (String line : lines) {
            out.print(line);
        }
    }

    public static void main(String[] args) throws Exception {
        SystemPropertySourceCodeGenerator generator = new SystemPropertySourceCodeGenerator();
        generator.generate(System.out);
    }
}
