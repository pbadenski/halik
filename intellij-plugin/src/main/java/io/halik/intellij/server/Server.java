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

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;

public class Server implements ApplicationComponent {
    public void initComponent() {
        try {
            Sparkify.start(Config.PORT);
        } catch (Exception e) {
            Notifications.Bus.notify(
                    new Notification(
                            Notifications.SYSTEM_MESSAGES_GROUP_ID,
                            "Error while initializing Halik",
                            e.getMessage(),
                            NotificationType.ERROR));
        }
    }

    public void disposeComponent() {
    }

    @NotNull
    public String getComponentName() {
        return "Halik server";
    }
}