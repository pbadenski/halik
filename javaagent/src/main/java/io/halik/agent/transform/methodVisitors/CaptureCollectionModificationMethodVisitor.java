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
import io.halik.agent.transform.utils.ComputeCommonSuperClass;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static io.halik.agent.BytecodeUtilitiesDumpingGround.duplicateValueSecondFromTopOnStackWhichIsObjectReference;
import static io.halik.agent.BytecodeUtilitiesDumpingGround.duplicateValueThirdFromTopOnStackWhichIsObjectReference;

public class CaptureCollectionModificationMethodVisitor extends MethodVisitor {

    public CaptureCollectionModificationMethodVisitor(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        try {
            Type[] argumentTypes = Type.getArgumentTypes(desc);
            boolean isBeingInstrumented = false;
            if (static_java_util_Collections_callOperatingOnCollectionWithinAreaOfInterest(opcode, owner, name)) {
                if (oneTypeOneArgument(argumentTypes)) {
                    isBeingInstrumented = true;
                    mv.visitInsn(Opcodes.DUP);
                } else if (twoTypeOneArguments(argumentTypes)) {
                    isBeingInstrumented = true;
                    duplicateValueSecondFromTopOnStackWhichIsObjectReference(argumentTypes[0], mv);
                }
            }
            if (dynamic_java_util_Collection_call(opcode, owner)
                    && !name.equals("<init>")) {
                if (oneTypeOneArgument(argumentTypes)) {
                    isBeingInstrumented = true;
                    duplicateValueSecondFromTopOnStackWhichIsObjectReference(Type.getType(owner), mv);
                } else if (twoTypeOneArguments(argumentTypes)) {
                    isBeingInstrumented = true;
                    duplicateValueThirdFromTopOnStackWhichIsObjectReference(mv);
                }

            }
            mv.visitMethodInsn(opcode, owner, name, desc, itf);

            if (static_java_util_Arrays_asList(opcode, owner, name)
                || (dynamic_java_util_Collection_call(opcode, owner)
                    && name.equals("<init>"))) {
                isBeingInstrumented = true;
                mv.visitInsn(Opcodes.DUP);
            }
            if (isBeingInstrumented) {
                if (!Type.getReturnType(desc).equals(Type.VOID_TYPE)) {
                    mv.visitInsn(Opcodes.SWAP);
                }
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, FlowFacade.BYTECODE_TYPE_NAME, "captureCollectionModification", "(Ljava/util/Collection;)V", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean dynamic_java_util_Collection_call(int opcode, String owner) {
        return opcode != Opcodes.INVOKESTATIC
                && ComputeCommonSuperClass.cowardlyIsInstanceOf(owner, "java/util/Collection");
    }

    private boolean oneTypeOneArgument(Type[] argumentTypes) {
        return argumentTypes.length == 1
                && argumentTypes[0].getSize() == 1;
    }

    private boolean static_java_util_Arrays_asList(int opcode, String owner, String name) {
        return opcode == Opcodes.INVOKESTATIC
                && owner.equals("java/util/Arrays")
                && name.equals("asList");
    }

    private boolean static_java_util_Collections_callOperatingOnCollectionWithinAreaOfInterest(int opcode, String owner, String name) {
        boolean methodMayHaveModifiedContentsOfCollection = name.equals("addAll")
                || name.equals("remove")
                || name.equals("removeAll")
                || name.equals("fill")
                || name.equals("copy")
                || name.equals("replaceAll")
                || name.equals("reverse")
                || name.equals("shuffle")
                || name.equals("sort");
        return opcode == Opcodes.INVOKESTATIC
                && owner.equals("java/util/Collections")
                && methodMayHaveModifiedContentsOfCollection;
    }

    private boolean twoTypeOneArguments(Type[] argumentTypes) {
        return argumentTypes.length == 2
                && argumentTypes[0].getSize() == 1
                && argumentTypes[1].getSize() == 1;
    }
}
