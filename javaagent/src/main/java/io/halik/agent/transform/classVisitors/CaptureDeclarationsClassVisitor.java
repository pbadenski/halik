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

import io.halik.agent.transform.methodVisitors.CaptureVariableDeclarationMethodVisitor;
import io.halik.agent.transform.classTransformers.kotlin.KotlinFilter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.concurrent.atomic.AtomicInteger;

public class CaptureDeclarationsClassVisitor extends ClassVisitor {
    private int classIndex;
    private AtomicInteger methodIndex = new AtomicInteger(0);
    private boolean useKotlinFilter;

    public CaptureDeclarationsClassVisitor(int classIndex) {
        super(Opcodes.ASM5);
        this.classIndex = classIndex;
    }

    @Override
    public void visitSource(String source, String debug) {
        if (source != null && source.endsWith(".kt")) {
            useKotlinFilter = true;
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        CaptureVariableDeclarationMethodVisitor baseVisitor
                = new CaptureVariableDeclarationMethodVisitor(classIndex, methodIndex.get());
        if (useKotlinFilter) {
            return new KotlinFilter(name, baseVisitor);
        }
        methodIndex.incrementAndGet();
        return baseVisitor;
    }
}
