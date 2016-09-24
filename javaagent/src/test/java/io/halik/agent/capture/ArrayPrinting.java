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

import com.google.common.collect.ImmutableMap;
import io.halik.agent.capture.write.JavaObjectsPacker;
import org.json.JSONObject;
import org.junit.Test;
import org.msgpack.packer.BufferPacker;
import org.msgpack.util.json.JSON;

import static java.lang.System.identityHashCode;
import static org.assertj.core.api.Assertions.assertThat;

public class ArrayPrinting {

    private final BufferPacker packer = new JSON().createBufferPacker();

    @Test
    public void shouldPrintIntegerValue() throws Exception {
        boolean[] array = {true, false};

        JavaObjectsPacker.packArray(array, packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));
        assertThat(json.get("id")).isEqualTo(identityHashCode(array));
        assertThat(json.get("t")).isEqualTo("A");
        assertThat(json.getJSONArray("v").get(0)).isEqualTo(true);
        assertThat(json.getJSONArray("v").get(1)).isEqualTo(false);
    }

    @Test
    public void shouldPrintArrayOfArrays() throws Exception {
        boolean[] innerArray = {true, false};
        boolean[][] array = {innerArray};

        JavaObjectsPacker.packArray(array, packer);

        JSONObject json = new JSONObject(new String(packer.toByteArray()));
        assertThat(json.get("id")).isEqualTo(identityHashCode(array));
        assertThat(json.get("t")).isEqualTo("A");
        assertThat(json.getJSONArray("v").get(0))
                .isEqualToComparingFieldByField(
                        new JSONObject(ImmutableMap.<String, Object>of("t", "A", "id", identityHashCode(innerArray))));
    }
}