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
package io.halik.agent.capture.track.variables;

import com.google.common.base.Function;
import io.halik.agent.capture.DumpingGround;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.Map;

import static io.halik.agent.capture.DumpingGround.uniqueMethodIdentifier;

public class VariableModificationTracker {

    private Map<Integer, Object> modifiedVariablesByIndex =  new Int2ObjectOpenHashMap<>();

    public void trackVariableModification(int index, Object value) {
        if (DumpingGround.shouldBeSkippedByCapture(value)) {
            return;
        }
        modifiedVariablesByIndex.put(index, value);
    }

    public CollectedModifications collect(
            VariableDefinitionLookup variableDefinitionLookup,
            int classIndex,
            int methodIndex,
            int atLabel) {
        Map<Integer, Object> modifiedVariablesByIndexToBeProcessed = modifiedVariablesByIndex;
        modifiedVariablesByIndex = new Int2ObjectOpenHashMap<>();

        List<Variable> collectedVariables = new ObjectArrayList<>();
        for (Map.Entry<Integer, Object> entry : modifiedVariablesByIndexToBeProcessed.entrySet()) {
            SimpleVariableDefinition variableDefinition =
                    variableDefinitionLookup.lookupVariableDefinition(
                            uniqueMethodIdentifier(classIndex, methodIndex),
                            entry.getKey(),
                            atLabel);
            // variable comes from overridden frame - ignore | honestly, I know too little about JVM yet,
            // and I don't understand if I should care about this :(...
            if (variableDefinition != null) {
                collectedVariables.add(variableDefinition.variable(entry.getValue()));
            }
        }
        return new CollectedModifications(collectedVariables);
    }

    public int count() {
        return modifiedVariablesByIndex.size();
    }

    public void dumpAll() {
        modifiedVariablesByIndex = new Int2ObjectOpenHashMap<>();
    }

    public static class CollectedModifications {
        private final List<Variable> collectedVariables;

        public CollectedModifications(List<Variable> collectedVariables) {
            this.collectedVariables = collectedVariables;
        }

        public void consume(Function<Variable, Object> function) {
            for (Variable variable : collectedVariables) {
                function.apply(variable);
            }
        }

        public int count() {
            return collectedVariables.size();
        }
    }
}
