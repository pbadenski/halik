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
package io.halik.intellij.server;

import com.intellij.openapi.project.ProjectManager;
import io.halik.intellij.server.routes.NotifyNewSession;
import io.halik.intellij.server.routes.SessionInfo;
import io.halik.intellij.server.routes.SessionList;
import io.halik.intellij.server.routes.Source;
import org.apache.xmlbeans.impl.common.ReaderInputStream;
import org.jetbrains.annotations.NotNull;
import spark.Route;
import spark.Spark;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.StringReader;
import java.util.Vector;

import static java.util.Arrays.asList;

public class Sparkify {
    public static void start(int port) {
        Spark.port(port);
        Spark.get("/", (request, response) -> {
            String id = request.params().get("id");
            if (id != null) {
                response.redirect("/browse/" + id, 301);
                return "";
            } else {
                return Server.class.getResourceAsStream("/sessions.html");
            }
        });
        Spark.get("/browse/*", serveClasspathFile("/index.html"));
        Spark.get("/session", new SessionList());
        Spark.get("/session/:id", new SessionInfo());
        Spark.get("/session/:id/sources/:class", new Source(ProjectManager.getInstance()));
        Spark.get("/session/:id/threads", serveNewStyleJsonOutputFile("threads"));
        Spark.get("/session/:id/classes", serveNewStyleJsonOutputFile("classes"));
        Spark.get("/session/:id/methods", serveNewStyleJsonOutputFile("methods"));
        Spark.get("/session/:id/metadata", serveJsonOutputFile("metadata"));
        backwardsCompatibility_removeIn1_0();
        Spark.get("/session/:id/flow/:thread", (request, response) -> {
            String sessionId = request.params("id");
            String threadNumber = request.params("thread");
            response.header("content-type", "text/plain");
            return new FileInputStream(Config.SESSIONS_DIR + sessionId + "/" + threadNumber + ".flow.dbg");
        });
        Spark.get("/session/:id/snapshot/:thread", (request, response) -> {
            String sessionId = request.params("id");
            String threadNumber = request.params("thread");
            response.header("content-type", "text/plain");
            return new FileInputStream(Config.SESSIONS_DIR + sessionId + "/" + threadNumber + ".snapshot.dbg");
        });
        Spark.get("/user/sessions", new SessionList());
        Spark.post("/notify", new NotifyNewSession());
        Spark.get("/*", (request, response) -> Server.class.getResourceAsStream(request.pathInfo()));
        Spark.exception(Exception.class, (exception, request, response) -> {
            exception.printStackTrace();
        });
    }

    private static void backwardsCompatibility_removeIn1_0() {
        Spark.get("/session/:id/:thread", (request, response) -> {
            String sessionId = request.params("id");
            String threadNumber = request.params("thread");
            response.header("content-type", "text/plain");
            return new FileInputStream(Config.SESSIONS_DIR + sessionId + "/" + threadNumber + ".dbg");
        });
    }

    private static Route serveNewStyleJsonOutputFile(String artifactName) {
        return (request, response) -> {
            String sessionId = request.params("id");
            FileInputStream fileInputStream =  new FileInputStream(Config.SESSIONS_DIR + sessionId + "/" + artifactName);
            response.header("content-type", "application/json");
            fileInputStream.skip(1);
            return new SequenceInputStream(new Vector<>(asList(
                    new ReaderInputStream(new StringReader("["), "UTF-8"),
                    fileInputStream,
                    new ReaderInputStream(new StringReader("]"), "UTF-8"))).elements());
        };

    }

    @NotNull
    private static Route serveClasspathFile(String name) {
        return (request, response) -> Server.class.getResourceAsStream(name);
    }

    private static Route serveJsonOutputFile(final String fileName) {
        return (request, response) -> {
            String sessionId = request.params("id");
            response.header("content-type", "application/json");
            return new FileInputStream(Config.SESSIONS_DIR + sessionId + "/" + fileName);
        };
    }
}
