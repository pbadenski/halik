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

public class ArrayOfBooleans {
    public static int FIRST_REFERENCE;
    public static int SECOND_REFERENCE;

    public void main() {
        boolean[] arrayOfBooleans = new boolean[] { true, false };
        FIRST_REFERENCE = System.identityHashCode(arrayOfBooleans);
        arrayOfBooleans[0] = false;
        arrayOfBooleans = new boolean[] {false, false, false};
        SECOND_REFERENCE = System.identityHashCode(arrayOfBooleans);
        arrayOfBooleans = null;
    }
}
