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
package io.halik.agent.transform;

import com.google.common.base.Predicate;
import io.halik.agent.capture.track.variables.TypeInModification;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.objectweb.asm.MethodVisitor;

import java.util.Map;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.filter;

public class VariableStateCapturer {
    private Map<Integer, TypeInModification> modifiedVariablesByIndex = new Int2ObjectOpenHashMap<>();

    public TypeInModification trackOnVariableModification(int index, TypeInModification type) {
        return this.modifiedVariablesByIndex.put(index, type);
    }

    public void captureState(MethodVisitor mv, String methodName) {
        for (Map.Entry<Integer, TypeInModification> entry :
                filter(modifiedVariablesByIndex.entrySet(), not(withinConstructorAndAccessingThis(methodName)))) {
            entry.getValue().captureVariableState(entry.getKey(), mv);
        }
        modifiedVariablesByIndex = new Int2ObjectOpenHashMap<>();
    }

    // skip capturing "this" to avoid "uninitializedThis" error.. so far this is the best I can come up with
    private Predicate<Map.Entry<Integer, TypeInModification>> withinConstructorAndAccessingThis(final String methodName) {
        return new Predicate<Map.Entry<Integer, TypeInModification>>() {
            @Override
            public boolean apply(Map.Entry<Integer, TypeInModification> input) {
                return methodName.startsWith("<init>") && input.getKey().equals(0);
            }
        };
    }
}
