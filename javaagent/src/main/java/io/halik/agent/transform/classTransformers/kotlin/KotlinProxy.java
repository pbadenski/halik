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
package io.halik.agent.transform.classTransformers.kotlin;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class KotlinProxy extends MethodVisitor {
    private String methodName;
    private final MethodVisitor originalVisitor;

    public KotlinProxy(String methodName, MethodVisitor modifiedVisitor, MethodVisitor originalVisitor) {
        super(Opcodes.ASM5, modifiedVisitor);
        this.methodName = methodName;
        this.originalVisitor = originalVisitor;
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        if (Rules.dontCaptureVariable(name)) {
            originalVisitor.visitLocalVariable(name, desc, signature, start, end, index);
        } else {
            super.visitLocalVariable(name, desc, signature, start, end, index);
        }
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        if (Rules.dontCaptureField(owner, methodName)) {
            originalVisitor.visitFieldInsn(opcode, owner, name, desc);
        } else {
            super.visitFieldInsn(opcode, owner, name, desc);
        }
    }

}
