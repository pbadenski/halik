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

public class ArrayOfChars {
    public static int FIRST_REFERENCE;
    public static int SECOND_REFERENCE;

    public void main() {
        char[] arrayOfChars = new char[] { 'a', 'b'};
        FIRST_REFERENCE = System.identityHashCode(arrayOfChars);
        arrayOfChars[0] = 'c';
        arrayOfChars = new char[] {'d', 'e', 'f'};
        SECOND_REFERENCE = System.identityHashCode(arrayOfChars);
        arrayOfChars = null;
    }
}
