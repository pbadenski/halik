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

import io.halik.agent.transform.methodVisitors.CaptureStackTraceDepthMethodVisitor;
import io.halik.agent.transform.utils.ComputeCommonSuperClassWriter;
import org.objectweb.asm.*;
import org.objectweb.asm.util.CheckClassAdapter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import static io.halik.agent.BytecodeUtilitiesDumpingGround.decideFlagsForClassWriterBasedOnClassJavaVersion;
import static io.halik.agent.BytecodeUtilitiesDumpingGround.readClassMajorVersion;
import static io.halik.agent.transform.utils.ShouldTransformClass.*;
import static io.halik.agent.transform.utils.ShouldTransformClass.builtinJavaClass;

public class CaptureStackTraceDepthTransformer implements ClassFileTransformer {
    private boolean testMode;
    private boolean debug;

    public CaptureStackTraceDepthTransformer(boolean testMode, boolean debug) {
        this.testMode = testMode;
        this.debug = debug;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classFileBuffer) throws IllegalClassFormatException {
        if (classLoaderIsNullWhichMightCauseNoClassDefFoundError(loader)
                || ignored(className)
                || ignoreInTestMode(className)) {
            return classFileBuffer;
        }
        ClassReader reader = new ClassReader(classFileBuffer);
        ClassWriter writer =
                new ComputeCommonSuperClassWriter(
                        reader,
                        decideFlagsForClassWriterBasedOnClassJavaVersion(readClassMajorVersion(classFileBuffer)));
        ClassVisitor classVisitor = new DecoratingClassVisitor(writer);

        if (debug) {
            classVisitor = new CheckClassAdapter(classVisitor);
        }
        reader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
        if (debug) {
            System.out.println(className + " transformed by CSTD transformer.");
        }
        return writer.toByteArray();
    }

    public static boolean ignored(String className) {
        return runningFromIntelliJWhichMightCauseStackOverflowError(className)
                || classBelongsToJavaAgent(className)
                || builtinJavaClass(className);
    }

    private boolean ignoreInTestMode(String className) {
        return testMode && (className.startsWith("com/google"));
    }


    private static class DecoratingClassVisitor extends ClassVisitor {
        public DecoratingClassVisitor(ClassWriter writer) {
            super(Opcodes.ASM5, writer);
        }


        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            try {
                MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
                return new CaptureStackTraceDepthMethodVisitor(mv);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }
}
