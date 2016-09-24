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
import test.halik.FieldArrayOfInts;
import test.halik.FieldModificationThroughReflection;

import static com.google.common.collect.Lists.newArrayList;
import static io.halik.agent.integration.asserts.FlowAssert.assertThat;

public class Fields {
    @Test
    public void shouldCaptureArrayOfIntegersAssignment() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();

        new FieldArrayOfInts().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow)
                .atLine(16)
                .hasField("array", newArrayList(1, 2), FieldArrayOfInts.OBJECT_REFERENCE);
    }

    @Test
    public void shouldCaptureModificationThroughReflection() throws Exception {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();

        new FieldModificationThroughReflection().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow)
                .atLine(10)
                .hasField("foo", 2, FieldModificationThroughReflection.REFERENCE);
    }

}
