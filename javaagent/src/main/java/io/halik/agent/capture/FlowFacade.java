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

import com.google.common.util.concurrent.MoreExecutors;
import io.halik.agent.boot.ThreadNames;
import io.halik.agent.capture.track.variables.CompleteVariableDefinition;
import io.halik.agent.capture.track.variables.VariableDefinitionLookup;
import io.halik.agent.output.JSONOutput;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class FlowFacade {
    public static final String BYTECODE_TYPE_NAME = "io/halik/agent/capture/FlowFacade";
    private static Flow flow = setupFlow(new Flow());

    private static Flow setupFlow(final Flow flow) {
        flow.createFlowDirectory();
        flow.registerHookToPrintSessionURLOnExit();
        ThreadNames.registerThreadNameChangeListener(new ThreadNames.OnThreadNameChange() {
            @Override
            public void process(ThreadNames.ThreadNameChange threadNameChange) {
                flow.captureThreadNameChange(threadNameChange.id, threadNameChange.name);
            }
        });
        return flow;
    }

    public static JSONOutput __redirectOutputToJSONObject() {
        flow = __createTestInstance();
        return flow.__redirectOutputToJSONObject();
    }

    public static Flow __createTestInstance() {
        class InTestFlow extends Flow {
            public InTestFlow(VariableDefinitionLookup variableDefinitionLookup) {
                super(variableDefinitionLookup);
            }

            public InTestFlow() {
            }

            @Override
            public void captureMetadata(boolean captureCollections) {
                
            }

            @Override
            public void captureThreadNameChange(long id, String name) {
                System.out.println(id);
                System.out.println(name);
            }

            @Override
            public void captureClassName(String name) {
            }

            @Override
            public void captureMethodNames(List<String> methodIndex) {
            }

            @Override
            protected void notifyNewSession() {
            }

            @Override
            protected ExecutorService createWritersExecutorService() {
                return MoreExecutors.newDirectExecutorService();
            }

            @Override
            void createFlowDirectory() {
            }

            @Override
            void registerHookToPrintSessionURLOnExit() {
            }
        }
        if (flow == null) {
            flow = new InTestFlow();
        } else {
            flow = new InTestFlow(variableDefinitionLookup());
        }
        return setupFlow(flow);
    }

    public static void captureLineExecution(
            int classIndex, int methodIndex, int lineNumber, int labelNumber) {
        flow.captureLineExecution(classIndex, methodIndex, lineNumber, labelNumber);
    }

    public static void dumpLeftoverVariableStateAtMethodExit() {
        flow.dumpLeftoverVariableStateAtMethodExit();
    }

    public static void captureVariableDefinition(CompleteVariableDefinition completeVariableDefinition) {
        flow.captureVariableDefinition(completeVariableDefinition);
    }

    public static void captureTag(String tag) {
        flow.captureTag(tag);
    }

    public static void captureVariableModification(Object value, int index) {
        flow.captureVariableModification(value, index);
    }

    public static void captureVariableModification(int value, int index) {
        flow.captureVariableModification(value, index);
    }

    public static void captureVariableModification(float value, int index) {
        flow.captureVariableModification(value, index);
    }

    public static void captureVariableModification(double value, int index) {
        flow.captureVariableModification(value, index);
    }

    public static void captureVariableModification(long value, int index) {
        flow.captureVariableModification(value, index);
    }

    public static void captureArrayModification(Object value) {
        flow.captureArrayModification(value);
    }

    public static void captureCollectionModification(Collection value) {
        flow.captureCollectionModification(value);
    }

    public static void captureMapModification(Map value) {
        flow.captureMapModification(value);
    }

    public static void captureFieldModification(Object object, String fieldName, Object fieldValue) {
        flow.captureFieldModification(object, fieldName, fieldValue);
    }

    public static void captureFieldModification(Object object, String fieldName, boolean fieldValue) {
        flow.captureFieldModification(object, fieldName, fieldValue);
    }

    public static void captureFieldModification(Object object, String fieldName, double fieldValue) {
        flow.captureFieldModification(object, fieldName, fieldValue);
    }

    public static void captureFieldModification(Object object, String fieldName, float fieldValue) {
        flow.captureFieldModification(object, fieldName, fieldValue);
    }

    public static void captureFieldModification(Object object, String fieldName, long fieldValue) {
        flow.captureFieldModification(object, fieldName, fieldValue);
    }

    public static void captureFieldModification(Object object, String fieldName, short fieldValue) {
        flow.captureFieldModification(object, fieldName, fieldValue);
    }

    public static void captureFieldModification(Object object, String fieldName, byte fieldValue) {
        flow.captureFieldModification(object, fieldName, fieldValue);
    }

    public static void captureFieldModification(Object object, String fieldName, char fieldValue) {
        flow.captureFieldModification(object, fieldName, fieldValue);
    }

    public static void captureFieldModification(Object object, String fieldName, int fieldValue) {
        flow.captureFieldModification(object, fieldName, fieldValue);
    }

    public static void captureMetadata(boolean captureCollections) {
        flow.captureMetadata(captureCollections);
    }

    public static void captureClassName(String name) {
        flow.captureClassName(name);
    }

    public static VariableDefinitionLookup variableDefinitionLookup() {
        return flow.variableDefinitionLookup();
    }

    public static void increase() {
        flow.increase();
    }

    public static void decrease() {
        flow.decrease();
    }

    public static void fetch() {
        flow.fetch(1);
    }

    public static void captureMethodNames(List<String> methodIndex) {
        flow.captureMethodNames(methodIndex);
    }
}
