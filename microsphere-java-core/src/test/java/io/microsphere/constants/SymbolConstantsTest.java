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

import static io.microsphere.constants.SymbolConstants.AND;
import static io.microsphere.constants.SymbolConstants.AND_CHAR;
import static io.microsphere.constants.SymbolConstants.AT;
import static io.microsphere.constants.SymbolConstants.AT_CHAR;
import static io.microsphere.constants.SymbolConstants.COLON;
import static io.microsphere.constants.SymbolConstants.COLON_CHAR;
import static io.microsphere.constants.SymbolConstants.COMMA;
import static io.microsphere.constants.SymbolConstants.COMMA_CHAR;
import static io.microsphere.constants.SymbolConstants.DOLLAR;
import static io.microsphere.constants.SymbolConstants.DOLLAR_CHAR;
import static io.microsphere.constants.SymbolConstants.DOT;
import static io.microsphere.constants.SymbolConstants.DOT_CHAR;
import static io.microsphere.constants.SymbolConstants.DOUBLE_QUOTATION;
import static io.microsphere.constants.SymbolConstants.DOUBLE_QUOTATION_CHAR;
import static io.microsphere.constants.SymbolConstants.EQUAL;
import static io.microsphere.constants.SymbolConstants.EQUAL_CHAR;
import static io.microsphere.constants.SymbolConstants.EXCLAMATION;
import static io.microsphere.constants.SymbolConstants.EXCLAMATION_CHAR;
import static io.microsphere.constants.SymbolConstants.GREATER_THAN;
import static io.microsphere.constants.SymbolConstants.GREATER_THAN_CHAR;
import static io.microsphere.constants.SymbolConstants.GREATER_THAN_OR_EQUAL_TO;
import static io.microsphere.constants.SymbolConstants.HYPHEN;
import static io.microsphere.constants.SymbolConstants.HYPHEN_CHAR;
import static io.microsphere.constants.SymbolConstants.LEFT_PARENTHESIS;
import static io.microsphere.constants.SymbolConstants.LEFT_PARENTHESIS_CHAR;
import static io.microsphere.constants.SymbolConstants.LESS_THAN;
import static io.microsphere.constants.SymbolConstants.LESS_THAN_CHAR;
import static io.microsphere.constants.SymbolConstants.LESS_THAN_OR_EQUAL_TO;
import static io.microsphere.constants.SymbolConstants.QUERY_STRING;
import static io.microsphere.constants.SymbolConstants.QUERY_STRING_CHAR;
import static io.microsphere.constants.SymbolConstants.QUESTION_MARK;
import static io.microsphere.constants.SymbolConstants.QUESTION_MARK_CHAR;
import static io.microsphere.constants.SymbolConstants.RIGHT_PARENTHESIS;
import static io.microsphere.constants.SymbolConstants.RIGHT_PARENTHESIS_CHAR;
import static io.microsphere.constants.SymbolConstants.SEMICOLON;
import static io.microsphere.constants.SymbolConstants.SEMICOLON_CHAR;
import static io.microsphere.constants.SymbolConstants.SHARP;
import static io.microsphere.constants.SymbolConstants.SHARP_CHAR;
import static io.microsphere.constants.SymbolConstants.SPACE;
import static io.microsphere.constants.SymbolConstants.SPACE_CHAR;
import static io.microsphere.constants.SymbolConstants.UNDER_SCORE;
import static io.microsphere.constants.SymbolConstants.UNDER_SCORE_CHAR;
import static io.microsphere.constants.SymbolConstants.WILDCARD;
import static io.microsphere.constants.SymbolConstants.WILDCARD_CHAR;
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
        assertEquals(',', COMMA_CHAR);
        assertEquals(' ', SPACE_CHAR);
        assertEquals('@', AT_CHAR);
        assertEquals('!', EXCLAMATION_CHAR);
        assertEquals('"', DOUBLE_QUOTATION_CHAR);
        assertEquals('$', DOLLAR_CHAR);
        assertEquals('.', DOT_CHAR);
        assertEquals('&', AND_CHAR);
        assertEquals('=', EQUAL_CHAR);
        assertEquals('<', LESS_THAN_CHAR);
        assertEquals('>', GREATER_THAN_CHAR);
        assertEquals(':', COLON_CHAR);
        assertEquals(';', SEMICOLON_CHAR);
        assertEquals('#', SHARP_CHAR);
        assertEquals('?', QUESTION_MARK_CHAR);
        assertEquals('?', QUERY_STRING_CHAR);
        assertEquals('(', LEFT_PARENTHESIS_CHAR);
        assertEquals(')', RIGHT_PARENTHESIS_CHAR);
        assertEquals('_', UNDER_SCORE_CHAR);
        assertEquals('-', HYPHEN_CHAR);
        assertEquals('*', WILDCARD_CHAR);

        assertEquals(",", COMMA);
        assertEquals(" ", SPACE);
        assertEquals("@", AT);
        assertEquals("!", EXCLAMATION);
        assertEquals("\"", DOUBLE_QUOTATION);
        assertEquals("$", DOLLAR);
        assertEquals(".", DOT);
        assertEquals("&", AND);
        assertEquals("=", EQUAL);
        assertEquals("<", LESS_THAN);
        assertEquals("<=", LESS_THAN_OR_EQUAL_TO);
        assertEquals(">", GREATER_THAN);
        assertEquals(">=", GREATER_THAN_OR_EQUAL_TO);
        assertEquals(":", COLON);
        assertEquals(";", SEMICOLON);
        assertEquals("#", SHARP);
        assertEquals("?", QUESTION_MARK);
        assertEquals("?", QUERY_STRING);
        assertEquals("(", LEFT_PARENTHESIS);
        assertEquals(")", RIGHT_PARENTHESIS);
        assertEquals("_", UNDER_SCORE);
        assertEquals("-", HYPHEN);
        assertEquals("*", WILDCARD);
    }
}
