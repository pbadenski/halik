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
import io.halik.agent.output.JSONOutput;
import org.assertj.core.api.Condition;
import org.assertj.core.data.Index;
import io.halik.agent.capture.FlowFacade;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import test.halik.*;

import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static io.halik.agent.integration.asserts.FlowAssert.assertThat;

public class Maps {

    @Test
    public void shouldCaptureMapsOfPrimitivesAssignment() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();
        new MapsOfStuff().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow).atLine(11).hasReference(MapsOfStuff.FIRST_REFERENCE);
        assertThat(flow)
                .atLine(13)
                .hasReference(MapsOfStuff.FIRST_REFERENCE)
                .asList()
                .extracting("map")
                .has(new Condition<Object>() {
                    @Override
                    public boolean matches(Object object) {
                        Map<String, JSONObject> json = (Map) object;
                        return json.get("k").get("t").equals("P")
                                && json.get("k").get("v").equals("foo")
                                && json.get("v").get("t").equals("P")
                                && json.get("v").get("v").equals(1);
                    }
                }, Index.atIndex(0));

    }

    @Test
    public void shouldHandleMapWithNullEntrySet() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();

        new MapWithNullEntrySet().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow)
                .atLine(9).hasVariable("map");

    }

    @Test
    public void shouldHandleMapWithEntrySetSizeThrowingException() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();

        new MapWithEntrySetSizeThrowingException().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow)
                .atLine(8).hasVariable("map");

    }

    @Test
    public void shouldBeFineIfMapCannotBeIteratedOver() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();
        new MapThatCannotBeIteratedOver().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow)
                .atLine(23)
                .hasReference(MapThatCannotBeIteratedOver.REFERENCE);
    }
}
