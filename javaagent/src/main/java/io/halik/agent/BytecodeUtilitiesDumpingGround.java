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
/* Copyright (c) 2015-2016 Pawel Badenski */
package io.halik.agent;

import io.halik.agent.transform.classTransformers.CaptureStackTraceDepthTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.*;
import java.lang.instrument.IllegalClassFormatException;

public class BytecodeUtilitiesDumpingGround {
    public static boolean isMethodExit(int opcode) {
        return opcode == Opcodes.RETURN
                || opcode == Opcodes.IRETURN
                || opcode == Opcodes.FRETURN
                || opcode == Opcodes.ARETURN
                || opcode == Opcodes.LRETURN
                || opcode == Opcodes.DRETURN
                || opcode == Opcodes.ATHROW;
    }

    public static boolean typeIsObjectOrNDimensionalArray(String desc) {
        return desc.endsWith(";") || desc.startsWith("[");
    }

    public static void duplicateValueSecondFromTopOnStackWhichIsObjectReference(org.objectweb.asm.Type typeOnTopOnStack, MethodVisitor mv) {
        if (typeOnTopOnStack == org.objectweb.asm.Type.LONG_TYPE
         || typeOnTopOnStack == org.objectweb.asm.Type.DOUBLE_TYPE) {
            mv.visitInsn(Opcodes.DUP2_X1);
            mv.visitInsn(Opcodes.POP2);
            mv.visitInsn(Opcodes.DUP_X2);
            mv.visitInsn(Opcodes.DUP_X2);
            mv.visitInsn(Opcodes.POP);
        } else {
            mv.visitInsn(Opcodes.DUP_X1);
            mv.visitInsn(Opcodes.POP);
            mv.visitInsn(Opcodes.DUP_X1);
            mv.visitInsn(Opcodes.DUP_X1);
            mv.visitInsn(Opcodes.POP);
        }
    }

    public static void duplicateValueThirdFromTopOnStackWhichIsObjectReference(MethodVisitor mv) {
        mv.visitInsn(Opcodes.DUP_X2);
        mv.visitInsn(Opcodes.POP);
        mv.visitInsn(Opcodes.SWAP);
        mv.visitInsn(Opcodes.DUP);
        mv.visitInsn(Opcodes.DUP2_X2);
        mv.visitInsn(Opcodes.POP);
        mv.visitInsn(Opcodes.POP);
        mv.visitInsn(Opcodes.SWAP);
    }

    public static void duplicateArrayReferenceWhenValueStoredIntoCategoryTwoArray_requiresSinglePopBeforeNextInstruction(MethodVisitor mv) {
        mv.visitInsn(Opcodes.DUP2_X2);
        mv.visitInsn(Opcodes.POP2);
        mv.visitInsn(Opcodes.DUP2_X2);
        mv.visitInsn(Opcodes.DUP2_X2);
        mv.visitInsn(Opcodes.POP);
        mv.visitInsn(Opcodes.POP);
    }

    public static void duplicateArrayReferenceWhenValueStoredIntoCategoryOneArray(MethodVisitor mv) {
        duplicateValueThirdFromTopOnStackWhichIsObjectReference(mv);
    }

    public static boolean arrayModification(int opcode) {
        return categoryOneArrayModification(opcode) || categoryTwoArrayModification(opcode);
    }

    /**
     * cf. https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-2.html#jvms-2.11.1-320
     */
    public static boolean categoryTwoArrayModification(int opcode) {
        return opcode == Opcodes.DASTORE
                || opcode == Opcodes.LASTORE;
    }

    /**
     * cf. https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-2.html#jvms-2.11.1-320
     */
    public static boolean categoryOneArrayModification(int opcode) {
        return opcode == Opcodes.BASTORE
                || opcode == Opcodes.IASTORE
                || opcode == Opcodes.AASTORE
                || opcode == Opcodes.CASTORE
                || opcode == Opcodes.FASTORE
                || opcode == Opcodes.SASTORE;
    }

    public static int decideFlagsForClassWriterBasedOnClassJavaVersion(int classMajorVersion) {
        if (classMajorVersion <= 49) {
            return ClassWriter.COMPUTE_MAXS;
        } else {
            return ClassWriter.COMPUTE_FRAMES;
        }

    }

    public static int readClassMajorVersion(byte[] classFileBuffer) throws IllegalClassFormatException {
        try (InputStream in = new ByteArrayInputStream(classFileBuffer);
             DataInputStream data = new DataInputStream(in)) {
            if (0xCAFEBABE != data.readInt()) {
                throw new IllegalClassFormatException("Invalid header");
            }
            int minor = data.readUnsignedShort();
            int major = data.readUnsignedShort();
            return major;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void verifyClasses(ClassWriter writer) {
        try {
            CheckClassAdapter.verify(new ClassReader(writer.toByteArray()), false, new PrintWriter(System.out));
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static int stackTraceDepthWithoutIgnored(StackTraceElement[] stackTrace) {
        int sum = 0;
        for (StackTraceElement stackTraceElement : stackTrace) {
            if (!CaptureStackTraceDepthTransformer.ignored(stackTraceElement.getClassName())) {
                sum++;
            }
        }
        return sum;
    }
}
