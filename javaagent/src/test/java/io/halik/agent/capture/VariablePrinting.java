/*
 *   Copyright (C) 2016 Pawel Badenski
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.halik.agent.capture;

import io.halik.agent.SingleAgentLoader;
import io.halik.agent.capture.track.variables.SimpleVariableDefinition;
import io.halik.agent.capture.track.variables.Variable;
import io.halik.agent.integration.asserts.FlowAssert;
import io.halik.agent.output.JSONOutput;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.msgpack.packer.BufferPacker;
import org.msgpack.util.json.JSON;
import org.objectweb.asm.Type;
import test.halik.MethodParameters;
import test.halik.NullAssignment;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class VariablePrinting {
    private BufferPacker packer;

    @Before
    public void setUp() {
        packer = new JSON().createBufferPacker();
    }

    @Test
    public void shouldPrintIntegerValue() throws Exception {
        final SimpleVariableDefinition integer = new SimpleVariableDefinition("integer", Type.INT_TYPE, 0, 0);
        new Variable(integer, 0).pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));
        
        assertThat(json.get("n")).isEqualTo("integer");
        assertThat(json.get("v")).isEqualTo(0);
    }

    @Test
    public void shouldPrintIntegerMinValue() throws Exception {
        final SimpleVariableDefinition integer = new SimpleVariableDefinition("integer", Type.INT_TYPE, 0, 0);
        new Variable(integer, Integer.MIN_VALUE).pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("n")).isEqualTo("integer");
        assertThat(json.get("v")).isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    public void shouldPrintIntegerMaxValue() throws Exception {
        final SimpleVariableDefinition integer = new SimpleVariableDefinition("integer", Type.INT_TYPE, 0, 0);
        new Variable(integer, Integer.MAX_VALUE).pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("n")).isEqualTo("integer");
        assertThat(json.get("v")).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    public void shouldPrintByteValue() throws IOException {
        final SimpleVariableDefinition aByte = new SimpleVariableDefinition("byte", Type.BYTE_TYPE, 0, 0);
        new Variable(aByte, 0).pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("n")).isEqualTo("byte");
        assertThat(json.get("v")).isEqualTo(0);
    }

    @Test
    public void shouldPrintShortValue() throws IOException {
        final SimpleVariableDefinition aShort = new SimpleVariableDefinition("short", Type.SHORT_TYPE, 0, 0);
        new Variable(aShort, 0).pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("n")).isEqualTo("short");
        assertThat(json.get("v")).isEqualTo(0);
    }

    @Test
    public void shouldPrintStringValue() throws IOException {
        final SimpleVariableDefinition string = new SimpleVariableDefinition("string", Type.getType("Ljava/lang/String;"), 0, 0);
        new Variable(string, "v").pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("n")).isEqualTo("string");
        assertThat(json.get("v")).isEqualTo("v");
    }

    @Test
    public void shouldPrintBooleanValue() throws IOException {
        final SimpleVariableDefinition b = new SimpleVariableDefinition("b", Type.BOOLEAN_TYPE, 0, 0);
        new Variable(b, 1).pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("n")).isEqualTo("b");
        assertThat(json.get("v")).isEqualTo(true);
    }

    @Test
    public void shouldPrintDoubleValue() throws IOException {
        final SimpleVariableDefinition d = new SimpleVariableDefinition("d", Type.DOUBLE_TYPE, 0, 0);
        new Variable(d, 0.0).pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("n")).isEqualTo("d");
        assertThat(json.get("v")).isEqualTo(0.0);
    }

    @Test
    public void shouldPrintDoubleMinusInfinityValue() throws IOException {
        final SimpleVariableDefinition d = new SimpleVariableDefinition("d", Type.DOUBLE_TYPE, 0, 0);
        new Variable(d, Double.NEGATIVE_INFINITY).pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("n")).isEqualTo("d");
        assertThat(json.get("v")).isEqualTo("-Infinity");
    }

    @Test
    public void shouldPrintDoubleInfinityValue() throws IOException {
        final SimpleVariableDefinition d = new SimpleVariableDefinition("d", Type.DOUBLE_TYPE, 0, 0);
        new Variable(d, Double.POSITIVE_INFINITY).pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("n")).isEqualTo("d");
        assertThat(json.get("v")).isEqualTo("Infinity");
    }

    @Test
    public void shouldPrintDoubleNaNValue() throws IOException {
        final SimpleVariableDefinition d = new SimpleVariableDefinition("d", Type.DOUBLE_TYPE, 0, 0);
        new Variable(d, Double.NaN).pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("n")).isEqualTo("d");
        assertThat(json.get("v")).isEqualTo("NaN");
    }

    @Test
    public void shouldPrintFloatMinusInfinityValue() throws IOException {
        final SimpleVariableDefinition d = new SimpleVariableDefinition("f", Type.FLOAT_TYPE, 0, 0);
        new Variable(d, Float.NEGATIVE_INFINITY).pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("n")).isEqualTo("f");
        assertThat(json.get("v")).isEqualTo("-Infinity");
    }

    @Test
    public void shouldPrintFloatInfinityValue() throws IOException {
        final SimpleVariableDefinition f = new SimpleVariableDefinition("f", Type.FLOAT_TYPE, 0, 0);
        new Variable(f, Float.POSITIVE_INFINITY).pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("n")).isEqualTo("f");
        assertThat(json.get("v")).isEqualTo("Infinity");
    }

    @Test
    public void shouldPrintFloatNaNValue() throws IOException {
        final SimpleVariableDefinition f = new SimpleVariableDefinition("f", Type.FLOAT_TYPE, 0, 0);
        new Variable(f, Float.NaN).pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("n")).isEqualTo("f");
        assertThat(json.get("v")).isEqualTo("NaN");
    }


    @Test
    public void shouldPrintObjectValue() throws IOException {
        final SimpleVariableDefinition object = new SimpleVariableDefinition("object", Type.getType("Ljava/lang/Object;"), 0, 0);
        final Object value = new Object();
        new Variable(object, value).pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("n")).isEqualTo("object");
        assertThat(json.get("id")).isNotNull();
    }

    @Test
    public void shouldPrintArrayOfStringsValue() throws IOException {
        final SimpleVariableDefinition arrayOfStrings = new SimpleVariableDefinition("arrayOfStrings", Type.getType("[Ljava/lang/String;"), 0, 0);
        final String[] value = new String[]{"foo", "bar"};
        new Variable(arrayOfStrings, value).pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("n")).isEqualTo("arrayOfStrings");
        assertThat(json.getJSONArray("v").get(0)).isEqualTo("foo");
        assertThat(json.getJSONArray("v").get(1)).isEqualTo("bar");
    }

    @Test
    public void shouldPrintArrayOfObjectsValue() throws IOException {
        final SimpleVariableDefinition arrayOfObjects = new SimpleVariableDefinition("arrayOfObjects", Type.getType("[Ljava/lang/String;"), 0, 0);
        final Object[] value = new Object[]{new Object(), new Object()};
        new Variable(arrayOfObjects, value).pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("n")).isEqualTo("arrayOfObjects");
        assertThat(json.getJSONArray("v").get(0)).isNotNull();
        assertThat(json.getJSONArray("v").get(1)).isNotNull();
    }

    @Test
    public void shouldPrintArrayOfIntegersValue() throws IOException {
        final SimpleVariableDefinition arrayOfIntegers = new SimpleVariableDefinition("arrayOfIntegers", Type.getType("[I"), 0, 0);
        final int[] value = new int[]{10, 20};
        new Variable(arrayOfIntegers, value).pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("n")).isEqualTo("arrayOfIntegers");
        assertThat(json.getJSONArray("v").get(0)).isEqualTo(10);
        assertThat(json.getJSONArray("v").get(1)).isEqualTo(20);
    }

    @Test
    public void shouldPrintArrayOfBoxedPrimitiveValue() throws IOException {
        final SimpleVariableDefinition arrayOfIntegers = new SimpleVariableDefinition("arrayOfIntegers", Type.getType("[Ljava/langInteger;"), 0, 0);
        final Integer[] value = new Integer[]{10, 20};
        new Variable(arrayOfIntegers, value).pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("n")).isEqualTo("arrayOfIntegers");
        assertThat(json.getJSONArray("v").getJSONObject(0).get("v")).isEqualTo(10);
        assertThat(json.getJSONArray("v").getJSONObject(1).get("v")).isEqualTo(20);
    }

    @Test
    public void shouldPrintArrayOfBytesValue() throws IOException {
        final SimpleVariableDefinition arrayOfBytes = new SimpleVariableDefinition("arrayOfBytes", Type.getType("[B"), 0, 0);
        final byte[] value = new byte[]{1, 2};
        new Variable(arrayOfBytes, value).pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("n")).isEqualTo("arrayOfBytes");
        assertThat(json.getJSONArray("v").get(0)).isEqualTo(1);
        assertThat(json.getJSONArray("v").get(1)).isEqualTo(2);
    }

    @Test
    public void shouldPrintArrayOfCharactersValue() throws IOException {
        final SimpleVariableDefinition arrayOfCharacters = new SimpleVariableDefinition("arrayOfCharacters", Type.getType("[C"), 0, 0);
        final char[] value = new char[]{'a', 'b'};
        new Variable(arrayOfCharacters, value).pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("n")).isEqualTo("arrayOfCharacters");
        assertThat(json.getJSONArray("v").get(0)).isEqualTo(97);
        assertThat(json.getJSONArray("v").get(1)).isEqualTo(98);
    }

    @Test
    public void shouldPrintArrayOfBooleansValue() throws IOException {
        final SimpleVariableDefinition arrayOfBooleans = new SimpleVariableDefinition("arrayOfBooleans", Type.getType("[Z"), 0, 0);
        final boolean[] value = new boolean[]{true, false};
        new Variable(arrayOfBooleans, value).pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("n")).isEqualTo("arrayOfBooleans");
        assertThat(json.getJSONArray("v").get(0)).isEqualTo(true);
        assertThat(json.getJSONArray("v").get(1)).isEqualTo(false);
    }

    @Test
    public void shouldCaptureVariableStateWhenAssignedNull() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();

        new NullAssignment().main();

        JSONArray flow = jsonOutput.toJSON();
        FlowAssert.assertThat(flow).atLine(9)
                .hasVariable("nullString", null);
    }

    @Test
    public void shouldCaptureParameterWhenObject() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();

        new MethodParameters().main();

        JSONArray flow = jsonOutput.toJSON();
        FlowAssert.assertThat(flow).atLine(19)
                .hasVariable("objectParameter");
    }

    @Test
    public void shouldCaptureParameterWhenString() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();

        new MethodParameters().main();

        JSONArray flow = jsonOutput.toJSON();
        FlowAssert.assertThat(flow).atLine(16)
                .hasVariable("stringParameter");
    }

    @Test
    public void shouldCaptureParameterWhenList() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();

        new MethodParameters().main();

        JSONArray flow = jsonOutput.toJSON();
        FlowAssert.assertThat(flow).atLine(22)
                .hasVariable("listParameter");
    }
}