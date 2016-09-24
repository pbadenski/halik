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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import sun.reflect.Reflection;

import java.lang.reflect.Field;
import java.util.Set;

public class CaptureFieldAssignmentThroughReflectionMethodVisitor extends MethodVisitor {
    public CaptureFieldAssignmentThroughReflectionMethodVisitor(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (owner.equals("java/lang/reflect/Field") && name.equals("set")) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "io/halik/agent/capture/FieldFacade", "setField", "(Ljava/lang/reflect/Field;Ljava/lang/Object;Ljava/lang/Object;)V", false);
        } else {
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }
}
