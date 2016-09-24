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
package io.halik.agent.capture;

import java.util.Collection;

public class DumpingGround {
    public static boolean shouldBeSkippedByCapture(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Collection
            && value.getClass().getName().startsWith("org.hibernate")) {
            return true;
        }
        return false;
    }

    public static long uniqueMethodIdentifier(int classIndex, int methodIndex) {
        return szudzikPairing(classIndex, methodIndex);
    }

    private static long szudzikPairing(long a, long b) {
        return a >= b ? a * a + a + b : a + b * b;
    }
}
