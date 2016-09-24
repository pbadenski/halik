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
package sandbox.io.halik.bug_5;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        int[] array = {4, 1, 6, 3, 0, 4};
        for (int i = 0; i < array.length - 1; i++) {
            int minKey = i, min = array[i];
            for (int j = i + 1; j < array.length; j++) {
                if (array[j] < min) {
                    min = array[j];
                    minKey = j;
                }
            }
            int temp = array[i];
            array[i] = min;
            array[minKey] = temp;
        }
        System.out.println(Arrays.toString(array));
    }
}