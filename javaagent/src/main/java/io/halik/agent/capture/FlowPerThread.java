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

import io.halik.agent.BytecodeUtilitiesDumpingGround;
import io.halik.agent.capture.track.ArrayModificationTracker;
import io.halik.agent.capture.track.CollectionModificationTracker;
import io.halik.agent.capture.track.MapModificationTracker;
import io.halik.agent.capture.track.fields.FieldModificationTracker;
import io.halik.agent.capture.track.variables.VariableDefinitionLookup;
import io.halik.agent.capture.track.variables.VariableModificationTracker;
import io.halik.agent.output.JSONDirectBufferPacker;
import io.halik.agent.output.SaveTask;
import org.msgpack.MessagePack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

class FlowPerThread {
    private Flow flow;
    private final WritableByteChannel FLOW_OUTPUT_CHANNEL;
    private final WritableByteChannel SNAPSHOT_OUTPUT_CHANNEL;

    private final ExecutorService writers;

    private JSONDirectBufferPacker flowPacker;
    private JSONDirectBufferPacker snapshotPacker;

    final VariableModificationTracker variableTracker = new VariableModificationTracker();
    final FieldModificationTracker fieldModificationTracker = new FieldModificationTracker();
    final ArrayModificationTracker arrayModificationTracker = new ArrayModificationTracker();
    final CollectionModificationTracker collectionModificationTracker = new CollectionModificationTracker();
    final MapModificationTracker mapModificationTracker = new MapModificationTracker();
    final TagTracker tagTracker = new TagTracker();

    final Collector collector;

    private int step = 0;
    private int entriesWithinCaptureCode = 0;
    private int stackTraceDepth = -1;

    private JSONDirectBufferPacker.FlushBuffer flushBufferTask(final WritableByteChannel outputChannel) {
        return new JSONDirectBufferPacker.FlushBuffer() {
            @Override
            public void flush(ByteBuffer buffer) {
                writers.execute(new SaveTask(outputChannel, buffer));
            }
        };
    }

    public FlowPerThread(Flow flow, MessagePack messagePack, VariableDefinitionLookup variableDefinitionLookup, Runtime runtime) {
        this.flow = flow;
        final Thread thisFlowThread = Thread.currentThread();
        if (!thisFlowThread.getName().startsWith("$halik$")) {
            registerCleanupOnShutdown(runtime);
        }
        collector = new Collector(
                variableTracker, variableDefinitionLookup, fieldModificationTracker,
                arrayModificationTracker, collectionModificationTracker, mapModificationTracker
        );
        FLOW_OUTPUT_CHANNEL = initializeFlowOutputChannel();
        SNAPSHOT_OUTPUT_CHANNEL = initializeSnapshotOutputChannel();
        flowPacker = new JSONDirectBufferPacker(messagePack, flushBufferTask(flowOutputChannel()));
        snapshotPacker = new JSONDirectBufferPacker(messagePack, flushBufferTask(snapshotOutputChannel()));
        writers = flow.createWritersExecutorService();
    }

    private void registerCleanupOnShutdown(Runtime runtime) {
        try {
            runtime.addShutdownHook(new Thread(new Cleanup()));
        } catch (IllegalStateException e) {
            if (e.getMessage().equals("Shutdown in progress")) {
                // ignore, ehh Java why there's no way to see if shutdown is in progress :(
            } else {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private WritableByteChannel initializeFlowOutputChannel() {
        if (flow.__TEST_ONLY_OVERRIDE_FLOW_OUTPUT_CHANNEL != null) {
            return flow.__TEST_ONLY_OVERRIDE_FLOW_OUTPUT_CHANNEL;
        }
        return flow.sessionFileOutputChannel("flow");
    }

    private WritableByteChannel initializeSnapshotOutputChannel() {
        if (flow.__TEST_ONLY_OVERRIDE_SNAPSHOT_OUTPUT_CHANNEL != null) {
            return flow.__TEST_ONLY_OVERRIDE_SNAPSHOT_OUTPUT_CHANNEL;
        }
        return flow.sessionFileOutputChannel("snapshot");
    }

    private WritableByteChannel flowOutputChannel() {
        if (flow.__TEST_ONLY_OVERRIDE_FLOW_OUTPUT_CHANNEL != null) {
            return flow.__TEST_ONLY_OVERRIDE_FLOW_OUTPUT_CHANNEL;
        }
        return FLOW_OUTPUT_CHANNEL;
    }

    private WritableByteChannel snapshotOutputChannel() {
        if (flow.__TEST_ONLY_OVERRIDE_SNAPSHOT_OUTPUT_CHANNEL != null) {
            return flow.__TEST_ONLY_OVERRIDE_SNAPSHOT_OUTPUT_CHANNEL;
        }
        return SNAPSHOT_OUTPUT_CHANNEL;
    }

    JSONDirectBufferPacker flowPacker() {
        return flowPacker;
    }

    JSONDirectBufferPacker snapshotPacker() {
        return snapshotPacker;
    }

    int nextStep() {
        return step++;
    }

    void disableCaptureInCaseJavaAgentCallsClientCode() {
        entriesWithinCaptureCode++;
    }

    boolean capturingStateDisabled() {
        return entriesWithinCaptureCode >= 1;
    }

    boolean capturingLinesDisabled() {
        return entriesWithinCaptureCode > 1;
    }

    void reenableCapture() {
        entriesWithinCaptureCode--;
    }

    void increase() {
        if (stackTraceDepth == -1) {
            fetch(3);
        } else {
            stackTraceDepth++;
        }
    }

    void decrease() {
        stackTraceDepth--;
    }

    void fetch(int subtractInternalCalls) {
        stackTraceDepth
                = BytecodeUtilitiesDumpingGround.stackTraceDepthWithoutIgnored(new Throwable().getStackTrace()) - (subtractInternalCalls + 1);
    }

    public int stackTraceDepth() {
        return stackTraceDepth;
    }

    private class Cleanup implements Runnable {
        @Override
        public void run() {
            try {
                flushPackers();
                shutdownExecutors();
                closeChannels();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void closeChannels() throws IOException {
            if (flowOutputChannel() != null) {
                flowOutputChannel().close();
            }
            if (snapshotOutputChannel() != null) {
                snapshotOutputChannel().close();
            }
        }

        private void shutdownExecutors() throws InterruptedException {
            writers.shutdown();
            writers.awaitTermination(30, TimeUnit.SECONDS);
        }

        private void flushPackers() throws IOException {
            flowPacker.flush();
            snapshotPacker.flush();
        }
    }
}
