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
package io.microsphere.convert;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;

import static io.microsphere.convert.MapToPropertiesConverter.INSTANCE;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link MapToPropertiesConverter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class MapToPropertiesConverterTest extends BaseConverterTest<Map, Properties> {

    @Override
    protected AbstractConverter<Map, Properties> createConverter() {
        return INSTANCE;
    }

    @Override
    protected Map getSource() {
        return singletonMap("A", "1");
    }

    @Override
    protected Properties getTarget() {
        Properties properties = new Properties();
        properties.putAll(getSource());
        return properties;
    }

    @Test
    public void testConvertOnFailed() throws Throwable {
        Map<String, Object> map = singletonMap("key", null);
        assertThrows(NullPointerException.class, () -> this.converter.convert(map));
    }
}
