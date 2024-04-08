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
package io.microsphere.constants;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link SymbolConstants} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class SymbolConstantsTest {

    @Test
    public void test() {
        assertEquals(',', SymbolConstants.COMMA_CHAR);
        assertEquals(' ', SymbolConstants.SPACE_CHAR);
        assertEquals('!', SymbolConstants.EXCLAMATION_CHAR);
        assertEquals('"', SymbolConstants.DOUBLE_QUOTATION_CHAR);
        assertEquals('$', SymbolConstants.DOLLAR_CHAR);
        assertEquals('.', SymbolConstants.DOT_CHAR);
        assertEquals('&', SymbolConstants.AND_CHAR);
        assertEquals('=', SymbolConstants.EQUAL_CHAR);
        assertEquals('<', SymbolConstants.LESS_THAN_CHAR);
        assertEquals('>', SymbolConstants.GREATER_THAN_CHAR);
        assertEquals(':', SymbolConstants.COLON_CHAR);
        assertEquals(';', SymbolConstants.SEMICOLON_CHAR);
        assertEquals('#', SymbolConstants.SHARP_CHAR);
        assertEquals('?', SymbolConstants.QUESTION_MARK_CHAR);
        assertEquals('?', SymbolConstants.QUERY_STRING_CHAR);
        assertEquals('(', SymbolConstants.LEFT_PARENTHESIS_CHAR);
        assertEquals(')', SymbolConstants.RIGHT_PARENTHESIS_CHAR);
        assertEquals('_', SymbolConstants.UNDER_SCORE_CHAR);
        assertEquals('-', SymbolConstants.HYPHEN_CHAR);
        assertEquals('*', SymbolConstants.WILDCARD_CHAR);

        assertEquals(",", SymbolConstants.COMMA);
        assertEquals(" ", SymbolConstants.SPACE);
        assertEquals("!", SymbolConstants.EXCLAMATION);
        assertEquals("\"", SymbolConstants.DOUBLE_QUOTATION);
        assertEquals("$", SymbolConstants.DOLLAR);
        assertEquals(".", SymbolConstants.DOT);
        assertEquals("&", SymbolConstants.AND);
        assertEquals("=", SymbolConstants.EQUAL);
        assertEquals("<", SymbolConstants.LESS_THAN);
        assertEquals("<=", SymbolConstants.LESS_THAN_OR_EQUAL_TO);
        assertEquals(">", SymbolConstants.GREATER_THAN);
        assertEquals(">=", SymbolConstants.GREATER_THAN_OR_EQUAL_TO);
        assertEquals(":", SymbolConstants.COLON);
        assertEquals(";", SymbolConstants.SEMICOLON);
        assertEquals("#", SymbolConstants.SHARP);
        assertEquals("?", SymbolConstants.QUESTION_MARK);
        assertEquals("?", SymbolConstants.QUERY_STRING);
        assertEquals("(", SymbolConstants.LEFT_PARENTHESIS);
        assertEquals(")", SymbolConstants.RIGHT_PARENTHESIS);
        assertEquals("_", SymbolConstants.UNDER_SCORE);
        assertEquals("-", SymbolConstants.HYPHEN);
        assertEquals("*", SymbolConstants.WILDCARD);
    }
}
