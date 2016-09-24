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

import com.google.common.collect.ImmutableMap;
import io.halik.agent.SingleAgentLoader;
import io.halik.agent.capture.FlowFacade;
import io.halik.agent.output.JSONOutput;
import org.json.JSONArray;
import org.junit.Test;
import test.halik.CollectionThatCannotBeIteratedOver;
import test.halik.ListOfBooleans;

import static io.halik.agent.integration.asserts.FlowAssert.assertThat;

public class Collections {
    @Test
    public void shouldCaptureCollectionOfBooleansAssignment() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();
        new ListOfBooleans().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow).atLine(11).hasReference(ListOfBooleans.FIRST_REFERENCE);
        assertThat(flow)
                .atLine(13)
                .hasReference(ListOfBooleans.FIRST_REFERENCE)
                .asList()
                .extracting("map")
                .containsSequence(
                        ImmutableMap.of("t", "P", "v", true),
                        ImmutableMap.of("t", "P", "v", false));

        assertThat(flow)
                .atLine(14)
                .hasReference(ListOfBooleans.FIRST_REFERENCE)
                .asList()
                .extracting("map")
                .containsSequence(
                        ImmutableMap.of("t", "P", "v", true),
                        ImmutableMap.of("t", "P", "v", false),
                        ImmutableMap.of("t", "P", "v", false));

        assertThat(flow)
                .atLine(15)
                .hasReference(ListOfBooleans.FIRST_REFERENCE)
                .asList()
                .extracting("map")
                .containsSequence(
                        ImmutableMap.of("t", "P", "v", false),
                        ImmutableMap.of("t", "P", "v", true),
                        ImmutableMap.of("t", "P", "v", false),
                        ImmutableMap.of("t", "P", "v", false));

        assertThat(flow)
                .atLine(16)
                .hasReference(ListOfBooleans.FIRST_REFERENCE)
                .asList()
                .extracting("map")
                .containsSequence(
                        ImmutableMap.of("t", "P", "v", false),
                        ImmutableMap.of("t", "P", "v", false),
                        ImmutableMap.of("t", "P", "v", false),
                        ImmutableMap.of("t", "P", "v", true));

        assertThat(flow)
                .atLine(17)
                .hasReference(ListOfBooleans.FIRST_REFERENCE)
                .asList()
                .extracting("map")
                .containsSequence(
                        ImmutableMap.of("t", "P", "v", false),
                        ImmutableMap.of("t", "P", "v", false),
                        ImmutableMap.of("t", "P", "v", true));

        assertThat(flow)
                .atLine(18)
                .hasReference(ListOfBooleans.FIRST_REFERENCE)
                .asList()
                .extracting("map")
                .containsSequence(
                        ImmutableMap.of("t", "P", "v", true));
    }

    @Test
    public void shouldBeFineIfCollectionCannotBeIteratedOver() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();
        new CollectionThatCannotBeIteratedOver().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow)
                .atLine(19)
                .hasReference(CollectionThatCannotBeIteratedOver.REFERENCE);
    }
}
