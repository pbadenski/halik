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
package io.halik.agent.transform.classTransformers.kotlin;

import static io.halik.agent.transform.utils.ComputeCommonSuperClass.cowardlyIsInstanceOf;

public class Rules {
    static boolean dontCaptureVariable(String name) {
        return name.startsWith("$i$f$") || name.startsWith("$i$a$") || name.endsWith("$iv");
    }

    static boolean dontCaptureField(String owner, String methodName) {
        return cowardlyIsInstanceOf(owner, "kotlin/jvm/internal/Lambda") && methodName.equals("<init>");
    }
}
