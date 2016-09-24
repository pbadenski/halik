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

import io.halik.agent.capture.write.JavaObjectsPacker;
import org.json.JSONObject;
import org.junit.Test;
import org.msgpack.packer.BufferPacker;
import org.msgpack.util.json.JSON;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MapPrinting {

    private final BufferPacker packer = new JSON().createBufferPacker();

    @Test
    public void shouldPrintMapOfPrimitives() throws Exception {
        Map map = new HashMap();
        map.put(1, 2);
        map.put(3, 4);

        JavaObjectsPacker.packMap(map, packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));
        assertThat(json.getJSONArray("v").getJSONObject(0).getJSONObject("k").getInt("v"))
                .isEqualTo(1);
        assertThat(json.getJSONArray("v").getJSONObject(0).getJSONObject("v").getInt("v"))
                .isEqualTo(2);

        assertThat(json.getJSONArray("v").getJSONObject(1).getJSONObject("k").getInt("v"))
                .isEqualTo(3);
        assertThat(json.getJSONArray("v").getJSONObject(1).getJSONObject("v").getInt("v"))
                .isEqualTo(4);
    }
}