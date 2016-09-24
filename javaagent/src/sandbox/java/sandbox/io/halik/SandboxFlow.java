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
package sandbox.io.halik;

public class SandboxFlow {
    static final SandboxFlow INSTANCE = new SandboxFlow();
    public static final String BYTECODE_TYPE_NAME = "sandbox/io/halik/SandboxFlow";

    public int stackTraceDepth = 0;

    public static void enter() {
        INSTANCE.stackTraceDepth += 1;
    }

    public static void exit() {
        INSTANCE.stackTraceDepth -= 1;
    }
}
