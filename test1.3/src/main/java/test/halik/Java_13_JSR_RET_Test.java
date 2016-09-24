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

public class Java_13_JSR_RET_Test {
    public static void main(String[] args) {
        try {
            body();
        } catch (Exception e) {
            catchE();
        } finally {
            int i = 2;
            if (i == 1) {
                i=-1;
            }
        }
    }

    private static void body() {
        int i = 0;
    }

    private static void catchE() {
        int i = 1;
    }

}
