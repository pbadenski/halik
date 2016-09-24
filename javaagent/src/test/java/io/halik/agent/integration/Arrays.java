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
package io.halik.agent.integration;

import io.halik.agent.SingleAgentLoader;
import io.halik.agent.capture.FlowFacade;
import io.halik.agent.output.JSONOutput;
import org.json.JSONArray;
import org.junit.Test;
import test.halik.*;

import static com.google.common.collect.Lists.newArrayList;
import static io.halik.agent.integration.asserts.FlowAssert.assertThat;

public class Arrays {
    @Test
    public void shouldCaptureArrayOfBooleansAssignment() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();
        new ArrayOfBooleans().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow).atLine(10).hasReference(ArrayOfBooleans.FIRST_REFERENCE, newArrayList(true, false));
        assertThat(flow).atLine(12).hasReference(ArrayOfBooleans.FIRST_REFERENCE, newArrayList(false, false));
        assertThat(flow).atLine(13).hasReference(ArrayOfBooleans.SECOND_REFERENCE, newArrayList(false, false, false));
        // TODO: assertThat(flow).atLine(9).hasVariable("arrayOfBooleans", null);
    }

    @Test
    public void shouldCaptureArrayOfDoublesAssignment() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();
        new ArrayOfDoubles().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow).atLine(10).hasReference(ArrayOfDoubles.FIRST_REFERENCE, newArrayList(1.0, 2.0));
        assertThat(flow).atLine(12).hasReference(ArrayOfDoubles.FIRST_REFERENCE, newArrayList(3.0, 2.0));
        assertThat(flow).atLine(13).hasReference(ArrayOfDoubles.SECOND_REFERENCE, newArrayList(4.0, 5.0, 6.0));
        // TODO: assertThat(flow).atLine(9).hasVariable("arrayOfDoubles", null);
    }

    @Test
    public void shouldCaptureArrayOfIntsAssignment() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();
        new ArrayOfInts().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow).atLine(11).hasReference(ArrayOfInts.FIRST_REFERENCE, newArrayList(1, 2));
        assertThat(flow).atLine(13).hasReference(ArrayOfInts.FIRST_REFERENCE, newArrayList(3, 2));
        assertThat(flow).atLine(14).hasReference(ArrayOfInts.SECOND_REFERENCE, newArrayList(4, 5, 6));
        // TODO: assertThat(flow).atLine(9).hasVariable("arrayOfDoubles", null);
    }


    @Test
    public void shouldCaptureArrayOfCharsAssignment() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();
        new ArrayOfChars().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow).atLine(10).hasReference(ArrayOfChars.FIRST_REFERENCE, newArrayList(97, 98));
        assertThat(flow).atLine(12).hasReference(ArrayOfChars.FIRST_REFERENCE, newArrayList(99, 98));
        assertThat(flow).atLine(13).hasReference(ArrayOfChars.SECOND_REFERENCE, newArrayList(100, 101, 102));
        // TODO: assertThat(flow).atLine(9).hasVariable("arrayOfDoubles", null);
    }

    @Test
    public void shouldCaptureArrayOfFloatsAssignment() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();
        new ArrayOfFloats().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow).atLine(10).hasReference(ArrayOfFloats.FIRST_REFERENCE, newArrayList(1.0, 2.0));
        assertThat(flow).atLine(12).hasReference(ArrayOfFloats.FIRST_REFERENCE, newArrayList(3.0, 2.0));
        assertThat(flow).atLine(13).hasReference(ArrayOfFloats.SECOND_REFERENCE, newArrayList(4.0, 5.0, 6.0));
        // TODO: assertThat(flow).atLine(9).hasVariable("arrayOfDoubles", null);
    }

    @Test
    public void shouldCaptureArrayOfBytesAssignment() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();
        new ArrayOfBytes().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow).atLine(10).hasReference(ArrayOfBytes.FIRST_REFERENCE, newArrayList(1, 2));
        assertThat(flow).atLine(12).hasReference(ArrayOfBytes.FIRST_REFERENCE, newArrayList(3, 2));
        assertThat(flow).atLine(13).hasReference(ArrayOfBytes.SECOND_REFERENCE, newArrayList(4, 5, 6));
        // TODO: assertThat(flow).atLine(9).hasVariable("arrayOfDoubles", null);
    }

    @Test
    public void shouldCaptureArrayOfLongsAssignment() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();
        new ArrayOfLongs().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow).atLine(10).hasReference(ArrayOfLongs.FIRST_REFERENCE, newArrayList(1, 2));
        assertThat(flow).atLine(12).hasReference(ArrayOfLongs.FIRST_REFERENCE, newArrayList(3, 2));
        assertThat(flow).atLine(13).hasReference(ArrayOfLongs.SECOND_REFERENCE, newArrayList(4, 5, 6));
        // TODO: assertThat(flow).atLine(9).hasVariable("arrayOfDoubles", null);
    }

    @Test
    public void shouldCaptureOneArrayDeclarationAndOneArrayAssignment() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();
        new ArrayOfInts().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow).atLine(11).hasVariable("array", newArrayList(1, 2));
        assertThat(flow).atLine(11).hasReference(ArrayOfInts.FIRST_REFERENCE, newArrayList(1, 2));
    }

    @Test
    public void shouldCaptureArgumentOfMethodAsVariableDeclaration() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();
        new ArrayOfInts().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow).atLine(20).hasVariable("arrayArgument", newArrayList(4, 5, 6));
    }

}
