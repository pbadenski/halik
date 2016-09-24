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

public class ListOfBooleans {
    public static int FIRST_REFERENCE;

    public void main() {
        List<java.lang.Boolean> listOfBooleans = new ArrayList<>();
        FIRST_REFERENCE = System.identityHashCode(listOfBooleans);
        Collections.addAll(listOfBooleans, java.lang.Boolean.TRUE, java.lang.Boolean.FALSE);
        listOfBooleans.add(false);
        listOfBooleans.addAll(0, Arrays.asList(false));
        Collections.sort(listOfBooleans);
        listOfBooleans.remove(0);
        listOfBooleans.removeAll(Arrays.asList(false));
        listOfBooleans = null;
    }
}
