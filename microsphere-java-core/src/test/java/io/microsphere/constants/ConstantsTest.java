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

import java.io.File;

import static io.microsphere.constants.Constants.AND;
import static io.microsphere.constants.Constants.AND_CHAR;
import static io.microsphere.constants.Constants.ARCHIVE_ENTRY_SEPARATOR;
import static io.microsphere.constants.Constants.BACK_SLASH;
import static io.microsphere.constants.Constants.BACK_SLASH_CHAR;
import static io.microsphere.constants.Constants.CLASS;
import static io.microsphere.constants.Constants.CLASSPATH_PROTOCOL;
import static io.microsphere.constants.Constants.CLASS_EXTENSION;
import static io.microsphere.constants.Constants.COLON;
import static io.microsphere.constants.Constants.COLON_CHAR;
import static io.microsphere.constants.Constants.COMMA;
import static io.microsphere.constants.Constants.COMMA_CHAR;
import static io.microsphere.constants.Constants.DOLLAR;
import static io.microsphere.constants.Constants.DOLLAR_CHAR;
import static io.microsphere.constants.Constants.DOT;
import static io.microsphere.constants.Constants.DOT_CHAR;
import static io.microsphere.constants.Constants.DOUBLE_QUOTATION;
import static io.microsphere.constants.Constants.DOUBLE_QUOTATION_CHAR;
import static io.microsphere.constants.Constants.DOUBLE_SLASH;
import static io.microsphere.constants.Constants.EAR;
import static io.microsphere.constants.Constants.EAR_EXTENSION;
import static io.microsphere.constants.Constants.EAR_PROTOCOL;
import static io.microsphere.constants.Constants.ENABLED_PROPERTY_NAME;
import static io.microsphere.constants.Constants.EQUAL;
import static io.microsphere.constants.Constants.EQUAL_CHAR;
import static io.microsphere.constants.Constants.EXCLAMATION;
import static io.microsphere.constants.Constants.EXCLAMATION_CHAR;
import static io.microsphere.constants.Constants.FILE_PROTOCOL;
import static io.microsphere.constants.Constants.FILE_SEPARATOR;
import static io.microsphere.constants.Constants.FTP_PROTOCOL;
import static io.microsphere.constants.Constants.GREATER_THAN;
import static io.microsphere.constants.Constants.GREATER_THAN_CHAR;
import static io.microsphere.constants.Constants.GREATER_THAN_OR_EQUAL_TO;
import static io.microsphere.constants.Constants.HTTPS_PROTOCOL;
import static io.microsphere.constants.Constants.HTTP_PROTOCOL;
import static io.microsphere.constants.Constants.HYPHEN;
import static io.microsphere.constants.Constants.HYPHEN_CHAR;
import static io.microsphere.constants.Constants.JAR;
import static io.microsphere.constants.Constants.JAR_EXTENSION;
import static io.microsphere.constants.Constants.JAR_PROTOCOL;
import static io.microsphere.constants.Constants.LEFT_PARENTHESIS;
import static io.microsphere.constants.Constants.LEFT_PARENTHESIS_CHAR;
import static io.microsphere.constants.Constants.LESS_THAN;
import static io.microsphere.constants.Constants.LESS_THAN_CHAR;
import static io.microsphere.constants.Constants.LESS_THAN_OR_EQUAL_TO;
import static io.microsphere.constants.Constants.LINE_SEPARATOR;
import static io.microsphere.constants.Constants.MICROSPHERE_PROPERTY_NAME_PREFIX;
import static io.microsphere.constants.Constants.PATH_SEPARATOR;
import static io.microsphere.constants.Constants.QUERY_STRING;
import static io.microsphere.constants.Constants.QUERY_STRING_CHAR;
import static io.microsphere.constants.Constants.QUESTION_MARK;
import static io.microsphere.constants.Constants.QUESTION_MARK_CHAR;
import static io.microsphere.constants.Constants.RIGHT_PARENTHESIS;
import static io.microsphere.constants.Constants.RIGHT_PARENTHESIS_CHAR;
import static io.microsphere.constants.Constants.SEMICOLON;
import static io.microsphere.constants.Constants.SEMICOLON_CHAR;
import static io.microsphere.constants.Constants.SHARP;
import static io.microsphere.constants.Constants.SHARP_CHAR;
import static io.microsphere.constants.Constants.SLASH;
import static io.microsphere.constants.Constants.SLASH_CHAR;
import static io.microsphere.constants.Constants.SPACE;
import static io.microsphere.constants.Constants.SPACE_CHAR;
import static io.microsphere.constants.Constants.UNDER_SCORE;
import static io.microsphere.constants.Constants.UNDER_SCORE_CHAR;
import static io.microsphere.constants.Constants.WAR;
import static io.microsphere.constants.Constants.WAR_EXTENSION;
import static io.microsphere.constants.Constants.WAR_PROTOCOL;
import static io.microsphere.constants.Constants.WILDCARD;
import static io.microsphere.constants.Constants.WILDCARD_CHAR;
import static io.microsphere.constants.Constants.ZIP;
import static io.microsphere.constants.Constants.ZIP_EXTENSION;
import static io.microsphere.constants.Constants.ZIP_PROTOCOL;
import static io.microsphere.constants.Constants.PIPE;
import static io.microsphere.constants.Constants.VERTICAL_BAR;
import static io.microsphere.constants.Constants.DOUBLE_QUOTE;
import static io.microsphere.constants.Constants.QUOTE;
import static io.microsphere.constants.Constants.SINGLE_QUOTATION;
import static io.microsphere.constants.Constants.DOUBLE_QUOTE_CHAR;
import static io.microsphere.constants.Constants.QUOTE_CHAR;
import static io.microsphere.constants.Constants.SINGLE_QUOTATION_CHAR;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link Constants} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ConstantsTest {

    @Test
    public void test() {
        assertEquals("zip", ZIP);
        assertEquals("jar", JAR);
        assertEquals("war", WAR);
        assertEquals("ear", EAR);
        assertEquals("class", CLASS);

        assertEquals(".zip", ZIP_EXTENSION);
        assertEquals(".jar", JAR_EXTENSION);
        assertEquals(".war", WAR_EXTENSION);
        assertEquals(".ear", EAR_EXTENSION);
        assertEquals(".class", CLASS_EXTENSION);

        assertEquals('/', SLASH_CHAR);
        assertEquals('\\', BACK_SLASH_CHAR);
        assertEquals("/", SLASH);
        assertEquals("//", DOUBLE_SLASH);
        assertEquals("\\", BACK_SLASH);

        assertEquals("enabled", ENABLED_PROPERTY_NAME);
        assertEquals("microsphere.", MICROSPHERE_PROPERTY_NAME_PREFIX);

        assertEquals("file", FILE_PROTOCOL);
        assertEquals("http", HTTP_PROTOCOL);
        assertEquals("https", HTTPS_PROTOCOL);
        assertEquals("ftp", FTP_PROTOCOL);
        assertEquals("zip", ZIP_PROTOCOL);
        assertEquals("jar", JAR_PROTOCOL);
        assertEquals("war", WAR_PROTOCOL);
        assertEquals("ear", EAR_PROTOCOL);
        assertEquals("classpath", CLASSPATH_PROTOCOL);

        assertEquals("!/", ARCHIVE_ENTRY_SEPARATOR);
        assertEquals(File.separator, FILE_SEPARATOR);
        assertEquals(File.pathSeparator, PATH_SEPARATOR);
        assertEquals(System.lineSeparator(), LINE_SEPARATOR);

        assertEquals(',', COMMA_CHAR);
        assertEquals(' ', SPACE_CHAR);
        assertEquals('!', EXCLAMATION_CHAR);
        assertEquals('\'', QUOTE_CHAR);
        assertEquals('"', DOUBLE_QUOTE_CHAR);
        assertEquals('\'', SINGLE_QUOTATION_CHAR);
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
        assertEquals("|", VERTICAL_BAR);
        assertEquals("|", PIPE);

        assertEquals(",", COMMA);
        assertEquals(" ", SPACE);
        assertEquals("!", EXCLAMATION);
        assertEquals("'", QUOTE);
        assertEquals("\"", DOUBLE_QUOTE);
        assertEquals("'", SINGLE_QUOTATION);
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
        assertEquals("|", VERTICAL_BAR);
        assertEquals("|", PIPE);
    }
}
