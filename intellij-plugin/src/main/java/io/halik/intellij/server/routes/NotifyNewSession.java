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

import com.google.common.io.CharStreams;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.ide.DataManager;
import com.intellij.ide.browsers.BrowserLauncher;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.Consumer;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.InputStreamReader;
import java.net.URI;

public class NotifyNewSession implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        String requestBody =
                CharStreams.toString(new InputStreamReader(request.raw().getInputStream()));
        JsonObject json = new JsonParser().parse(requestBody).getAsJsonObject();
        final String newSessionId = json.get("newSession").getAsString();
        if (newSessionId != null) {
            final NotificationGroup GROUP_DISPLAY_ID_INFO =
                    new NotificationGroup("My notification group",
                            NotificationDisplayType.BALLOON, true);
            ApplicationManager.getApplication().invokeLater(() -> {
                final String sessionUrl = "http://localhost:33284/browse/" + newSessionId;
                String notificationMessage =
                        "Your session is available <a href='#'>here</a>.";
                final Notification notification = GROUP_DISPLAY_ID_INFO.createNotification(
                        "Halik recording started",
                        notificationMessage,
                        NotificationType.INFORMATION,
                        (ignore, ignore2) -> BrowserLauncher.getInstance().browse(URI.create(sessionUrl)));
                DataManager.getInstance().getDataContextFromFocus().doWhenDone((Consumer<DataContext>) dataContext -> {
                    Project project = DataKeys.PROJECT.getData(dataContext);
                    Notifications.Bus.notify(notification, project);
                });
            });
        }
        response.status(202);
        return "OK";
    }
}
