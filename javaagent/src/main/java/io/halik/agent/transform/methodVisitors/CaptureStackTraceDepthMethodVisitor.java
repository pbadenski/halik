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
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class CaptureStackTraceDepthMethodVisitor extends MethodVisitor {
    private Label catchBlock;

    public CaptureStackTraceDepthMethodVisitor(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }

    @Override
    public final void visitCode() {
        mv.visitCode();
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, FlowFacade.BYTECODE_TYPE_NAME, "increase", "()V", false);
    }

    @Override
    public final void visitInsn(int opcode) {
        switch ( opcode ) {
            case Opcodes.ARETURN:
            case Opcodes.DRETURN:
            case Opcodes.FRETURN:
            case Opcodes.IRETURN:
            case Opcodes.LRETURN:
            case Opcodes.RETURN:
            case Opcodes.ATHROW:
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, FlowFacade.BYTECODE_TYPE_NAME, "decrease", "()V", false);
                break;
            default:
                break;
        }
        mv.visitInsn(opcode);
    }

    @Override
    public final void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        mv.visitTryCatchBlock(start, end, handler, type);
        catchBlock = handler;
    }

    @Override
    public final void visitLabel(Label label) {
        mv.visitLabel(label);
        if (label == catchBlock) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, FlowFacade.BYTECODE_TYPE_NAME, "fetch", "()V", false);
        }
    }
}
