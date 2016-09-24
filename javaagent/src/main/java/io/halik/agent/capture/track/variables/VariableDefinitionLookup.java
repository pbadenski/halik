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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.Collections;
import java.util.Map;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Maps.newConcurrentMap;

public class VariableDefinitionLookup {
    public static final VariableDefinitionLookup EMPTY = new VariableDefinitionLookup() {
        @Override
        public void trackVariableDefinition(CompleteVariableDefinition completeVariableDefinition) {
        }

        @Override
        public SimpleVariableDefinition lookupVariableDefinition(long uniqueMethodIdentifier, int index, int atLine) {
            return null;
        }

        @Override
        public Iterable<Map.Entry<Range<Integer>, SimpleVariableDefinition>> lookupVariableDefinitions(long uniqueMethodIdentifier, Integer fromLabel) {
            return Collections.emptyList();
        }
    };

    private Map<Long, Map<Integer, RangeMap<Integer, SimpleVariableDefinition>>> rangedVariablesByMethod
            = newConcurrentMap();

    public void trackVariableDefinition(CompleteVariableDefinition completeVariableDefinition) {
        long longMethodSignature = completeVariableDefinition.uniqueMethodIdentifier();
        Map<Integer, RangeMap<Integer, SimpleVariableDefinition>> variablesInMethod
                = rangedVariablesByMethod.get(longMethodSignature);

        if (variablesInMethod == null) {
            variablesInMethod = new Int2ObjectOpenHashMap<>();
            rangedVariablesByMethod.put(longMethodSignature, variablesInMethod);
        }
        RangeMap<Integer, SimpleVariableDefinition> rangedVariablesAtIndex =
                variablesInMethod.get(completeVariableDefinition.getIndex());
        if (rangedVariablesAtIndex == null) {
            rangedVariablesAtIndex = TreeRangeMap.create();
            variablesInMethod.put(completeVariableDefinition.getIndex(), rangedVariablesAtIndex);
        }

        rangedVariablesAtIndex.put(
                Range.closed(
                        completeVariableDefinition.getFromLabel(),
                        completeVariableDefinition.getToLabel()),
                new SimpleVariableDefinition(
                        completeVariableDefinition.getVariableName(),
                        completeVariableDefinition.getType(),
                        completeVariableDefinition.getFromLabel(),
                        completeVariableDefinition.getIndex()));
    }

    public SimpleVariableDefinition lookupVariableDefinition(long uniqueMethodIdentifier, int index, int atLabel) {
        Map<Integer, RangeMap<Integer, SimpleVariableDefinition>> methodVariables
                = rangedVariablesByMethod.get(uniqueMethodIdentifier);
        if (methodVariables == null) {
            return null;
        }
        RangeMap<Integer, SimpleVariableDefinition> variablesAtIndexRanged = methodVariables.get(index);
        if (variablesAtIndexRanged == null) {
            return null;
        }
        return variablesAtIndexRanged.get(atLabel);
    }

    public Iterable<Map.Entry<Range<Integer>, SimpleVariableDefinition>> lookupVariableDefinitions(
            long uniqueMethodIdentifier, final Integer fromLabel) {
        Map<Integer, RangeMap<Integer, SimpleVariableDefinition>> methodVariables = rangedVariablesByMethod.get(uniqueMethodIdentifier);
        if (methodVariables == null) {
            return Collections.emptyList();
        }
        return
                filter(
                        transform(methodVariables.entrySet(), new Function<Map.Entry<Integer, RangeMap<Integer, SimpleVariableDefinition>>, Map.Entry<Range<Integer>, SimpleVariableDefinition>>() {
                            @Override
                            public Map.Entry<Range<Integer>, SimpleVariableDefinition> apply(Map.Entry<Integer, RangeMap<Integer, SimpleVariableDefinition>> input) {
                                return Iterables.tryFind(input.getValue().asMapOfRanges().entrySet(), new Predicate<Map.Entry<Range<Integer>, SimpleVariableDefinition>>() {
                                    @Override
                                    public boolean apply(Map.Entry<Range<Integer>, SimpleVariableDefinition> input) {
                                        return input.getValue().fromLabel == fromLabel;
                                    }
                                }).orNull();
                            }
                        }),
                        Predicates.notNull());
    }

}
