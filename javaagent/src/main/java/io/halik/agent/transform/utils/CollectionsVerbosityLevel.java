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
package io.halik.agent.transform.utils;

import com.google.common.base.Predicate;

import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Iterables.any;
import static java.util.Arrays.asList;

public enum CollectionsVerbosityLevel {
    NORMAL,
    DEBUG(asList(
            "org/mockito",
            "org/hamcrest",
            "org/msgpack")),
    TRACE(asList(
            "org/springframework/asm",
            "org/objectweb/asm",
            "org/objenesis")),
    ALL;
    private final List<String> excludedPackages;

    CollectionsVerbosityLevel() {
        this(Collections.<String>emptyList());
    }

    CollectionsVerbosityLevel(List<String> excludedPackages) {
        this.excludedPackages = excludedPackages;
    }

    public boolean filtered(String className) {
        for (CollectionsVerbosityLevel verbosityLevel : asList(ALL, TRACE, DEBUG, NORMAL)) {
            if (verbosityLevel == this) {
                return false;
            }
            if (any(verbosityLevel.excludedPackages, startsWith(className))) {
                return true;
            }
        }
        return false;
    }

    private Predicate<String> startsWith(final String className) {
        return new Predicate<String>() {
            @Override
            public boolean apply(String filter) {
                return className.startsWith(filter);
            }
        };
    }
}
