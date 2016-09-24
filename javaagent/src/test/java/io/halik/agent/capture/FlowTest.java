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
package io.halik.agent.capture;

import com.google.common.reflect.AbstractInvocationHandler;
import com.google.common.reflect.Reflection;
import io.halik.agent.capture.track.variables.VariableDefinitionLookup;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.mockito.ArgumentCaptor;
import org.msgpack.MessagePack;
import org.msgpack.packer.BufferPacker;

import java.io.IOException;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

public class FlowTest {
    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().mute().enableLog();

    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().mute();

    @Test
    public void shouldStopSessionCaptureOnException() throws Exception {
        MessagePack messagePack = mock(MessagePack.class);
        BufferPacker bufferPacker = mock(BufferPacker.class);
        when(messagePack.createBufferPacker()).thenReturn(bufferPacker);
        when(bufferPacker.writeMapBegin(anyInt())).thenThrow(new IOException());

        Flow flow = new Flow(messagePack, new VariableDefinitionLookup(), mock(Runtime.class));
        flow.__redirectOutputToJSONObject();

        flow.captureLineExecution(0, 0, 0, 0);
        flow.captureLineExecution(0, 0, 0, 0);
        flow.captureLineExecution(0, 0, 0, 0);

        verify(messagePack, times(0)).createBufferPacker();
    }

    @Test
    public void shouldChangeFinalMessageToIndicateExceptionOccurred() throws Exception {
        Runtime runtime = mock(Runtime.class);
        ArgumentCaptor<Thread> shutdownHookCaptor = ArgumentCaptor.forClass(Thread.class);

        Flow flow = new Flow(null, null, runtime);
        flow.__redirectOutputToJSONObject();
        flow.registerHookToPrintSessionURLOnExit();
        verify(runtime).addShutdownHook(shutdownHookCaptor.capture());

        flow.onExceptionDuringCapture(new Exception(), Thread.currentThread());
        shutdownHookCaptor.getValue().run();

        assertThat(systemOutRule.getLog())
                .startsWith("!!! Exception occurred while running Halik. " +
                        "Halik will be disabled " +
                        "and your program will continue to work correctly.\n");
    }
}