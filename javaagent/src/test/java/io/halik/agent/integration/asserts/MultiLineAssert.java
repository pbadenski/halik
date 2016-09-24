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
package io.halik.agent.integration.asserts;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.assertj.core.api.AbstractAssert;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.transform;

public class MultiLineAssert extends AbstractAssert<MultiLineAssert, Iterable<JSONObject>> {

    private final int line;

    protected MultiLineAssert(int line, Iterable<JSONObject> objectsAtLine) {
        super(objectsAtLine, MultiLineAssert.class);
        this.line = line;
    }

    public MultiLineAssert hasVariable(final String name, final Object expectedValue) {
        Iterable<JSONObject> allLineObjects = Iterables.filter(actual, new Predicate<JSONObject>() {
            @Override
            public boolean apply(JSONObject input) {
                return Grep.tryFindInLine("sn", input, Grep.byName(name)).isPresent();
            }
        });
        failIfVariableNotPresent(name, allLineObjects);
        boolean expectedValueExists = Iterables.any(allLineObjects, new Predicate<JSONObject>() {
            @Override
            public boolean apply(JSONObject input) {
                Object value = extractValue(Grep.tryFindInLine("sn", input, Grep.byName(name)).get());
                return Objects.deepEquals(value, expectedValue);
            }
        });
        if (!expectedValueExists) {
            failOnMissingValue(name, expectedValue);
        }
        return this;
    }

    private Object extractValue(JSONObject variable) {
        Object actualValue = variable.get("v");
        if (actualValue instanceof JSONArray) {
            actualValue = JSONExtensions.toList((JSONArray) actualValue);
        }
        return actualValue;
    }

    private void failOnMissingValue(String name, Object expectedValue) {
        failWithMessage("Line %d: Expected variable `%s` to be: <%s>, but selected lines do not have it: \n %s",
                line, name, expectedValue, prettyString(actual));
    }

    private StringBuilder prettyString(Iterable<JSONObject> allLineObjects) {
        return Joiner.on(",").appendTo(new StringBuilder(), transform(allLineObjects, prettyString()));
    }

    private Function<JSONObject, String> prettyString() {
        return new Function<JSONObject, String>() {
            @Override
            public String apply(JSONObject input) {
                return input.toString(2);
            }
        };
    }

    private void failIfVariableNotPresent(String name, Iterable<JSONObject> lineObjects) {
        if (isEmpty(lineObjects)) {
            failWithMessage("Variable with name <%s> wasn't found in lines:\n %s",
                    name, prettyString(actual));
        }
    }

}
