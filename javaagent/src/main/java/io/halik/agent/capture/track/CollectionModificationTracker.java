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

import java.util.Collection;
import java.util.Set;

public class CollectionModificationTracker {
    private Set<Collection> modifiedCollectionsByReference = new ReferenceOpenHashSet<>();

    public void trackModification(Collection value) {
        modifiedCollectionsByReference.add(value);
    }

    public CollectedModifications collect() {
        Set<Collection> modifiedCollectionByReferenceToBeProcessed = modifiedCollectionsByReference;
        modifiedCollectionsByReference = new ReferenceOpenHashSet<>();
        return new CollectedModifications(modifiedCollectionByReferenceToBeProcessed);
    }

    public static class CollectedModifications {
        private final Set<Collection> collectedCollectionsByReference;

        public CollectedModifications(Set<Collection> collectedCollectionsByReference) {
            this.collectedCollectionsByReference = collectedCollectionsByReference;
        }

        public void consume(Function<Collection, Object> function) {
            for (Collection collection : collectedCollectionsByReference) {
                function.apply(collection);
            }
        }

        public int count() {
            return collectedCollectionsByReference.size();
        }
    }
}
