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
package io.halik.agent.transform.classVisitors;

import io.halik.agent.transform.classTransformers.kotlin.KotlinProxy;
import io.halik.agent.transform.methodVisitors.*;
import org.objectweb.asm.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CaptureFlowAndStateClassVisitor extends ClassVisitor {
    private final int classIndex;
    private final List<String> methodIndex;
    private boolean useKotlinFilter;
    private Set<String> syntheticFields = new HashSet<>();

    public CaptureFlowAndStateClassVisitor(ClassWriter writer, int classIndex, List<String> methodIndex) {
        super(Opcodes.ASM5, writer);
        this.classIndex = classIndex;
        this.methodIndex = methodIndex;
    }

    @Override
    public void visitSource(String source, String debug) {
        if (source != null && source.endsWith(".kt")) {
            useKotlinFilter = true;
        }
        cv.visitSource(source, debug);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
            syntheticFields.add(name + ":" + desc);
        }
        return cv.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        CaptureFlowMethodVisitor flowVisitor =
                new CaptureFlowMethodVisitor(mv, classIndex, methodIndex.size(), desc);

        MethodVisitor variableStateMethodVisitor =
                new CaptureVariableAssignmentMethodVisitor(flowVisitor, classIndex, methodIndex.size(), name);
        if (useKotlinFilter) {
            variableStateMethodVisitor = new KotlinProxy(name, variableStateMethodVisitor, flowVisitor);
        }

        MethodVisitor fieldAssignmentMethodVisitor =
                new CaptureFieldAssignmentMethodVisitor(variableStateMethodVisitor, syntheticFields);
        if (useKotlinFilter) {
            fieldAssignmentMethodVisitor = new KotlinProxy(name, fieldAssignmentMethodVisitor, variableStateMethodVisitor);
        }

        MethodVisitor fieldAssignmentThroughReflectionMethodVisitor =
                new CaptureFieldAssignmentThroughReflectionMethodVisitor(fieldAssignmentMethodVisitor);

        MethodVisitor arrayStateMethodVisitor =
                new CaptureArrayModificationMethodVisitor(fieldAssignmentThroughReflectionMethodVisitor);

        methodIndex.add(name);
        return new CaptureJUnitTagMethodVisitor(arrayStateMethodVisitor);
    }
}
