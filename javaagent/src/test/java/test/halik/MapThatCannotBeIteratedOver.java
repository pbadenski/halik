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
package test.halik;

import java.util.*;

public class MapThatCannotBeIteratedOver {
    public static Integer REFERENCE = null;
    class ThatMap extends HashMap {
        @Override
        public Set<Entry> entrySet() {
            return new HashSet<Entry>() {
                @Override
                public Iterator<Entry> iterator() {
                    throw new RuntimeException("can't iterate");
                }
            };
        }
    }

    public void main() {
        ThatMap thatMap = new ThatMap();
        REFERENCE = System.identityHashCode(thatMap);
        thatMap.put("foo", "bar");
    }
}
