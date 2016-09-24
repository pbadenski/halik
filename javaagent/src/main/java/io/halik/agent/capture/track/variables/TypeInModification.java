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
package io.halik.agent.capture.track.variables;

import io.halik.agent.capture.FlowFacade;
import io.halik.agent.transform.VariableStateCapturer;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singleton;

public enum TypeInModification {
    ARRAY(Opcodes.ALOAD, singleton(Opcodes.ASTORE), "Ljava/lang/Object;"),
    INTEGER(Opcodes.ILOAD, newHashSet(Opcodes.ISTORE, Opcodes.IINC), "I"),
    FLOAT(Opcodes.FLOAD, singleton(Opcodes.FSTORE), "F"),
    DOUBLE(Opcodes.DLOAD, singleton(Opcodes.DSTORE), "D"),
    LONG(Opcodes.LLOAD, singleton(Opcodes.LSTORE), "J");

    private final int typeAccessOpcode;
    private final Set<Integer> typeModificationOpcodes;
    private String typeSignature;

    public static TypeInModification byType(Type type) {
        String descriptor = type.getDescriptor();
        if (descriptor.equals("I") || descriptor.equals("Z") || descriptor.equals("C")
                || descriptor.equals("B") || descriptor.equals("S")) {
            return INTEGER;
        }
        if (descriptor.equals("F")) {
            return FLOAT;
        }
        if (descriptor.equals("D")) {
            return DOUBLE;
        }
        if (descriptor.equals("J")) {
            return LONG;
        }
        return ARRAY;
    }

    TypeInModification(int typeAccessOpcode, Set<Integer> typeModificationOpcodes, String typeSignature) {
        this.typeAccessOpcode = typeAccessOpcode;
        this.typeModificationOpcodes = typeModificationOpcodes;
        this.typeSignature = typeSignature;
    }

    public static void afterVariableUsage(int opcode, int index, VariableStateCapturer variableStateCapturer) {
        for (TypeInModification type : TypeInModification.values()) {
            type.trackVariableUsageForType(opcode, index, variableStateCapturer);
        }
    }

    private void trackVariableUsageForType(int opcode, int index, VariableStateCapturer variableStateCapturer) {
        if (typeModificationOpcodes.contains(opcode)) {
            variableStateCapturer.trackOnVariableModification(index, this);
        }
    }

    public void captureVariableState(Integer index, MethodVisitor mv) {
        mv.visitVarInsn(typeAccessOpcode, index);
        mv.visitLdcInsn(index);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, FlowFacade.BYTECODE_TYPE_NAME, "captureVariableModification", "("+ typeSignature +"I)V", false);
    }
}
