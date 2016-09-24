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

import io.halik.agent.capture.FlowFacade;
import io.halik.agent.capture.track.variables.CompleteVariableDefinition;
import it.unimi.dsi.fastutil.objects.Reference2IntLinkedOpenHashMap;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.SortedMap;

public class CaptureVariableDeclarationMethodVisitor extends MethodVisitor {
    private final int classIndex;
    private final int methodIndex;

    private SortedMap<Label, Integer> labels = new Reference2IntLinkedOpenHashMap<>();

    public CaptureVariableDeclarationMethodVisitor(int classIndex, int methodIndex) {
        super(Opcodes.ASM5);
        this.classIndex = classIndex;
        this.methodIndex = methodIndex;
    }

    @Override
    public void visitLabel(Label label) {
        labels.put(label, labels.size());
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        FlowFacade.captureVariableDefinition(
                new CompleteVariableDefinition(classIndex, methodIndex, Type.getType(desc),
                        labels.get(start), labels.get(end), name, index));
    }
}
