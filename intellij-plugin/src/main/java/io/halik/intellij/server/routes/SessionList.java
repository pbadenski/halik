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

import com.google.gson.*;
import io.halik.intellij.server.Config;
import io.halik.intellij.server.api.FlowAPI;
import io.halik.intellij.server.api.FlowEntry;
import io.halik.intellij.server.api.SessionAPI;
import org.jetbrains.annotations.NotNull;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public class SessionList implements Route {
    private static final SimpleDateFormat ISO_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.header("content-type", "application/json");
        return listOfSessionsInJson();
    }

    private String listOfSessionsInJson() {
        List<File> sessions = asList(new File(Config.SESSIONS_DIR).listFiles());
        sessions.sort(byModificationDateDescending());
        Stream<String> sessionList = sessions.stream().map(File::getName);
        JsonArray jsonDocument = new JsonArray();
        sessionList.map(sessionId -> {
            File sessionDir = new File(Config.SESSIONS_DIR, sessionId);
            String title = guessTitle(sessionId);
            JsonObject session = new JsonObject();
            session.addProperty("id", sessionId);
            session.addProperty("date", ISO_DATE_TIME_FORMAT.format(sessionDir.lastModified()));
            session.addProperty("size", folderSize(sessionDir));
            session.addProperty("title", title);
            session.addProperty("link", String.format("http://localhost:%s/session/%s", Config.PORT, sessionId));
            return session;
        }).forEach(jsonDocument::add);

        return jsonDocument.toString();
    }

    public static long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }

    private String guessTitle(String sessionId) {
        return new SessionAPI(sessionId)
                .getFlow(1)
                .map(flowApi -> flowApi.getEntry(1))
                .flatMap(FlowEntry::className)
                .orElse(sessionId);
    }


    @NotNull
    private Comparator<File> byModificationDateDescending() {
        return (f1, f2) -> -Long.compare(f1.lastModified(), f2.lastModified());
    }
}
