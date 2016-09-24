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

import io.halik.intellij.server.Config;

import java.io.File;
import java.util.Optional;

public class SessionAPI {
    private String sessionId;

    public SessionAPI(String sessionId) {
        this.sessionId = sessionId;
    }

    public Optional<FlowAPI> getFlow(int number) {
        if (new File(new File(Config.SESSIONS_DIR, sessionId), number + ".dbg").exists()) {
            return Optional.of(new FlowAPI_V1(sessionId, number));
        }

        if (new File(new File(Config.SESSIONS_DIR, sessionId), number + ".flow.dbg").exists()) {
            return Optional.of(new FlowAPI_V2(sessionId, number));
        }
        return Optional.empty();
    }
}
