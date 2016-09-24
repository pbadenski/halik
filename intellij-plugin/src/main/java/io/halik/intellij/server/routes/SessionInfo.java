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
package io.halik.intellij.server.routes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.halik.intellij.server.Config;
import org.jetbrains.annotations.NotNull;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;

public class SessionInfo implements Route {

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.header("content-type", "application/json");
        return sessionInfoInJson(request.params("id"));
    }

    private String sessionInfoInJson(String sessionId) {
        Stream<String> flowFiles = Arrays.stream(new File(Config.SESSIONS_DIR + sessionId).list())
                .filter(filename -> filename.endsWith("flow.dbg"));
        JsonArray jsonDocument;
        if (flowFiles.count() == 0) {
            jsonDocument = OLD_jsonDocumentThreadListForSession(sessionId);
        } else {
            jsonDocument = jsonDocumentThreadListForSession(sessionId);
        }
        return jsonDocument.toString();
    }

    @NotNull
    private JsonArray OLD_jsonDocumentThreadListForSession(String sessionId) {
        Stream<String> oldStyleFlowFiles =
                Arrays.stream(new File(Config.SESSIONS_DIR + sessionId).list()).filter(filename -> filename.endsWith(".dbg"));
        Stream<String> threadList = oldStyleFlowFiles.map(threadFile -> threadFile.split("\\.")[0]);
        JsonArray jsonDocument = new JsonArray();
        threadList.map(threadNumber -> {
            JsonObject thread = new JsonObject();
            thread.addProperty("number", Long.valueOf(threadNumber));
            thread.addProperty("link", String.format("http://localhost:%s/session/%s/%s", Config.PORT, sessionId, threadNumber));
            return thread;
        }).forEach(jsonDocument::add);
        return jsonDocument;
    }

    private JsonArray jsonDocumentThreadListForSession(String sessionId) {
        Stream<String> flowFiles = Arrays.stream(new File(Config.SESSIONS_DIR + sessionId).list())
                .filter(filename -> filename.endsWith("flow.dbg"));
        Stream<String> threadList = flowFiles.map(threadFile -> threadFile.split("\\.")[0]);
        JsonArray jsonDocument = new JsonArray();
        threadList.map(threadNumber -> {
            JsonObject thread = new JsonObject();
            thread.addProperty("number", Long.valueOf(threadNumber));
            thread.addProperty("flow", String.format("http://localhost:%s/session/%s/flow/%s", Config.PORT, sessionId, threadNumber));
            thread.addProperty("snapshot", String.format("http://localhost:%s/session/%s/snapshot/%s", Config.PORT, sessionId, threadNumber));
            thread.addProperty("classes", String.format("http://localhost:%s/session/%s/classes", Config.PORT, sessionId));
            thread.addProperty("methods", String.format("http://localhost:%s/session/%s/methods", Config.PORT, sessionId));
            return thread;
        }).forEach(jsonDocument::add);
        return jsonDocument;
    }
}
