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
package io.halik.agent.transform.methodVisitors;

import com.google.common.collect.Iterators;
import com.google.common.collect.Range;
import io.halik.agent.capture.FlowFacade;
import io.halik.agent.capture.track.variables.SimpleVariableDefinition;
import io.halik.agent.capture.track.variables.TypeInModification;
import io.halik.agent.transform.VariableStateCapturer;
import it.unimi.dsi.fastutil.objects.Reference2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;
import java.util.Set;

import static io.halik.agent.capture.DumpingGround.uniqueMethodIdentifier;

public class CaptureVariableAssignmentMethodVisitor extends MethodVisitor {
    private final int classIndex;
    private int methodIndex;
    private final String methodName;

    private final VariableStateCapturer variableStateCapturer = new VariableStateCapturer();
    private Map<Label, Integer> labels = new Reference2IntLinkedOpenHashMap<>();
    private Set<Label> disableTrackingFor = new ReferenceOpenHashSet<>();

    public CaptureVariableAssignmentMethodVisitor(MethodVisitor mv, int classIndex, int methodIndex,
                                                  String methodName) {
        super(Opcodes.ASM5, mv);
        this.classIndex = classIndex;
        this.methodIndex = methodIndex;
        this.methodName = methodName;
    }

    @Override
    public void visitIincInsn(int index, int increment) {
        mv.visitIincInsn(index, increment);
        TypeInModification.afterVariableUsage(Opcodes.IINC, index, variableStateCapturer);
    }

    @Override
    public void visitVarInsn(int opcode, int index) {
        mv.visitVarInsn(opcode, index);
        if (disableTrackingFor.contains(currentLabel())) {
            return;
        }
        TypeInModification.afterVariableUsage(opcode, index, variableStateCapturer);
    }

    private Label currentLabel() {
        Map.Entry<Label, Integer> lastEntry = Iterators.getLast(labels.entrySet().iterator(), null);
        if (lastEntry == null) {
            return null;
        }
        return lastEntry.getKey();
    }

    @Override
    public void visitLabel(Label label) {
        labels.put(label, labels.size());
        captureFromDeclarations(label);
        variableStateCapturer.captureState(mv, methodName);
        mv.visitLabel(label);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        mv.visitLineNumber(line, start);
        variableStateCapturer.captureState(mv, methodName);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        // assume any captured variable is address type value
        // which can't be tracked... potentially might cause losing
        // on actual proper variable modifications, but I'm accepting
        // the risk. JSR is deprecated and only exists in classes compiled
        // by legacy compilers.
        if (opcode == Opcodes.JSR) {
            disableTrackingFor.add(label);
        } else {
            variableStateCapturer.captureState(mv, methodName);
        }
        mv.visitJumpInsn(opcode, label);
    }

    private void captureFromDeclarations(Label start) {
        Iterable<Map.Entry<Range<Integer>, SimpleVariableDefinition>> variableDefinitions =
                FlowFacade.variableDefinitionLookup().lookupVariableDefinitions(
                        uniqueMethodIdentifier(classIndex, methodIndex),
                        labels.get(start));
        for (Map.Entry<Range<Integer>, SimpleVariableDefinition> variableByIndex : variableDefinitions) {
            variableStateCapturer.trackOnVariableModification(
                    variableByIndex.getValue().index,
                    TypeInModification.byType(variableByIndex.getValue().type)
            );
        }
    }
}
