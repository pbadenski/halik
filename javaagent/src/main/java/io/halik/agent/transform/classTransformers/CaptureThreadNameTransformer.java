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
package io.halik.agent.transform.classTransformers;

import io.halik.agent.capture.FlowFacade;
import io.halik.agent.transform.utils.ComputeCommonSuperClass;
import io.halik.agent.transform.utils.ComputeCommonSuperClassWriter;
import javassist.bytecode.Opcode;
import org.objectweb.asm.*;
import org.objectweb.asm.util.CheckClassAdapter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import static io.halik.agent.BytecodeUtilitiesDumpingGround.decideFlagsForClassWriterBasedOnClassJavaVersion;
import static io.halik.agent.BytecodeUtilitiesDumpingGround.duplicateValueSecondFromTopOnStackWhichIsObjectReference;
import static io.halik.agent.BytecodeUtilitiesDumpingGround.readClassMajorVersion;

public class CaptureThreadNameTransformer implements ClassFileTransformer {
    private final boolean testMode;
    private final boolean debug;

    public CaptureThreadNameTransformer(boolean testMode, boolean debug) {
        this.testMode = testMode;
        this.debug = debug;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classFileBuffer) throws IllegalClassFormatException {
        if (!isThread(className)) {
            return classFileBuffer;
        }
        ClassReader reader = new ClassReader(classFileBuffer);
        ClassWriter writer =
                new ComputeCommonSuperClassWriter(
                        reader,
                        decideFlagsForClassWriterBasedOnClassJavaVersion(readClassMajorVersion(classFileBuffer)));
        ClassVisitor classVisitor = new CaptureThreadNameClassVisitor(writer);
        if (debug) {
            classVisitor = new CheckClassAdapter(classVisitor);
        }
        reader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }

    private boolean isThread(String className) {
        return ComputeCommonSuperClass.cowardlyIsInstanceOf(className, "java/lang/Thread");
    }


    private class CaptureThreadNameClassVisitor extends ClassVisitor {
        public CaptureThreadNameClassVisitor(ClassWriter writer) {
            super(Opcodes.ASM5, writer);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return new CaptureThreadNameMethodVisitor(name, cv.visitMethod(access, name, desc, signature, exceptions));
        }
    }

    private class CaptureThreadNameMethodVisitor extends MethodVisitor {
        private final String thisMethodName;

        public CaptureThreadNameMethodVisitor(String thisMethodName, MethodVisitor mv) {
            super(Opcodes.ASM5, mv);
            this.thisMethodName = thisMethodName;
        }

        @Override
        public void visitInsn(int opcode) {
            switch ( opcode ) {
                case Opcodes.ARETURN:
                case Opcodes.DRETURN:
                case Opcodes.FRETURN:
                case Opcodes.IRETURN:
                case Opcodes.LRETURN:
                case Opcodes.RETURN:
                case Opcodes.ATHROW:
                    if (thisMethodName.equals("setName") || thisMethodName.equals("<init>")) {
                        mv.visitVarInsn(Opcode.ALOAD, 0);
                        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "io/halik/agent/boot/ThreadNames", "recordThreadName", "(Ljava/lang/Thread;)V", false);
                    }
                    break;
                default:
                    break;
            }
            mv.visitInsn(opcode);
        }
    }
}
