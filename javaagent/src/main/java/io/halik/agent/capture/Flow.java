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

import com.google.common.base.Optional;
import com.google.common.io.Files;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.halik.agent.AgentMain;
import io.halik.agent.capture.track.variables.CompleteVariableDefinition;
import io.halik.agent.capture.track.variables.VariableDefinitionLookup;
import io.halik.agent.capture.write.MessagePackDumpingGround;
import io.halik.agent.capture.write.MessagePackWriter;
import io.halik.agent.output.JSONDirectBufferPacker;
import io.halik.agent.output.JSONOutput;
import io.halik.agent.output.NullOutputStream;
import io.halik.agent.output.SaveTask;
import org.json.JSONArray;
import org.msgpack.MessagePack;
import org.msgpack.util.json.JSON;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class Flow {
    private final long BASE_TIMESTAMP = System.currentTimeMillis();
    WritableByteChannel __TEST_ONLY_OVERRIDE_FLOW_OUTPUT_CHANNEL = null;
    WritableByteChannel __TEST_ONLY_OVERRIDE_SNAPSHOT_OUTPUT_CHANNEL = null;

    private final UUID FLOW_ID = UUID.randomUUID();

    private final VariableDefinitionLookup variableDefinitionLookup;
    private final MessagePack messagePack;
    private final Runtime runtime;

    private String FINAL_MESSAGE;
    private boolean allCaptureDisabled = false;

    public Flow() {
        this(new VariableDefinitionLookup());
    }

    public Flow(VariableDefinitionLookup variableDefinitionLookup) {
        this(createMessagePack(), variableDefinitionLookup, Runtime.getRuntime());
    }

    Flow(MessagePack messagePack, VariableDefinitionLookup variableDefinitionLookup, Runtime runtime) {
        this.messagePack = messagePack;
        this.variableDefinitionLookup = variableDefinitionLookup;
        this.runtime = runtime;
        notifyNewSession();
    }

    protected void notifyNewSession() {
        try {
            HttpURLConnection connection =
                    (HttpURLConnection) new URL("http://localhost:33284/notify").openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);
            String message = "{\"newSession\": \"" + FLOW_ID + "\"}";
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(message);
            writer.flush();
            writer.close();
            connection.getResponseCode();
            connection.disconnect();
        } catch (MalformedURLException e) {
            // swallow
        } catch (IOException e) {
            // swallow
        }
    }

    void registerHookToPrintSessionURLOnExit() {
        String host = AgentMain.host != null ? AgentMain.host : "halik.io";
            FINAL_MESSAGE = "\nYour session is available to explore at: http://" + host + "/browse/" + FLOW_ID + "\n";
        runtime.addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.print(FINAL_MESSAGE);
            }
        });
    }

    private String flowDirectory() {
        return sessionDirectory() + FLOW_ID;
    }

    private String sessionDirectory() {
        return AgentMain.sessionsDirectory != null ? AgentMain.sessionsDirectory : "/var/halik/";
    }

    void createFlowDirectory() {
        new File(sessionDirectory()).mkdirs();
        new File(flowDirectory()).mkdirs();
    }

    public JSONOutput __redirectOutputToJSONObject() {
        final ByteArrayOutputStream flowBOS = new ByteArrayOutputStream();
        __TEST_ONLY_OVERRIDE_FLOW_OUTPUT_CHANNEL = Channels.newChannel(flowBOS);
        ByteArrayOutputStream snapshotBOS = new ByteArrayOutputStream();
        __TEST_ONLY_OVERRIDE_SNAPSHOT_OUTPUT_CHANNEL = Channels.newChannel(snapshotBOS);
        return new JSONOutput(flowBOS, snapshotBOS) {
            @Override
            public JSONArray toJSON() {
                try {
                    perThread().flowPacker().flush();
                    perThread().snapshotPacker().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return super.toJSON();
            }
        };
    }

    public VariableDefinitionLookup variableDefinitionLookup() {
        if (capturingStateDisabled()) {
            return VariableDefinitionLookup.EMPTY;
        }
        return variableDefinitionLookup;
    }

    public void captureMetadata(boolean captureCollections) {
        try {
            String metadata = "{\"captureCollectionsAndMaps\": " + captureCollections + "}";
            Files.write(metadata.getBytes(), new File(flowDirectory() + "/metadata"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void captureThreadNameChange(long id, String name) {
        try {
            String threadNameChange = String.format(",{\"id\": %d, \"name\": \"%s\" }", id, name);
            synchronized (this) {
                Files.append(threadNameChange, new File(flowDirectory() + "/threads"), Charset.forName("UTF-8"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void captureClassName(String name) {
        try {
            String classNameIndexEntry = String.format(",\"%s\"", name);
            synchronized (this) {
                Files.append(classNameIndexEntry, new File(flowDirectory() + "/classes"), Charset.forName("UTF-8"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void captureMethodNames(List<String> methodIndex) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(",[");
            for (int i = 0; i < methodIndex.size(); i++) {
                String methodName = methodIndex.get(i);
                sb.append(String.format("\"%s\"", methodName));
                if (i < methodIndex.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");

            synchronized (this) {
                Files.append(sb.toString(), new File(flowDirectory() + "/methods"), Charset.forName("UTF-8"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    WritableByteChannel sessionFileOutputChannel(final String type) {
        if (Thread.currentThread().getName().startsWith("$halik$")) {
            return Channels.newChannel(new NullOutputStream());
        }
        return createPrintStream(flowDirectory() + "/" + Thread.currentThread().getId() + "." + type + ".dbg");
    }

    protected ExecutorService createWritersExecutorService() {
        return Executors.newFixedThreadPool(1,
                new ThreadFactoryBuilder().setNameFormat("$halik$writers-%d").build());
    }

    private ThreadLocal<FlowPerThread> perThread = new ThreadLocal<>();

    private FlowPerThread perThread() {
        FlowPerThread flowPerThread = perThread.get();
        if (flowPerThread == null) {
            flowPerThread = new FlowPerThread(this, messagePack, variableDefinitionLookup, this.runtime);
            perThread.set(flowPerThread);
        }
        return flowPerThread;
    }

    public final void captureLineExecution(
            final int classIndex, final int methodIndex, final int lineNumber, int labelNumber) {
        perThread().disableCaptureInCaseJavaAgentCallsClientCode();
        try {
            if (!capturingLinesDisabled()) {
                final int nextStep = perThread().nextStep();

                packFlow(classIndex, methodIndex, lineNumber);

                Collector.CollectedObjects collectedObjects =
                        perThread().collector.collect(classIndex, methodIndex, labelNumber);
                packSnapshot(collectedObjects, nextStep);
            }
        } catch (RuntimeException e) {
            onExceptionDuringCapture(e, Thread.currentThread());
        } catch (IOException e) {
            onExceptionDuringCapture(e, Thread.currentThread());
        } finally {
            perThread().reenableCapture();
        }
    }

    private void packSnapshot(Collector.CollectedObjects collectedObjects, int nextStep) throws IOException {
        final JSONDirectBufferPacker snapshotPacker = perThread().snapshotPacker();
        if (collectedObjects.count() > 0) {
            snapshotPacker.writeMapBegin(2);
            MessagePackWriter messagePackWriter = new MessagePackWriter(snapshotPacker);
            snapshotPacker.write("i");
            snapshotPacker.write(nextStep);
            snapshotPacker.write("sn");
            snapshotPacker.writeArrayBegin(collectedObjects.count());
            collectedObjects.consume(messagePackWriter);
            snapshotPacker.writeArrayEnd();
            snapshotPacker.writeMapEnd();
            snapshotPacker.endJSONLine();
        }
    }

    private void packFlow(int classIndex, int methodIndex, int lineNumber) throws IOException {
        final long timestamp = System.currentTimeMillis();
        final Optional<String> tag = perThread().tagTracker.consumeTag();

        final JSONDirectBufferPacker flowPacker = perThread().flowPacker();
        flowPacker.writeMapBegin(5 + (tag.isPresent() ? 1 : 0));
        flowPacker.write("t").write(timestamp - BASE_TIMESTAMP);

        if (tag.isPresent()) {
            flowPacker.write("tg").write(tag.get());
        }
        flowPacker.write("c").write(classIndex);
        flowPacker.write("m").write(methodIndex);
        flowPacker.write("l").write(lineNumber);
        flowPacker.write("sd").write(perThread().stackTraceDepth());
        flowPacker.writeMapEnd();
        flowPacker.endJSONLine();
    }

    void onExceptionDuringCapture(Exception e, Thread inThread) {
        e.printStackTrace();
        stopCaptureOnException();
        System.out.println("!!! Exception occurred while running Halik. " +
                "Halik will be disabled and your program will continue to work correctly.");
        System.out.println("-- DIAGNOSTIC DATA [BEGIN] --");
        System.out.println("Session: "  + FLOW_ID);
        System.out.println("Thread: "  + inThread.getId());
        System.out.println("-- DIAGNOSTIC DATA [END] --");
        FINAL_MESSAGE = "";
    }

    private static MessagePack createMessagePack() {
        MessagePack messagePack = new JSON();
        messagePack.register(BigInteger.class, MessagePackDumpingGround.bigIntegerTemplate());
        return messagePack;
    }

    private boolean capturingLinesDisabled() {
        return perThread().capturingLinesDisabled() || allCaptureDisabled;
    }

    private boolean capturingStateDisabled() {
        return perThread().capturingStateDisabled() || allCaptureDisabled;
    }

    private void stopCaptureOnException() {
        allCaptureDisabled = true;
    }

    private FileChannel createPrintStream(String filename) {
        try {
            return new FileOutputStream(filename).getChannel();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public void dumpLeftoverVariableStateAtMethodExit() {
        if (capturingStateDisabled()) {
            return;
        }

        perThread().variableTracker.dumpAll();
    }

    public void captureVariableDefinition(CompleteVariableDefinition completeVariableDefinition) {
        if (capturingStateDisabled()) {
            return;
        }
        variableDefinitionLookup().trackVariableDefinition(completeVariableDefinition);
    }

    public void captureTag(String tag) {
        if (capturingStateDisabled()) {
            return;
        }
        perThread().tagTracker.trackTag(tag);
    }

    public void captureVariableModification(Object value, int index) {
        if (capturingStateDisabled()) {
            return;
        }
        perThread().variableTracker.trackVariableModification(index, value);
    }

    public void captureVariableModification(int value, int index) {
        if (capturingStateDisabled()) {
            return;
        }
        perThread().variableTracker.trackVariableModification(index, value);
    }

    public void captureVariableModification(float value, int index) {
        if (capturingStateDisabled()) {
            return;
        }
        perThread().variableTracker.trackVariableModification(index, value);
    }

    public void captureVariableModification(double value, int index) {
        if (capturingStateDisabled()) {
            return;
        }
        perThread().variableTracker.trackVariableModification(index, value);
    }

    public void captureVariableModification(long value, int index) {
        if (capturingStateDisabled()) {
            return;
        }
        perThread().variableTracker.trackVariableModification(index, value);
    }

    public void captureArrayModification(Object value) {
        if (capturingStateDisabled()) {
            return;
        }
        perThread().arrayModificationTracker.trackModification(value);
    }

    public void captureCollectionModification(Collection value) {
        if (capturingStateDisabled()) {
            return;
        }
        perThread().collectionModificationTracker.trackModification(value);
    }

    public void captureMapModification(Map value) {
        if (capturingStateDisabled()) {
            return;
        }
        perThread().mapModificationTracker.trackModification(value);
    }

    public void captureFieldModification(Object object, String fieldName, Object fieldValue) {
        if (capturingStateDisabled()) {
            return;
        }
        perThread().fieldModificationTracker.trackFieldAssignment(object, fieldName, fieldValue);
    }

    public void captureFieldModification(Object object, String fieldName, boolean fieldValue) {
        if (capturingStateDisabled()) {
            return;
        }
        perThread().fieldModificationTracker.trackFieldAssignment(object, fieldName, fieldValue);
    }

    public void captureFieldModification(Object object, String fieldName, double fieldValue) {
        if (capturingStateDisabled()) {
            return;
        }
        perThread().fieldModificationTracker.trackFieldAssignment(object, fieldName, fieldValue);
    }

    public void captureFieldModification(Object object, String fieldName, float fieldValue) {
        if (capturingStateDisabled()) {
            return;
        }
        perThread().fieldModificationTracker.trackFieldAssignment(object, fieldName, fieldValue);
    }

    public void captureFieldModification(Object object, String fieldName, long fieldValue) {
        if (capturingStateDisabled()) {
            return;
        }
        perThread().fieldModificationTracker.trackFieldAssignment(object, fieldName, fieldValue);
    }

    public void captureFieldModification(Object object, String fieldName, short fieldValue) {
        if (capturingStateDisabled()) {
            return;
        }
        perThread().fieldModificationTracker.trackFieldAssignment(object, fieldName, fieldValue);
    }

    public void captureFieldModification(Object object, String fieldName, byte fieldValue) {
        if (capturingStateDisabled()) {
            return;
        }
        perThread().fieldModificationTracker.trackFieldAssignment(object, fieldName, fieldValue);
    }

    public void captureFieldModification(Object object, String fieldName, char fieldValue) {
        if (capturingStateDisabled()) {
            return;
        }
        perThread().fieldModificationTracker.trackFieldAssignment(object, fieldName, fieldValue);
    }

    public void captureFieldModification(Object object, String fieldName, int fieldValue) {
        if (capturingStateDisabled()) {
            return;
        }
        perThread().fieldModificationTracker.trackFieldAssignment(object, fieldName, fieldValue);
    }

    final void increase() {
        if (capturingStateDisabled()) {
            return;
        }
        perThread().increase();
    }

    final void decrease() {
        if (capturingStateDisabled()) {
            return;
        }
        perThread().decrease();
    }

    public final void fetch(int subtractInternalCalls) {
        if (capturingStateDisabled()) {
            return;
        }
        perThread().fetch(subtractInternalCalls);
    }

}
