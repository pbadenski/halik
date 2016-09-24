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
package io.halik.quicksort;

public class QuickSort_Example {
  public static void main(String[] args) {
    int[] array =
        {4, 3, 12, 15, 6, 10, 14, 3};
    Arrays2.sort(array);
    System.out.print("Sorted array: [ ");
    for (int i : array) {
      System.out.print(i + " ");
    }
    System.out.print("]\n");
  }
}
