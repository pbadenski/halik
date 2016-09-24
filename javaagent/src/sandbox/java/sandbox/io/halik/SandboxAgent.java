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

import java.lang.instrument.Instrumentation;

public class SandboxAgent {
    public static void agentmain(String agentArguments,
                                 Instrumentation instrumentation) {
        premain(agentArguments, instrumentation);
    }

    public static void premain(String agentArguments,
                               Instrumentation instrumentation) {
        SandboxFlow instance = SandboxFlow.INSTANCE;
        instrumentation.addTransformer(new SandboxClassTransformer());
    }
}
