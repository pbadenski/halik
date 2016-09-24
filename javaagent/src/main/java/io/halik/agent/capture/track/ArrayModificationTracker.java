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
package io.halik.agent.capture.track;

import com.google.common.base.Function;
import io.halik.agent.capture.track.variables.Variable;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newIdentityHashSet;

public class ArrayModificationTracker {
    private Set<Object> modifiedArraysByReference = new ReferenceOpenHashSet<>();

    public void trackModification(Object value) {
        modifiedArraysByReference.add(value);
    }

    public CollectedModifications collect() {
        Set<Object> modifiedArraysByReferenceToBeProcessed = modifiedArraysByReference;
        modifiedArraysByReference = new ReferenceOpenHashSet<>();
        return new CollectedModifications(modifiedArraysByReferenceToBeProcessed);
    }

    public static class CollectedModifications {
        private final Set<Object> collectedArraysByReference;

        public CollectedModifications(Set<Object> collectedArraysByReference) {
            this.collectedArraysByReference = collectedArraysByReference;
        }

        public void consume(Function<Object, Object> function) {
            for (Object array : collectedArraysByReference) {
                function.apply(array);
            }
        }

        public int count() {
            return collectedArraysByReference.size();
        }
    }
}
