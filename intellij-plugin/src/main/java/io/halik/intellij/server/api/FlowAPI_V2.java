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
package io.halik.intellij.server.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.halik.intellij.server.Config;
import org.apache.xmlbeans.impl.common.ReaderInputStream;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static java.util.Arrays.asList;

public class FlowAPI_V2 extends FlowAPI {
    private final String sessionId;
    private final int flow;
    private final File flowFile;

    public FlowAPI_V2(String sessionId, int flowNumber) {
        this.sessionId = sessionId;
        this.flow = flowNumber;
        this.flowFile = new File(new File(Config.SESSIONS_DIR, sessionId), flowNumber + ".flow.dbg");
    }

    public FlowEntry getEntry(int lineNumber) {
        return new FlowEntry_V2(readLine(flowFile), classIndex());
    }

    private List<String> classIndex() {
        try {
            SequenceInputStream arrayedJson = arrayedJson(new File(new File(Config.SESSIONS_DIR, sessionId), "classes"));
            JsonArray jsonArray = new JsonParser().parse(new InputStreamReader(arrayedJson)).getAsJsonArray();

            List<String> index = new ArrayList<>();
            for (JsonElement jsonElement : jsonArray) {
                index.add(jsonElement.getAsString());
            }
            return index;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private SequenceInputStream arrayedJson(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.skip(1);
        return new SequenceInputStream(new Vector<>(asList(
                new ReaderInputStream(new StringReader("["), "UTF-8"),
                fileInputStream,
                new ReaderInputStream(new StringReader("]"), "UTF-8"))).elements());
    }
}
