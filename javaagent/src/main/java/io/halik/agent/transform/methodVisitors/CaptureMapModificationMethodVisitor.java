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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static io.halik.agent.BytecodeUtilitiesDumpingGround.duplicateValueThirdFromTopOnStackWhichIsObjectReference;
import static io.halik.agent.transform.utils.ComputeCommonSuperClass.cowardlyIsInstanceOf;

public class CaptureMapModificationMethodVisitor extends MethodVisitor {

    public CaptureMapModificationMethodVisitor(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        try {
            Type[] argumentTypes = Type.getArgumentTypes(desc);
            boolean shouldBeInstrumented =
                    dynamicCallOperatingOnCollectionWithinAreaOfInterest(opcode, owner, argumentTypes);
            if (shouldBeInstrumented) {
                if (dynamicCallOperatingOnCollectionWithinAreaOfInterest(opcode, owner, argumentTypes)) {
                    duplicateValueThirdFromTopOnStackWhichIsObjectReference(mv);
                }
            }
            mv.visitMethodInsn(opcode, owner, name, desc, itf);

            if (shouldBeInstrumented) {
                if (!Type.getReturnType(desc).equals(Type.VOID_TYPE)) {
                    mv.visitInsn(Opcodes.DUP_X1);
                    mv.visitInsn(Opcodes.POP);
                }
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, FlowFacade.BYTECODE_TYPE_NAME, "captureMapModification", "(Ljava/util/Map;)V", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean dynamicCallOperatingOnCollectionWithinAreaOfInterest(int opcode, String owner, Type[] argumentTypes) {
        return opcode != Opcodes.INVOKESTATIC
                && cowardlyIsInstanceOf(owner, "java/util/Map")
                && argumentTypes.length == 2
                && argumentTypes[0].getSize() == 1
                && argumentTypes[1].getSize() == 1;
    }

}
