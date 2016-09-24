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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.google.common.collect.Iterables.tryFind;

public class FlowAssert extends AbstractAssert<FlowAssert, JSONArray> {

    protected FlowAssert(JSONArray actual) {
        super(actual, FlowAssert.class);
    }

    public static FlowAssert assertThat(JSONArray flow) {
        return new FlowAssert(flow);
    }

    public LineAssert atLine(int line) {
        try {
            Optional<JSONObject> objectAtLine = objectAtLine(line);
            if (!objectAtLine.isPresent()) {
                failWithMessage("Line %s not found in JSON:\n%s",
                        line, actual.toString(2));
                return null;
            }
            return new LineAssert(line, objectAtLine.get());
        } catch (JSONException e) {
            failWithMessage("Exception \"%s\" was reported for line %s in JSON:\n%s",
                    e.getMessage(), line, actual.toString(2));
            return null;
        }
    }

    public MultiLineAssert allAtLine(int line) {
        try {
            Iterable<JSONObject> objectsAtLine = objectsAtLine(line);
            if (Iterables.isEmpty(objectsAtLine)) {
                failWithMessage("Line %s not found in JSON:\n%s",
                        line, actual.toString(2));
                return null;
            }
            return new MultiLineAssert(line, objectsAtLine);
        } catch (JSONException e) {
            failWithMessage("Exception \"%s\" was reported for line %s in JSON:\n%s",
                    e.getMessage(), line, actual.toString(2));
            return null;
        }
    }

    private Iterable<JSONObject> objectsAtLine(final int line) {
        List<JSONObject> lines = JSONExtensions.toList(actual);
        return Iterables.filter(lines, new Predicate<JSONObject>() {
            @Override
            public boolean apply(JSONObject input) {
                return input.get("l").equals(line);
            }
        });
    }

    private Optional<JSONObject> objectAtLine(final int line) {
        List<JSONObject> lines = JSONExtensions.toList(actual);
        return tryFind(lines, new Predicate<JSONObject>() {
            @Override
            public boolean apply(JSONObject input) {
                return input.get("l").equals(line);
            }
        });
    }

}
