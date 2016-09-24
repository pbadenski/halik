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
import org.assertj.core.api.Condition;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import test.halik.CallWithinAgentCode;
import test.halik.LikeJUnit;

import static io.halik.agent.integration.asserts.FlowAssert.assertThat;

public class FlowFacadeTest {

    @Test
    public void shouldTagWhatLooksLikeJUnit() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();

        new LikeJUnit().shouldWork();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow).atLine(7).doesNotHave(tag());
        assertThat(flow).atLine(11).has(tag()).has(tagEqualTo("junit"));
    }

    @Test
    public void shouldStopCapturingWithinAgentCode() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();

        new CallWithinAgentCode().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow)
                .atLine(31)
                .hasVariable("list");

        assertThat(flow)
                .atLine(31)
                .has(new Condition<JSONObject>() {
                    @Override
                    public boolean matches(JSONObject value) {
                        return didntCatchAnyManualFlowFacadeCrapTriggeredFromListIterator(value);
                    }

                    private boolean didntCatchAnyManualFlowFacadeCrapTriggeredFromListIterator(JSONObject value) {
                        int listVariableDeclaration = 1;
                        int listNewInstanceInvocation = 1;
                        return value.getJSONArray("sn").length() == listVariableDeclaration + listNewInstanceInvocation;
                    }
                });
    }

    private Condition<JSONObject> tag() {
        return new Condition<JSONObject>() {
            @Override
            public boolean matches(JSONObject value) {
                return value.has("tg");
            }
        };
    }

    private Condition<JSONObject> tagEqualTo(final String tag) {
        return new Condition<JSONObject>() {
            @Override
            public boolean matches(JSONObject value) {
                return value.getString("tg").equals(tag);
            }
        };
    }

}

