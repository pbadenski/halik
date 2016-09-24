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
package io.halik.agent.capture.write;

import org.msgpack.packer.Packer;
import org.msgpack.util.json.JSONPacker;

import java.io.IOException;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static java.lang.System.identityHashCode;

public class JavaObjectsPacker {
    public static boolean isPrimitive(Object object) {
        return object instanceof Integer
                || object instanceof Byte
                || object instanceof Short
                || object instanceof Long
                || object instanceof Double
                || object instanceof BigDecimal
                || object instanceof BigInteger
                || object instanceof Float
                || object instanceof String
                || object instanceof Character
                || object instanceof Boolean;
    }

    static void packArrayElement(Object element, Packer packer) throws IOException {
        if (element == null) {
            packer.writeNil();
        } else if (isPrimitive(element)) {
            packPrimitive(element, packer);
        } else if (element.getClass().isArray()) {
            packArrayRef(element, packer);
        } else {
            packObject(element, packer);
        }
    }

    static void packArrayOfValuesOrRefs(Object value, Packer packer) throws IOException {
        if (value instanceof String[]) {
            String[] array = (String[]) value;
            packer.writeArrayBegin(array.length);
            for (String each : array) {
                packer.write(each);
            }
            packer.writeArrayEnd();
        } else if (value instanceof int[]) {
            int[] array = (int[]) value;
            packer.writeArrayBegin(array.length);
            for (int each : array) {
                packer.write(each);
            }
            packer.writeArrayEnd();
        } else if (value instanceof char[]) {
            char[] array = (char[]) value;
            packer.writeArrayBegin(array.length);
            for (char each : array) {
                packer.write(each);
            }
            packer.writeArrayEnd();
        } else if (value instanceof byte[]) {
            byte[] array = (byte[]) value;
            packer.writeArrayBegin(array.length);
            for (byte each : array) {
                packer.write(each);
            }
            packer.writeArrayEnd();
        } else if (value instanceof short[]) {
            short[] array = (short[]) value;
            packer.writeArrayBegin(array.length);
            for (short each : array) {
                packer.write(each);
            }
            packer.writeArrayEnd();
        } else if (value instanceof long[]) {
            long[] array = (long []) value;
            packer.writeArrayBegin(array.length);
            for (long each : array) {
                packer.write(each);
            }
            packer.writeArrayEnd();
        } else if (value instanceof double[]) {
            double[] array = (double[]) value;
            packer.writeArrayBegin(array.length);
            for (double each : array) {
                packer.write(each);
            }
            packer.writeArrayEnd();
        } else if (value instanceof float[]) {
            float[] array = (float[]) value;
            packer.writeArrayBegin(array.length);
            for (float each : array) {
                packer.write(each);
            }
            packer.writeArrayEnd();
        } else if (value instanceof boolean[]) {
            boolean[] array = (boolean[]) value;
            packer.writeArrayBegin(array.length);
            for (boolean each : array) {
                packer.write(each);
            }
            packer.writeArrayEnd();
        } else if (value instanceof Object[]) {
            Object[] array = (Object[]) value;
            packer.writeArrayBegin(array.length);
            for (Object element : array) {
                packArrayElement(element, packer);
            }
            packer.writeArrayEnd();
        } else {
            throw new RuntimeException("Unknown array: " + value.getClass());
        }
    }

    private static boolean extraObjectToStringMetadata(Object value) {
        return value instanceof Class || value instanceof Type || value instanceof Enum  || value instanceof Member;
    }

    private static void packObject(Object value, Packer packer) throws IOException {
        packer.writeMapBegin(3 + (extraObjectToStringMetadata(value) ? 1 : 0));
        packObjectContents(value, packer);
        packer.writeMapEnd();
    }

    public static void packNamedObject(Object value, String name, Packer packer) throws IOException {
        packer.writeMapBegin(4 + (extraObjectToStringMetadata(value) ? 1 : 0));
        packer.write("n").write(name);
        packObjectContents(value, packer);
        packer.writeMapEnd();
    }

    public static void packFieldObject(int objectId, Object value, String name, Packer packer) throws IOException {
        packer.writeMapBegin(5 + (extraObjectToStringMetadata(value) ? 1 : 0));
        packer.write("o").write(objectId);
        packer.write("n").write(name);
        packObjectContents(value, packer);
        packer.writeMapEnd();
    }

    private static void packObjectContents(Object value, Packer packer) throws IOException {
        packer.write("t").write("O");
        packer.write("#c").write((value != null) ? value.getClass().getName() : null);
        packer.write("id").write(identityHashCode(value));
        if (extraObjectToStringMetadata(value)) {
            packer.write("#toS").write(value != null ? value.toString() : null);
        }
    }

    private static void packPrimitive(Object value, Packer packer) throws IOException {
        packer.writeMapBegin(2);
        packPrimitiveContents(value, packer);
        packer.writeMapEnd();
    }

    public static void packNamedPrimitive(Object value, String name, Packer packer) throws IOException {
        packer.writeMapBegin(3);
        packer.write("n").write(name);
        packPrimitiveContents(value, packer);
        packer.writeMapEnd();
    }

    public static void packFieldPrimitive(Integer objectId, Object value, String name, Packer packer) throws IOException {
        packer.writeMapBegin(4);
        packer.write("o").write(objectId);
        packer.write("n").write(name);
        packPrimitiveContents(value, packer);
        packer.writeMapEnd();
    }

    private static void packPrimitiveContents(Object value, Packer packer) throws IOException {
        packer.write("t").write("P");
        if (packer instanceof JSONPacker) {
            value = handleFloatingTypeCornerCaseForJSONPacker(value);
        }
        packer.write("v").write(value);
    }

    private static Object handleFloatingTypeCornerCaseForJSONPacker(Object value) {
        Object result = value;
        if (Double.valueOf(Double.NaN).equals(value)
                || Float.valueOf(Float.NaN).equals(value)) {
            result = "NaN";
        } else if (Double.valueOf(Double.NEGATIVE_INFINITY).equals(value)
                || Float.valueOf(Float.NEGATIVE_INFINITY).equals(value)) {
            result = "-Infinity";
        } else if (Double.valueOf(Double.POSITIVE_INFINITY).equals(value)
                || Float.valueOf(Float.POSITIVE_INFINITY).equals(value)) {
            result = "Infinity";
        }
        return result;
    }

    public static void packCollection(Collection collection, Packer packer) throws IOException {
        packer.writeMapBegin(3);
        packCollectionContents(collection, packer);
        packer.writeMapEnd();
    }

    public static void packNamedCollection(Collection collection, String name, Packer packer) throws IOException {
        boolean isClojureType = collection.getClass().getName().startsWith("clojure.lang");
        packer.writeMapBegin(3 + (isClojureType ? 1 : 0));
        packer.write("n").write(name);
        packCollectionHeader(collection, packer);
        if (isClojureType) {
            packCollectionValue(collection, packer);
        }
        packer.writeMapEnd();
    }

    public static void packFieldCollection(int objectId, Collection collection, String name, Packer packer) throws IOException {
        boolean isClojureType = collection.getClass().getName().startsWith("clojure.lang");
        packer.writeMapBegin(4 + (isClojureType ? 1 : 0));
        packer.write("o").write(objectId);
        packer.write("n").write(name);
        packCollectionHeader(collection, packer);
        if (isClojureType) {
            packCollectionValue(collection, packer);
        }
        packer.writeMapEnd();
    }

    private static void packCollectionContents(Collection collection, Packer packer) throws IOException {
        packCollectionHeader(collection, packer);
        packCollectionValue(collection, packer);
    }

    private static void packCollectionHeader(Collection collection, Packer packer) throws IOException {
        packer.write("t").write("C");
        packer.write("id").write(identityHashCode(collection));
    }

    private static void packCollectionValue(Collection collection, Packer packer) throws IOException {
        packer.write("v");
        int collectionSize;
        try {
            collectionSize = collection.size();
        } catch (Exception e) {
            packer.writeNil();
            return;
        }
        Iterator iterator;
        try {
            iterator = collection.iterator();
        } catch (Exception e) {
            packer.writeNil();
            return;
        }
        packer.writeArrayBegin(collectionSize);
        for (; iterator.hasNext(); ) {
            Object element = iterator.next();
            packArrayElement(element, packer);
        }
        packer.writeArrayEnd();
    }

    public static void packArrayRef(Object value, Packer packer) throws IOException {
        packer.writeMapBegin(2);
        packArrayHeader(value, packer);
        packer.writeMapEnd();
    }

    public static void packArray(Object value, Packer packer) throws IOException {
        packer.writeMapBegin(3);
        packArrayContents(value, packer);
        packer.writeMapEnd();
    }

    public static void packNamedArray(Object value, String name, Packer packer) throws IOException {
        packer.writeMapBegin(4);
        packer.write("n").write(name);
        packArrayContents(value, packer);
        packer.writeMapEnd();
    }

    public static void packFieldArray(Integer objectId, Object value, String name, Packer packer) throws IOException {
        packer.writeMapBegin(5);
        packer.write("o").write(objectId);
        packer.write("n").write(name);
        packArrayContents(value, packer);
        packer.writeMapEnd();
    }

    private static void packArrayContents(Object value, Packer packer) throws IOException {
        packArrayHeader(value, packer);
        packer.write("v");
        packArrayOfValuesOrRefs(value, packer);
    }

    private static void packArrayHeader(Object value, Packer packer) throws IOException {
        packer.write("t").write("A");
        packer.write("id").write(identityHashCode(value));
    }

    public static void packMap(Map value, Packer packer) throws IOException {
        packer.writeMapBegin(3);
        packMapContents(value, packer);
        packer.writeMapEnd();
    }

    private static void packMapContents(Map map, Packer packer) throws IOException {
        packMapHeader(map, packer);
        packMapValue(map, packer);
    }

    private static void packMapHeader(Map map, Packer packer) throws IOException {
        packer.write("t").write("M");
        packer.write("id").write(identityHashCode(map));
    }

    private static void packMapValue(Map map, Packer packer) throws IOException {
        packer.write("v");

        Set entrySet = null;
        try {
            entrySet = map.entrySet();
        } catch (Exception e) {
            // swallow on purpose
        }
        if (entrySet == null) {
            packer.writeNil();
        } else {
            packMapEntries(packer, entrySet);
        }
    }

    private static void packMapEntries(Packer packer, Set<Map.Entry> entrySet) throws IOException {
        boolean disableCheckUntilWeHaveBetterApproachForConcurrentListAccess = false;
        int entriesSize;
        try {
            entriesSize = entrySet.size();
        } catch (Exception e) {
            packer.writeNil();
            return;
        }
        Iterator<Map.Entry> iterator;
        try {
            iterator = entrySet.iterator();
        } catch (Exception e) {
            packer.writeNil();
            return;
        }
        packer.writeArrayBegin(entriesSize);
        for (; iterator.hasNext(); ) {
            Map.Entry element = iterator.next();
            packer.writeMapBegin(2);
            packer.write("k");
            Object key = null;
            try {
                key = element.getKey();
            } catch (Exception e) {
                // swallow on purpose
            }
            if (key == null) {
                packer.writeNil();
            } else {
                packArrayElement(key, packer);
            }
            packer.write("v");
            Object value = null;
            try {
                value = element.getValue();
            } catch (Exception e) {
                // swallow on purpose
            }
            if (value == null) {
                packer.writeNil();
            } else {
                packArrayElement(value, packer);
            }
            packer.writeMapEnd();
        }
        packer.writeArrayEnd(disableCheckUntilWeHaveBetterApproachForConcurrentListAccess);
    }

    public static void packNamedMap(Map map, String name, Packer packer) throws IOException {
        boolean isClojureType = map.getClass().getName().startsWith("clojure.lang");
        packer.writeMapBegin(3 + (isClojureType ? 1 : 0));
        packer.write("n").write(name);
        packMapHeader(map, packer);
        if (isClojureType) {
            packMapValue(map, packer);
        }
        packer.writeMapEnd();
    }

    public static void packFieldMap(int objectId, Map map, String name, Packer packer) throws IOException {
        boolean isClojureType = map.getClass().getName().startsWith("clojure.lang");
        packer.writeMapBegin(4 + (isClojureType ? 1 : 0));
        packer.write("o").write(objectId);
        packer.write("n").write(name);
        packMapHeader(map, packer);
        if (isClojureType) {
            packMapValue(map, packer);
        }
        packer.writeMapEnd();
    }
}
