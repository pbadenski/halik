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
package io.halik.agent.output;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class JSONOutput {
    private final ByteArrayOutputStream flowBOS;
    private final ByteArrayOutputStream snapshotBOS;

    public JSONOutput(ByteArrayOutputStream flowBOS, ByteArrayOutputStream snapshotBOS) {
        this.flowBOS = flowBOS;
        this.snapshotBOS = snapshotBOS;
    }

    private JSONArray toJson(ByteArrayOutputStream bos) {
        String array = "[" + new String(bos.toByteArray()) + "]";
        return new JSONArray(array);
    }

    public JSONArray toJSON() {
        JSONArray flow = toJson(flowBOS);
        JSONArray snapshot = toJson(snapshotBOS);
        for (int i = 0; i < snapshot.length(); i++) {
            JSONObject each = snapshot.getJSONObject(i);
            flow.getJSONObject(each.getInt("i")).put("sn", each.getJSONArray("sn"));
        }
        return flow;
    }
}
