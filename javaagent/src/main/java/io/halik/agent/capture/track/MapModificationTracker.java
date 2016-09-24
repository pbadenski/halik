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
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.util.Map;
import java.util.Set;

public class MapModificationTracker {
    private Set<Map> modifiedMapsByReference = new ReferenceOpenHashSet<>();

    public void trackModification(Map value) {
        modifiedMapsByReference.add(value);
    }

    public CollectedModifications collect() {
        Set<Map> modifiedMapsByReferenceToBeProcessed = modifiedMapsByReference;
        modifiedMapsByReference = new ReferenceOpenHashSet<>();
        return new CollectedModifications(modifiedMapsByReferenceToBeProcessed);
    }

    public static class CollectedModifications {
        private final Set<Map> collectedMapsByReference;

        public CollectedModifications(Set<Map> collectedMapsByReference) {
            this.collectedMapsByReference = collectedMapsByReference;
        }

        public void consume(Function<Map, Object> function) {
            for (Map map : collectedMapsByReference) {
                function.apply(map);
            }
        }

        public int count() {
            return collectedMapsByReference.size();
        }
    }
}
