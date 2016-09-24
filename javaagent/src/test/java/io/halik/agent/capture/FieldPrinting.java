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
import io.halik.agent.capture.track.fields.Field;
import io.halik.agent.integration.asserts.FlowAssert;
import io.halik.agent.output.JSONOutput;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.msgpack.packer.BufferPacker;
import org.msgpack.util.json.JSON;
import test.halik.NullAssignment;

import java.io.IOException;

import static java.lang.System.identityHashCode;
import static org.assertj.core.api.Assertions.assertThat;

public class FieldPrinting {
    private BufferPacker packer;

    @Before
    public void setUp() {
        packer = new JSON().createBufferPacker();
    }

    @Test
    public void shouldPrintIntegerFieldValue() throws IOException {
        Field field = new Field(new Object(), "field", 0);
        field.pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("o")).isNotNull();
        assertThat(json.get("n")).isEqualTo("field");
        assertThat(json.get("v")).isEqualTo(0);
    }

    @Test
    public void shouldPrintStringFieldValue() throws IOException {
        Field field = new Field(new Object(), "field", "string");
        field.pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("o")).isNotNull();
        assertThat(json.get("n")).isEqualTo("field");
        assertThat(json.get("v")).isEqualTo("string");
    }

    @Test
    public void shouldPrintBooleanFieldValue() throws IOException {
        Field field = new Field(new Object(), "field", true);
        field.pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("o")).isNotNull();
        assertThat(json.get("n")).isEqualTo("field");
        assertThat(json.get("v")).isEqualTo(true);
    }

    @Test
    public void shouldPrintObjectFieldValue() throws IOException {
        Field field = new Field(new Object(), "field", new Object());
        field.pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("o")).isNotNull();
        assertThat(json.get("n")).isEqualTo("field");
        assertThat(json.get("id")).isNotNull();
    }

    @Test
    public void shouldPrintArrayOfStringsFieldValue() throws IOException {
        Field field = new Field(new Object(), "field", new String[]{"foo", "bar"});
        field.pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("o")).isNotNull();
        assertThat(json.get("n")).isEqualTo("field");
        assertThat(json.getJSONArray("v").get(0)).isEqualTo("foo");
        assertThat(json.getJSONArray("v").get(1)).isEqualTo("bar");
    }

    @Test
    public void shouldPrintArrayOfObjectsFieldValue() throws IOException {
        Field field = new Field(new Object(), "field", new Object[]{new Object(), new Object()});
        field.pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("o")).isNotNull();
        assertThat(json.get("n")).isEqualTo("field");
        assertThat(json.getJSONArray("v").get(0)).isNotNull();
        assertThat(json.getJSONArray("v").get(1)).isNotNull();
    }

    @Test
    public void shouldPrintArrayOfIntegersFieldValue() throws IOException {
        Field field = new Field(new Object(), "field", new int[]{10, 20});
        field.pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("o")).isNotNull();
        assertThat(json.get("n")).isEqualTo("field");
        assertThat(json.getJSONArray("v").get(0)).isEqualTo(10);
        assertThat(json.getJSONArray("v").get(1)).isEqualTo(20);
    }

    @Test
    public void shouldPrintArrayOfBooleansFieldValue() throws IOException {
        Field field = new Field(new Object(), "field", new boolean[]{true, false});
        field.pack(packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));

        assertThat(json.get("o")).isNotNull();
        assertThat(json.get("n")).isEqualTo("field");
        assertThat(json.getJSONArray("v").get(0)).isEqualTo(true);
        assertThat(json.getJSONArray("v").get(1)).isEqualTo(false);
    }

    @Test
    public void shouldCaptureFieldStateWhenAssignedNull() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();

        NullAssignment parentObject = new NullAssignment();
        parentObject.main();

        JSONArray flow = jsonOutput.toJSON();
        FlowAssert.assertThat(flow).atLine(10)
                .hasField("nullStringField", null, identityHashCode(parentObject));
    }
}