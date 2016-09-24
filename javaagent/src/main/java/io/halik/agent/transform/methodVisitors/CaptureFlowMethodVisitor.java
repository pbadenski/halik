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
import it.unimi.dsi.fastutil.objects.Reference2IntLinkedOpenHashMap;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;

public class CaptureFlowMethodVisitor extends MethodVisitor {
    private final int classIndex;
    private final int methodIndex;
    private final String methodDescriptor;

    private Map<Label, Integer> labels = new Reference2IntLinkedOpenHashMap<>();

    public CaptureFlowMethodVisitor(MethodVisitor mv, int classIndex, int methodIndex, String methodDescriptor) {
        super(Opcodes.ASM5, mv);
        this.classIndex = classIndex;
        this.methodIndex = methodIndex;
        this.methodDescriptor = methodDescriptor;
    }

    @Override
    public void visitInsn(int opcode) {
        switch ( opcode ) {
            case Opcodes.ARETURN:
            case Opcodes.DRETURN:
            case Opcodes.FRETURN:
            case Opcodes.IRETURN:
            case Opcodes.LRETURN:
            case Opcodes.RETURN:
            case Opcodes.ATHROW:
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, FlowFacade.BYTECODE_TYPE_NAME, "dumpLeftoverVariableStateAtMethodExit", "()V", false);
                break;
            default:
                break;
        }
        mv.visitInsn(opcode);
    }

    @Override
    public void visitLabel(Label label) {
        labels.put(label, labels.size());
        mv.visitLabel(label);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        captureLineExecution(line, start);
        mv.visitLineNumber(line, start);
    }

    private void captureLineExecution(int line, Label start) {
        mv.visitIntInsn(Opcodes.SIPUSH, classIndex);
        mv.visitIntInsn(Opcodes.SIPUSH, methodIndex);
        mv.visitIntInsn(Opcodes.SIPUSH, line);
        mv.visitIntInsn(Opcodes.SIPUSH, labels.get(start));
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, FlowFacade.BYTECODE_TYPE_NAME, "captureLineExecution", "(IIII)V", false);
    }
}
