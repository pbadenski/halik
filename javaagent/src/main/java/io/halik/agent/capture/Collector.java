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
package io.halik.agent.capture;

import com.google.common.base.Function;
import io.halik.agent.capture.track.ArrayModificationTracker;
import io.halik.agent.capture.track.CollectionModificationTracker;
import io.halik.agent.capture.track.MapModificationTracker;
import io.halik.agent.capture.track.fields.Field;
import io.halik.agent.capture.track.fields.FieldModificationTracker;
import io.halik.agent.capture.track.variables.Variable;
import io.halik.agent.capture.track.variables.VariableDefinitionLookup;
import io.halik.agent.capture.track.variables.VariableModificationTracker;
import io.halik.agent.capture.write.MessagePackWriter;

import java.util.Collection;
import java.util.Map;

public class Collector {
    private final VariableModificationTracker variableModificationTracker;
    private final FieldModificationTracker fieldModificationTracker;
    private final ArrayModificationTracker arrayModificationTracker;
    private final VariableDefinitionLookup variableDefinitionLookup;
    private final MapModificationTracker mapModificationTracker;
    private final CollectionModificationTracker collectionModificationTracker;

    public Collector(VariableModificationTracker variableModificationTracker,
                     VariableDefinitionLookup variableDefinitionLookup,
                     FieldModificationTracker fieldModificationTracker, ArrayModificationTracker arrayModificationTracker,
                     CollectionModificationTracker collectionModificationTracker,
                     MapModificationTracker mapModificationTracker) {
        this.variableModificationTracker = variableModificationTracker;
        this.collectionModificationTracker = collectionModificationTracker;
        this.fieldModificationTracker = fieldModificationTracker;
        this.arrayModificationTracker = arrayModificationTracker;
        this.variableDefinitionLookup = variableDefinitionLookup;
        this.mapModificationTracker = mapModificationTracker;
    }

    class CollectedObjects {
        private final VariableModificationTracker.CollectedModifications collectedVariableModifications;
        private final CollectionModificationTracker.CollectedModifications collectedCollectionModifications;
        private final MapModificationTracker.CollectedModifications collectedMapModifications;
        private final ArrayModificationTracker.CollectedModifications collectedArrayModifications;
        private final FieldModificationTracker.CollectedModifications collectedFieldModifications;

        public CollectedObjects(VariableModificationTracker.CollectedModifications collectedVariableModifications,
                                ArrayModificationTracker.CollectedModifications collectedArrayModifications,
                                CollectionModificationTracker.CollectedModifications collectedCollectionModifications,
                                MapModificationTracker.CollectedModifications collectedMapModifications,
                                FieldModificationTracker.CollectedModifications collectedFieldModifications) {
            this.collectedVariableModifications = collectedVariableModifications;
            this.collectedCollectionModifications = collectedCollectionModifications;
            this.collectedMapModifications = collectedMapModifications;
            this.collectedArrayModifications = collectedArrayModifications;
            this.collectedFieldModifications = collectedFieldModifications;
        }

        int count() {
            return this.collectedVariableModifications.count() +
                    this.collectedArrayModifications.count() +
                    this.collectedCollectionModifications.count() +
                    this.collectedFieldModifications.count() +
                    this.collectedMapModifications.count();
        }

        void consume(final MessagePackWriter printer) {
            this.collectedVariableModifications.consume(new Function<Variable, Object>() {
                @Override
                public Object apply(Variable variable) {
                    printer.writeVariable(variable);
                    return null;
                }
            });
            this.collectedArrayModifications.consume(new Function<Object, Object>() {

                @Override
                public Object apply(Object value) {
                    printer.writeArray(value);
                    return null;
                }
            });
            this.collectedCollectionModifications.consume(new Function<Collection, Object>() {
                @Override
                public Object apply(Collection value) {
                    printer.writeCollection(value);
                    return null;
                }
            });
            this.collectedMapModifications.consume(new Function<Map, Object>() {
                @Override
                public Object apply(Map value) {
                    printer.writeMap(value);
                    return null;
                }
            });
            this.collectedFieldModifications.consume(new Function<Field, Object>() {
                @Override
                public Object apply(Field field) {
                    printer.writeField(field);
                    return null;
                }
            });
        }
    }

    CollectedObjects collect(final int classIndex, final int methodIndex, int atLabel) {
        return new CollectedObjects(
                variableModificationTracker
                        .collect(variableDefinitionLookup, classIndex, methodIndex, atLabel),
                arrayModificationTracker.collect(),
                collectionModificationTracker.collect(),
                mapModificationTracker.collect(),
                fieldModificationTracker.collect());

    };
}
