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
package sandbox.io.halik;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class SandboxMethodVisitor extends MethodVisitor {
    public SandboxMethodVisitor(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }

    @Override
    public void visitCode() {
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, SandboxFlow.BYTECODE_TYPE_NAME, "enter", "()V", false);
        mv.visitCode();
    }


    @Override
    public void visitInsn ( int inst ) {
        switch ( inst ) {
            case Opcodes.ARETURN:
            case Opcodes.DRETURN:
            case Opcodes.FRETURN:
            case Opcodes.IRETURN:
            case Opcodes.LRETURN:
            case Opcodes.RETURN:
            case Opcodes.ATHROW:
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, SandboxFlow.BYTECODE_TYPE_NAME, "exit", "()V", false);
                break;
            default:
                break;
        }

        super.visitInsn ( inst );
    }
    @Override
    public void visitEnd() {

        mv.visitEnd();
    }
}
