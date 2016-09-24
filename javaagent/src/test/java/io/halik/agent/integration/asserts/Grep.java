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
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Iterables.tryFind;

public class Grep {
    public static Optional<JSONObject> tryFindInLine(String bucket, JSONObject object, Predicate<JSONObject> usingPredicate) {
        List<JSONObject> allInBucket = Collections.emptyList();
        if (object.has(bucket)){
            allInBucket = JSONExtensions.toList(object.getJSONArray(bucket));
        }
        return tryFind(allInBucket, usingPredicate);
    }

    public static Predicate<JSONObject> byName(final String name) {
        return new Predicate<JSONObject>() {
            @Override
            public boolean apply(JSONObject variable) {
                if (!variable.has("n")) {
                    return false;
                }
                return variable.get("n").equals(name);
            }
        };
    }
}
