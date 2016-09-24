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
import test.halik.BooleanUsage;
import test.halik.ByteUsage;
import test.halik.*;
import test.halik.DoubleUsage;
import test.halik.FloatUsage;
import test.halik.LongUsage;

import static io.halik.agent.integration.asserts.FlowAssert.assertThat;

public class Primitives {
    @Test
    public void shouldCaptureIntegerAssignment() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();

        new IntUsage().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow).atLine(7).hasVariable("i", 0);
        assertThat(flow).atLine(8).hasVariable("i", 1);
        assertThat(flow).atLine(9).hasVariable("i", 2);
        assertThat(flow).atLine(10).hasVariable("i", 42);
        assertThat(flow).atLine(11).hasVariable("i", 43);
        assertThat(flow).atLine(12).hasVariable("i", 44);
    }

    @Test
    public void shouldCaptureLongAssignment() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();
        new LongUsage().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow).atLine(7).hasVariable("l", 0);
        assertThat(flow).atLine(8).hasVariable("l", 1);
        assertThat(flow).atLine(9).hasVariable("l", 2);
        assertThat(flow).atLine(10).hasVariable("l", 42);
        assertThat(flow).atLine(11).hasVariable("l", 43);
        assertThat(flow).atLine(12).hasVariable("l", 44);
    }

    @Test
    public void shouldCaptureCharAssignment() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();
        new CharUsage().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow).atLine(7).hasVariable("ch", 97);
        assertThat(flow).atLine(8).hasVariable("ch", 98);
        assertThat(flow).atLine(9).hasVariable("ch", 195);
        assertThat(flow).atLine(10).hasVariable("ch", 99);
        assertThat(flow).atLine(11).hasVariable("ch", 100);
        assertThat(flow).atLine(12).hasVariable("ch", 101);
    }

    @Test
    public void shouldCaptureByteAssignment() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();
        new ByteUsage().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow).atLine(7).hasVariable("b", 0);
        assertThat(flow).atLine(8).hasVariable("b", 1);
        assertThat(flow).atLine(9).hasVariable("b", 3);
        assertThat(flow).atLine(10).hasVariable("b", 42);
        assertThat(flow).atLine(11).hasVariable("b", 43);
        assertThat(flow).atLine(12).hasVariable("b", 44);
    }

    @Test
    public void shouldCaptureFloatAssignment() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();
        new FloatUsage().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow).atLine(7).hasVariable("f", 0.0);
        assertThat(flow).atLine(8).hasVariable("f", 1.0);
        assertThat(flow).atLine(9).hasVariable("f", 2.01);
        assertThat(flow).atLine(10).hasVariable("f", 42.0);
        assertThat(flow).atLine(11).hasVariable("f", 43.0);
        assertThat(flow).atLine(12).hasVariable("f", 44.0);
    }

    @Test
    public void shouldCaptureBooleanAssignment() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();
        new BooleanUsage().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow).atLine(7).hasVariable("b", false);
        assertThat(flow).atLine(8).hasVariable("b", true);
        assertThat(flow).atLine(9).hasVariable("b", false);
        assertThat(flow).atLine(10).hasVariable("b", true);
        assertThat(flow).atLine(11).hasVariable("b", false);
    }


    @Test
    public void shouldCaptureDoubleAssignment() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();
        new DoubleUsage().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow).atLine(7).hasVariable("d", 0.0);
        assertThat(flow).atLine(8).hasVariable("d", 1.0);
        assertThat(flow).atLine(9).hasVariable("d", 2.01);
        assertThat(flow).atLine(10).hasVariable("d", 42.0);
        assertThat(flow).atLine(11).hasVariable("d", 43.0);
        assertThat(flow).atLine(12).hasVariable("d", 44.0);
    }

}
