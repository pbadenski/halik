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

/**
 * support for old version - to be deleted with 1.X upgrade
 */
@Deprecated
public class FlowAPI_V1 extends FlowAPI {
    private final String sessionId;
    private final int flow;
    private final File flowFile;

    public FlowAPI_V1(String sessionId, int flow) {
        this.sessionId = sessionId;
        this.flow = flow;
        this.flowFile = new File(new File(Config.SESSIONS_DIR, sessionId), flow + ".dbg");
    }

    @Override
    @Deprecated
    public FlowEntry getEntry(int lineNumber) {
        return new FlowEntry_V1(readLine(flowFile));
    }
}
