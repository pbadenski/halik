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

import io.halik.agent.BytecodeUtilitiesDumpingGround;
import io.halik.agent.capture.FlowFacade;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Set;

public class CaptureFieldAssignmentMethodVisitor extends MethodVisitor {

    private final Set<String> syntheticFields;

    public CaptureFieldAssignmentMethodVisitor(MethodVisitor mv, Set<String> syntheticFields) {
        super(Opcodes.ASM5, mv);
        this.syntheticFields = syntheticFields;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        if (opcode == Opcodes.PUTFIELD && !fieldNamesToBeIgnored(name, desc)) {
            org.objectweb.asm.Type type = org.objectweb.asm.Type.getType(desc);
            BytecodeUtilitiesDumpingGround.duplicateValueSecondFromTopOnStackWhichIsObjectReference(type, mv);
            // Stack | ObjRef
        }
        mv.visitFieldInsn(opcode, owner, name, desc);
        if (opcode == Opcodes.PUTFIELD && !fieldNamesToBeIgnored(name, desc)) {
            mv.visitInsn(Opcodes.DUP);

            // Stack | ObjRef, ObjRef
            mv.visitLdcInsn(name);
            // Stack | ObjRef, ObjRef, String(fieldName)
            mv.visitInsn(Opcodes.SWAP);
            // Stack | ObjRef, String(fieldName), ObjRef
            mv.visitFieldInsn(Opcodes.GETFIELD, owner, name, desc);
            // Stack | ObjRef, String(fieldName), Value

            String type = BytecodeUtilitiesDumpingGround.typeIsObjectOrNDimensionalArray(desc) ? "Ljava/lang/Object;" : desc;
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    FlowFacade.BYTECODE_TYPE_NAME,
                    "captureFieldModification",
                    "(Ljava/lang/Object;Ljava/lang/String;" + type + ")V",
                    false);
        }
    }

    private boolean fieldNamesToBeIgnored(String name, String desc) {
        return syntheticFields.contains(name + ":" + desc);
    }
}
