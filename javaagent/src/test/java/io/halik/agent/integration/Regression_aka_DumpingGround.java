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
import io.halik.agent.capture.Flow;
import io.halik.agent.capture.FlowFacade;
import io.halik.agent.output.JSONOutput;
import io.halik.agent.capture.track.variables.CompleteVariableDefinition;
import org.json.JSONArray;
import org.junit.Test;
import org.objectweb.asm.Type;
import test.halik.*;

import static io.halik.agent.integration.asserts.FlowAssert.assertThat;

public class Regression_aka_DumpingGround {
    @Test
    public void shouldIgnoreLocalFullFrameRedefinitionToAvoidNullPointerExceptions() {
        FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();

        new FrameFullVariables().main();
    }

    @Test
    public void shouldIgnoreClassCastExceptionsCausedByFrameRedefinition_THIS_NEEDS_TO_BE_SUPPORTED_BY_SCOPED_VARIABLE_DEFINITIONS() {
        FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();

        new FrameFullVariablesWithLimitedScopeVariable().main();
    }

    @Test
    public void shouldStopCaptureIfClientCodeIsExecutedWithCustomCollectionsToAvoidStackOverflowError() {
        FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();

        new CustomCollection().main();
    }

    @Test
    public void shouldCaptureVariableStateWhenGotoStatement() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();

        new Goto().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow).allAtLine(7)
                .hasVariable("i", 1)
                .hasVariable("i", 2)
                .hasVariable("i", 3);
    }

    @Test
    public void shouldCaptureCorrectStackTraceDepth() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();

        int expectedStackTraceDepth = new StackTraceDepth().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow)
                .atLine(6)
                .hasStackTraceDepth(expectedStackTraceDepth);
    }

    @Test
    public void shouldCaptureDifferentVariablesOnSameLocalIndexButWithinDifferentScopeCorrectly() {
        Flow flow = FlowFacade.__createTestInstance();
        JSONOutput jsonOutput = flow.__redirectOutputToJSONObject();
        flow.captureVariableDefinition(new CompleteVariableDefinition(0, 0, Type.BOOLEAN_TYPE, 5, 6, "b", 0));
        flow.captureVariableDefinition(new CompleteVariableDefinition(0, 0, Type.FLOAT_TYPE, 7, 8, "f", 0));
        flow.captureVariableModification(1, 0);
        flow.captureLineExecution(0, 0, 6, 6);
        flow.captureVariableModification(1.5f, 0);
        flow.captureLineExecution(0, 0, 8, 8);
        assertThat(jsonOutput.toJSON())
                .atLine(6)
                .hasVariable("b", true);
        assertThat(jsonOutput.toJSON())
                .atLine(8)
                .hasVariable("f", 1.5);
    }

    @Test
    public void shouldCaptureCorrectlyWhenVariableSeemsToBeDeclaredBeforeWhatsReportedInLocalVariableTable() {
        /**
         *
         * See "LocalVariableTableAndVariableLocationMismatch" for details
         *
         * For 'LOCALVARIABLE b Z L3 L1 1'
         *
         * Variable 'b' scope is defined as L3 -> L1, even though
         * there is 'ISTORE 1' in L2 which is right before L3. At the
         * same time L3 does not match any line explicitly. Halik's
         * code assumes it can use start of the scope to read the line
         * number - so we defensively make sure all labels have some idea
         * of what line do they correspond to.
         *
         * This test makes sure that logic is not removed.
         *
         */
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();

        new LocalVariableTableAndVariableLocationMismatch().main();

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow)
                .atLine(11)
                .hasVariable("b", false);
    }

    @Test
    public void shouldCaptureJava_13_ProgramUsingDeprecatedJSR_RET() {
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();

        new Java_13_JSR_RET_Test().main(null);

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow)
                .atLine(11)
                .hasVariable("i", 2);
    }

    @Test
    public void shouldDumpVariableStateLeftAtMethodExit() {
        // or otherwise after we leave method b() we get some indices
        // that we try to capture based on method a() local variable table
        JSONOutput jsonOutput = FlowFacade.__redirectOutputToJSONObject();
        SingleAgentLoader.load();

        new VariableStateLeftAtMethodExit().main(null);

        JSONArray flow = jsonOutput.toJSON();
        assertThat(flow)
                .atLine(11)
                .doesNotHaveVariable("a");
    }
}
