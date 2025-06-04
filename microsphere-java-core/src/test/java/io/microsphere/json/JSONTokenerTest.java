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

package io.microsphere.json;


import org.junit.jupiter.api.Test;

import static io.microsphere.json.JSONObject.NULL;
import static io.microsphere.json.JSONTokener.dehexchar;
import static io.microsphere.util.ClassLoaderUtils.getResourceAsString;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link JSONTokener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see JSONTokener
 * @since 1.0.0
 */
class JSONTokenerTest {

    @Test
    void testConstructor() {
        JSONTokener jsonTokener = new JSONTokener("\ufeffHello");
        assertNotNull(jsonTokener);
    }

    @Test
    void testConstrcutorWithNull() {
        new JSONTokener(null);
    }

    @Test
    void testNextValueOnSyntaxError() throws Throwable {
        JSONTokener jsonTokener = new JSONTokener("");
        assertThrows(JSONException.class, jsonTokener::nextValue);
    }

    @Test
    void testNextValueOnReadObject() throws Throwable {
        JSONTokener jsonTokener = getJSONTokener("test/json/data.json");
        JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
        assertNotNull(jsonObject);
        assertEquals("Mercy", jsonObject.getString("name"));
    }

    @Test
    void testNextValueOnReadArray() throws Throwable {
        JSONTokener jsonTokener = getJSONTokener("test/json/array.json");
        JSONArray jsonArray = (JSONArray) jsonTokener.nextValue();
        assertNotNull(jsonArray);
        assertEquals(3, jsonArray.length());
    }

    @Test
    void testNextValueOnReadString() throws Throwable {
        JSONTokener jsonTokener = new JSONTokener("\"Hello\"");
        assertEquals("Hello", jsonTokener.nextValue());

        jsonTokener = new JSONTokener("'hello'");
        assertEquals("hello", jsonTokener.nextValue());
    }

    @Test
    void testNextValueOnReadLiteral() throws Throwable {
        JSONTokener jsonTokener = new JSONTokener("Hello");
        assertEquals("Hello", jsonTokener.nextValue());
    }

    @Test
    void testReadObject() throws Throwable {
        JSONTokener jsonTokener = new JSONTokener("}");
        JSONObject jsonObject = jsonTokener.readObject();
        assertEquals(0, jsonObject.length());

        jsonTokener = new JSONTokener("\"name\" : \"Mercy\" }");
        jsonObject = jsonTokener.readObject();
        assertEquals("Mercy", jsonObject.getString("name"));

        jsonTokener = new JSONTokener("\"name\" : \"Mercy\" , \"age\" = 19 }");
        jsonObject = jsonTokener.readObject();
        assertEquals("Mercy", jsonObject.getString("name"));
        assertEquals(19, jsonObject.getInt("age"));

        jsonTokener = new JSONTokener("\"name\" : \"Mercy\" , \"age\" = 19 ; \"gender\" => \"male\" }");
        jsonObject = jsonTokener.readObject();
        assertEquals("Mercy", jsonObject.getString("name"));
        assertEquals(19, jsonObject.getInt("age"));
    }

    @Test
    void testReadObjectOnSyntaxError() {
        assertThrows(JSONException.class, () -> new JSONTokener("").readObject());
        assertThrows(JSONException.class, () -> new JSONTokener("null").readObject());
        assertThrows(JSONException.class, () -> new JSONTokener("{ \"name\" : \"Mercy\" }").readObject());
        assertThrows(JSONException.class, () -> new JSONTokener("\"name\"").readObject());
        assertThrows(JSONException.class, () -> new JSONTokener("\"name\" : \"Mercy\"").readObject());
    }

    @Test
    void testReadArray() throws Throwable {
        JSONTokener jsonTokener = new JSONTokener("[] ]");
        JSONArray jsonArray = jsonTokener.readArray();
        assertEquals(1, jsonArray.length());

        jsonTokener = new JSONTokener(" , ]");
        jsonArray = jsonTokener.readArray();
        assertEquals(2, jsonArray.length());

        jsonTokener = new JSONTokener(" \"name\" , \"age\" ]");
        jsonArray = jsonTokener.readArray();
        assertEquals(2, jsonArray.length());
        assertEquals("name", jsonArray.getString(0));
        assertEquals("age", jsonArray.getString(1));


        jsonTokener = new JSONTokener(" \"name\" ; \"age\" ; \"gender\" ]");
        jsonArray = jsonTokener.readArray();
        assertEquals(3, jsonArray.length());
        assertEquals("name", jsonArray.getString(0));
        assertEquals("age", jsonArray.getString(1));
        assertEquals("gender", jsonArray.getString(2));
    }

    @Test
    void testReadArrayOnSyntaxError() {
        assertThrows(JSONException.class, () -> new JSONTokener("").readArray());
        assertThrows(JSONException.class, () -> new JSONTokener("[").readArray());
        assertThrows(JSONException.class, () -> new JSONTokener("\"name\"").readArray());

    }

    @Test
    void testNextCleanInternal() throws Throwable {
        JSONTokener jsonTokener = getJSONTokener("test/json/clean.txt");
        int c = 0;
        do {
            c = jsonTokener.nextCleanInternal();
        } while (c != 'a');
    }

    @Test
    void testNextCleanInternalOnSyntaxError() {
        JSONTokener jsonTokener = new JSONTokener("/*");
        assertThrows(JSONException.class, () -> jsonTokener.nextCleanInternal());
    }

    @Test
    void testNextCleanInternalOn() throws JSONException {
        JSONTokener jsonTokener = new JSONTokener("\t \n\r");
        assertEquals(-1, jsonTokener.nextCleanInternal());
    }

    @Test
    void testNextCleanInternalOnSlash() throws JSONException {
        JSONTokener jsonTokener = new JSONTokener("/");
        assertEquals('/', jsonTokener.nextCleanInternal());

        jsonTokener = new JSONTokener("/a");
        assertEquals('/', jsonTokener.nextCleanInternal());
    }

    JSONTokener getJSONTokener(String resourceName) throws Throwable {
        String json = getResourceAsString(resourceName);
        return new JSONTokener(json);
    }

    @Test
    void testSkipToEndOfLine() {
        String input = "// This is a comment\r\nMore text";
        JSONTokener t = new JSONTokener(input);
        t.skipToEndOfLine();

        new JSONTokener("").skipToEndOfLine();
    }

    @Test
    void testNextString() {
        JSONTokener jsonTokener = new JSONTokener("");
    }


    @Test
    void testNextStringOnSyntaxError() {
        assertThrows(JSONException.class, () -> new JSONTokener("").nextString('\''));
        assertThrows(JSONException.class, () -> new JSONTokener("\\").nextString('\''));
    }

    @Test
    void testReadEscapeCharacter() throws JSONException {
        JSONTokener t = new JSONTokener("ntbnrf\'\"\\u0041");
        assertEquals('\n', t.readEscapeCharacter());
        assertEquals('\t', t.readEscapeCharacter());
        assertEquals('\b', t.readEscapeCharacter());
        assertEquals('\n', t.readEscapeCharacter());
        assertEquals('\r', t.readEscapeCharacter());
        assertEquals('\f', t.readEscapeCharacter());
        assertEquals('\'', t.readEscapeCharacter());
        assertEquals('"', t.readEscapeCharacter());
        assertEquals('\\', t.readEscapeCharacter());
        assertEquals('A', t.readEscapeCharacter());
    }

    @Test
    void testReadEscapeCharacterOnSyntaxError() throws JSONException {
        JSONTokener t = new JSONTokener("u123");
        assertThrows(JSONException.class, () -> t.readEscapeCharacter());
    }

    @Test
    void testReadLiteral() throws JSONException {
        assertEquals(NULL, new JSONTokener("null").readLiteral());
        assertEquals(TRUE, new JSONTokener("true").readLiteral());
        assertEquals(TRUE, new JSONTokener("TRUE").readLiteral());
        assertEquals(FALSE, new JSONTokener("false").readLiteral());
        assertEquals(FALSE, new JSONTokener("FALSE").readLiteral());
        assertEquals(0xAF, new JSONTokener("0xAF").readLiteral());
        assertEquals(0xAF, new JSONTokener("0XAF").readLiteral());
        assertEquals(01, new JSONTokener("01").readLiteral());
        assertEquals(123, new JSONTokener("123").readLiteral());
        assertEquals(0, new JSONTokener("0").readLiteral());
        assertEquals(21474836470L, new JSONTokener(Integer.MAX_VALUE + "0").readLiteral());
        assertEquals(-21474836470L, new JSONTokener("-" + Integer.MAX_VALUE + "0").readLiteral());
        assertEquals(123.45, new JSONTokener("123.45").readLiteral());
    }

    @Test
    void testReadLiteralOnSyntaxError() {
        assertThrows(JSONException.class, () -> new JSONTokener("").readLiteral());
    }

    @Test
    void testReadLiteralOnNumberFormatException() throws JSONException {
        assertEquals("a", new JSONTokener("a").readLiteral());
        assertEquals("0.1@", new JSONTokener("0.1@").readLiteral());
    }

    @Test
    void testNextToInternal() {
        assertEquals("Hello", new JSONTokener("Hello\r").nextToInternal("#"));
        assertEquals("Hello", new JSONTokener("Hello\n").nextToInternal("#"));
        assertEquals("Hello", new JSONTokener("Hello#").nextToInternal("#"));
        assertEquals("Hello", new JSONTokener("Hello").nextToInternal("#"));
    }

    @Test
    void testToString() {
        JSONTokener jsonTokener = new JSONTokener("Hello");
        assertNotNull(jsonTokener.toString());
    }

    @Test
    void testMore() {
        JSONTokener jsonTokener = new JSONTokener("Hello");
        assertTrue(jsonTokener.more());
        jsonTokener.next();
        assertTrue(jsonTokener.more());
        jsonTokener.next();
        assertTrue(jsonTokener.more());
        jsonTokener.next();
        assertTrue(jsonTokener.more());
        jsonTokener.next();
        assertTrue(jsonTokener.more());
        jsonTokener.next();
        assertFalse(jsonTokener.more());
    }

    @Test
    void testNext() {
        JSONTokener jsonTokener = new JSONTokener("Hello");
        assertEquals('H', jsonTokener.next());
        assertEquals('e', jsonTokener.next());
        assertEquals('l', jsonTokener.next());
        assertEquals('l', jsonTokener.next());
        assertEquals('o', jsonTokener.next());
        assertEquals('\0', jsonTokener.next());
    }

    @Test
    void testNextWithChar() throws JSONException {
        JSONTokener jsonTokener = new JSONTokener("Hello");
        assertEquals('H', jsonTokener.next('H'));
        assertEquals('e', jsonTokener.next('e'));
        assertEquals('l', jsonTokener.next('l'));
        assertEquals('l', jsonTokener.next('l'));
        assertEquals('o', jsonTokener.next('o'));
    }

    @Test
    void testNextOnSyntaxError() {
        JSONTokener jsonTokener = new JSONTokener("Hello");
        assertThrows(JSONException.class, () -> jsonTokener.next('X'));
    }

    @Test
    void testNextClean() throws JSONException {
        JSONTokener jsonTokener = new JSONTokener("Hello");
        assertEquals('H', jsonTokener.nextClean());
        assertEquals('e', jsonTokener.nextClean());
        assertEquals('l', jsonTokener.nextClean());
        assertEquals('l', jsonTokener.nextClean());
        assertEquals('o', jsonTokener.nextClean());
        assertEquals('\0', jsonTokener.nextClean());
    }

    @Test
    void testNextWithIndex() throws JSONException {
        JSONTokener jsonTokener = new JSONTokener("Hello");
        String result = jsonTokener.next(0);
        assertEquals("", result);

        result = jsonTokener.next(1);
        assertEquals("H", result);

        result = jsonTokener.next(2);
        assertEquals("el", result);
    }

    @Test
    void testNextWithIndexOnOutOfBounds() {
        JSONTokener jsonTokener = new JSONTokener("Hello");
        assertThrows(JSONException.class, () -> jsonTokener.next(6));
    }

    @Test
    void testNextTo() {
        JSONTokener jsonTokener = new JSONTokener("Hello");
        assertEquals("Hello", jsonTokener.nextTo("#"));
    }

    @Test
    void testNextToOnNullPointerException() {
        JSONTokener jsonTokener = new JSONTokener("Hello");
        assertThrows(NullPointerException.class, () -> jsonTokener.nextTo(null));
    }

    @Test
    void testNextToWithChar() {
        JSONTokener jsonTokener = new JSONTokener("Hello");
        assertEquals("Hello", jsonTokener.nextTo('#'));
    }

    @Test
    void testSkipPast() {
        JSONTokener jsonTokener = new JSONTokener("Hello");
        jsonTokener.skipPast("llo");
        jsonTokener.skipPast("#");
    }

    @Test
    void testSkipTo() {
        JSONTokener jsonTokener = new JSONTokener("Hello");
        assertEquals('H', jsonTokener.skipTo('H'));
        assertEquals('e', jsonTokener.skipTo('e'));
        assertEquals('l', jsonTokener.skipTo('l'));
        assertEquals('l', jsonTokener.skipTo('l'));
        assertEquals('o', jsonTokener.skipTo('o'));
        assertEquals('\0', jsonTokener.skipTo('X'));
    }

    @Test
    void testBack() {
        JSONTokener jsonTokener = new JSONTokener("Hello");
        assertEquals('H', jsonTokener.next());
        jsonTokener.back();
        assertEquals('H', jsonTokener.next());
        assertEquals('e', jsonTokener.next());
        assertEquals('l', jsonTokener.next());
        assertEquals('l', jsonTokener.next());
        assertEquals('o', jsonTokener.next());
        assertEquals('\0', jsonTokener.next());
        jsonTokener.back();
        jsonTokener.back();
        jsonTokener.back();
        jsonTokener.back();
        jsonTokener.back();
        jsonTokener.back();
    }

    @Test
    void testDehexchar() {
        assertEquals(0, dehexchar('0'));
        assertEquals(5, dehexchar('5'));
        assertEquals(9, dehexchar('9'));

        assertEquals(10, dehexchar('A'));
        assertEquals(12, dehexchar('C'));
        assertEquals(15, dehexchar('F'));

        assertEquals(10, dehexchar('a'));
        assertEquals(13, dehexchar('d'));
        assertEquals(15, dehexchar('f'));

        assertEquals(-1, dehexchar('G'));
        assertEquals(-1, dehexchar('z'));
        assertEquals(-1, dehexchar('@'));
        assertEquals(-1, dehexchar(' '));
        assertEquals(-1, dehexchar('#'));
    }

}