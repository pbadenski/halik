/*
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holders nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package io.halik.agent.transform.utils;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComputeCommonSuperClass {
    private static Map<String, TypeInfo> typeInfos = new HashMap<>();

    static class TypeInfo {
        private boolean isInterface;
        private String superName;
        private String[] interfaces;

        public TypeInfo(ClassReader classReader) {
            isInterface = (classReader.getAccess() & Opcodes.ACC_INTERFACE) != 0;
            superName = classReader.getSuperName();
            interfaces = classReader.getInterfaces();
        }

        private boolean isInterface() {
            return isInterface;
        }

        private String getSuperName() {
            return superName;
        }

        public String[] getInterfaces() {
            return interfaces;
        }
    }

    private static ClassLoader classLoader = ComputeCommonSuperClass.class.getClassLoader();

    public static boolean cowardlyIsInstanceOf(String type, String superType) {
        try {
            return isInstanceOf(type, superType);
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static boolean isInstanceOf(String type, String superType) {
        return type.equals(superType)
                || computeCommonSuperClass(type, superType).equals(superType);
    }

    public static String computeCommonSuperClass(String type1, String type2) {
        try {
            return tryComputeCommonSuperClass(type1, type2);
        } catch (ComputeCommonSuperClass.ClassNotFoundException e) {
            return "java/lang/Object";
        }
    }

    private static String tryComputeCommonSuperClass(String type1, String type2) {
        if (isAnInterface(type1)) {
            if (typeImplements(type2, type1)) {
                return type1;
            }
            if (isAnInterface(type2) && typeImplements(type1, type2)) {
                return type2;
            }
            return "java/lang/Object";
        } else if (isAnInterface(type2)) {
            if (typeImplements(type1, type2)) {
                return type2;
            } else {
                return "java/lang/Object";
            }
        } else {
            List<String> ancestorsOfType1 = typeAncestors(type1);
            List<String> ancestorsOfType2 = typeAncestors(type2);
            String commonSuperClass = "java/lang/Object";

            for (int i = 0; i < Math.min(ancestorsOfType1.size(), ancestorsOfType2.size()); i++) {
                String ancestorOfType1 = ancestorsOfType1.get(i);
                String ancestorOfType2 = ancestorsOfType2.get(i);
                if (ancestorOfType1.equals(ancestorOfType2)) {
                    commonSuperClass = ancestorOfType1;
                } else {
                    break;
                }
            }
            return commonSuperClass;
        }
    }


    /**
     * @param type the internal name of a class or interface
     * @return true if the given type is an interface, false otherwise
     * @throws java.io.IOException if the bytecode of 'type' cannot be loaded
     */
    private static boolean isAnInterface(String type) {
        return typeInfo(type).isInterface();
    }

    /**
     * Returns the internal names of the ancestor classes of the given type, ordered from most abstract to least.
     *
     * @param type the internal name of a class or interface.
     * @return a List containing the ancestor classes of 'type',
     * The returned list has the following format:
     * ["type1", "type2", ... , "typeN"], where type1 is a direct subclass of Object, and typeN is 'type'
     * If 'type' is Object, an empty list is returned
     * @throws java.io.IOException if the bytecode of 'type' or of some of its ancestor class
     *                             cannot be loaded.
     */
    private static List<String> typeAncestors(String type) {
        List<String> ancestors = new ObjectArrayList<>();
        TypeInfo info = typeInfo(type);
        while (!"java/lang/Object".equals(type)) {
            ancestors.add(type);
            type = info.getSuperName();
            info = typeInfo(type);
        }
        Collections.reverse(ancestors);
        return ancestors;
    }

    /**
     * Returns true if the given type implements the given interface.
     *
     * @param type
     *            the internal name of a class or interface.
     * @param interfaceName
     *            the internal name of a interface.
     * @return true if 'type' implements directly or indirectly 'itf'
     * @throws java.io.IOException
     *             if the bytecode of 'type' or of some of its ancestor class
     *             cannot be loaded.
     */
    private static boolean typeImplements(String type, String interfaceName) {
        TypeInfo info = typeInfo(type);
        while (!"java/lang/Object".equals(type)) {
            String[] itfs = info.getInterfaces();
            for (int i = 0; i < itfs.length; ++i) {
                if (itfs[i].equals(interfaceName)) {
                    return true;
                }
            }
            for (int i = 0; i < itfs.length; ++i) {
                if (typeImplements(itfs[i], interfaceName)) {
                    return true;
                }
            }
            type = info.getSuperName();
            info = typeInfo(type);
        }
        return false;
    }

    /**
     * Returns a ClassReader corresponding to the given class or interface.
     *
     * @param type
     *            the internal name of a class or interface.
     * @return the ClassReader corresponding to 'type'.
     * @throws java.io.IOException
     *             if the bytecode of 'type' cannot be loaded.
     */
    private static TypeInfo typeInfo(final String type) {
        TypeInfo typeInfo = typeInfos.get(type);
        if (typeInfo == null) {
            typeInfo = readTypeInfo(type);
            typeInfos.put(type, typeInfo);
        }
        return typeInfo;
    }

    private static TypeInfo readTypeInfo(String type) {
        try {
            InputStream is = classLoader.getResourceAsStream(type + ".class");
            try {
                return new TypeInfo(new ClassReader(is));
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        } catch (IOException e) {
            throw new ClassNotFoundException(e);
        }
    }

    private static class ClassNotFoundException extends RuntimeException {
        public ClassNotFoundException(Exception e) {
            super(e);
        }
    }
}
