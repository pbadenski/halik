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

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.Assertions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Iterables.tryFind;
import static io.halik.agent.integration.asserts.Grep.byName;
import static io.halik.agent.integration.asserts.Grep.tryFindInLine;

public class LineAssert extends AbstractAssert<LineAssert, JSONObject> {

    private final int line;

    protected LineAssert(int line, JSONObject lineObject) {
        super(lineObject, LineAssert.class);
        this.line = line;
    }

    public Optional<JSONObject> hasVariable(String name) {
        Optional<JSONObject> variable = tryFindInLine("sn", actual, byName(name));

        failIfVariableNotPresent(name, variable);
        return variable;
    }


    public Optional<JSONObject> doesNotHaveVariable(String name) {
        Optional<JSONObject> variable = tryFindInLine("sn", actual, byName(name));

        failIfVariablePresent(name, variable);
        return variable;
    }

    public void hasVariable(String name, Object expectedValue) {
        Optional<JSONObject> variable = hasVariable(name);
        failIfValuesAreDifferent(name, extractValue(variable.get()), expectedValue);
    }

    private Object extractValue(JSONObject variable) {
        Object actualValue = variable.get("v");
        if (actualValue instanceof JSONArray) {
            actualValue = JSONExtensions.toList((JSONArray) actualValue);
        } else if (actualValue == JSONObject.NULL) {
            actualValue = null;
        }
        return actualValue;
    }

    public AbstractObjectAssert<?, Object> hasReference(int reference) {
        JSONObject object = tryFindInLineById(reference, "sn");
        if (object.has("v")) {
            return Assertions.assertThat(extractValue(object));
        }
        return null;
    }

    public void hasReference(int id, Object expectedValue) {
        JSONObject variable = tryFindInLineById(id, "sn");

        failIfValuesAreDifferent(String.valueOf(id), extractValue(variable), expectedValue);
    }

    public void hasField(final String name, Object expectedValue, Object parentId) {
        Optional<JSONObject> optionalVariable =
                tryFindInLine("sn", actual, byName(name));
        failIfVariableNotPresent(name, optionalVariable);
        JSONObject variable = optionalVariable.get();
        if (!Objects.equals(variable.get("o"), parentId)) {
            failWithMessage("Line %d: Expected field `%s` to have parentId equal to: <%s>, but was: <%s>\n %s",
                    line, name, parentId, variable.get("id"), actual.toString(2));
        }

        failIfValuesAreDifferent(String.valueOf(name), extractValue(variable), expectedValue);
    }

    public void hasStackTraceDepth(int expectedStackTraceDepth) {
        int actualStackTraceDepth = actual.getInt("sd");
        if (actualStackTraceDepth != expectedStackTraceDepth) {
            failWithMessage("Line %d: Expected stack trace depth to be equal to <%s>, but was: <%s>\n %s",
                    line, expectedStackTraceDepth, actualStackTraceDepth, actual.toString(2));
        }
    }

    public static class SnapshotAssert extends AbstractListAssert<SnapshotAssert, List<JSONObject>, JSONObject> {

        protected SnapshotAssert(List<JSONObject> actual) {
            super(actual, SnapshotAssert.class);
        }
    }

    public SnapshotAssert snapshot() {
        return new SnapshotAssert(JSONExtensions.<JSONObject>toList(actual.getJSONArray("sn")));
    }

    private void failIfValuesAreDifferent(String value, Object actualValue, Object expectedValue) {
        boolean areEqual;
        if (actualValue instanceof Iterable && expectedValue instanceof Iterable) {
            areEqual = Iterables.elementsEqual((Iterable)actualValue, (Iterable)expectedValue);
        }
        else {
            areEqual = Objects.deepEquals(actualValue, expectedValue);
        }
        if (!areEqual) {
            failWithMessage("Line %d: Expected variable `%s` to be: <%s>, but was: <%s>\n %s",
                    line, value, expectedValue, actualValue, actual.toString(2));
        }
    }

    private void failIfVariableNotPresent(String name, Optional<JSONObject> variable) {
        if (!variable.isPresent()) {
            failWithMessage("Variable with name <%s> wasn't found in line:\n %s",
                    name, actual.toString(2));
        }
    }

    private void failIfVariablePresent(String name, Optional<JSONObject> variable) {
        if (variable.isPresent()) {
            failWithMessage("Variable with name <%s> not expected in line:\n %s",
                    name, actual.toString(2));
        }
    }

    private JSONObject tryFindInLineById(final int id, String bucket) {
        Optional<JSONObject> variable = tryFind(JSONExtensions.<JSONObject>toList(actual.getJSONArray(bucket)),
                new Predicate<JSONObject>() {
                    @Override
                    public boolean apply(JSONObject variable) {
                        return variable.get("id").equals(id);
                    }
                });
        if (!variable.isPresent()) {
            failWithMessage("Variable with id <%s> wasn't found in line:\n %s",
                    id, actual.toString(2));
        }
        return variable.get();
    }
}
