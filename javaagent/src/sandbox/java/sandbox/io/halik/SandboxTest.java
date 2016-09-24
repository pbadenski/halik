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

import com.ea.orbit.instrumentation.AgentLoader;
import io.halik.agent.capture.FlowFacade;
import org.junit.Test;
import sandbox.test.halik.FooClass;

import java.io.ByteArrayOutputStream;

public class SandboxTest {
        @Test
        public void stackTraceDepthExperiment() {
            AgentLoader.loadAgentClass(SandboxAgent.class.getName(), "test/halik");
            new FooClass().main();
        }
}
