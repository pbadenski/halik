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
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CaptureJUnitTagMethodVisitor extends MethodVisitor {
    public CaptureJUnitTagMethodVisitor(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc.equals("Lorg/junit/Test;")) {
            mv.visitLdcInsn("junit");
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    FlowFacade.BYTECODE_TYPE_NAME,
                    "captureTag", "(Ljava/lang/String;)V", false);
        }
        return mv.visitAnnotation(desc, visible);
    }
}
