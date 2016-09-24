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
package io.halik.agent.capture.track.fields;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import io.halik.agent.capture.DumpingGround;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static java.lang.System.identityHashCode;

public class FieldModificationTracker {
    private Map<Integer, Integer> parentsByObjectId = Maps.newHashMap();
    private Set<Field> modifiedFields = new ObjectOpenHashSet<>();

    public void trackFieldAssignment(Object object, String fieldName, Object fieldValue) {
        if (DumpingGround.shouldBeSkippedByCapture(fieldValue)) {
            return;
        }
        modifiedFields.add(new Field(object, fieldName, fieldValue));
        parentsByObjectId.put(identityHashCode(fieldValue), identityHashCode(object));
    }

    public CollectedModifications collect() {
        Set<Field> modifiedFieldsToBeProcessed = modifiedFields;
        modifiedFields = new ReferenceOpenHashSet<>();
        return new CollectedModifications(modifiedFieldsToBeProcessed);
    }

    public static class CollectedModifications {
        private final Set<Field> collectedFieldsByReference;

        public CollectedModifications(Set<Field> collectedFieldsByReference) {
            this.collectedFieldsByReference = collectedFieldsByReference;
        }

        public void consume(Function<Field, Object> function) {
            for (Field field : collectedFieldsByReference) {
                function.apply(field);
            }
        }

        public int count() {
            return collectedFieldsByReference.size();
        }
    }

}
