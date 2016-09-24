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
package io.halik.agent.capture.write;

import com.google.common.collect.Iterators;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.msgpack.util.json.JSONBufferPacker;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class JavaObjectsPackerTest {
    @Test
    public void shouldIgnoreCollectionSizeThrowingException() throws Exception {
        JSONBufferPacker packer = new JSONBufferPacker();
        JavaObjectsPacker.packCollection(new ArrayList() {
            @Override
            public int size() {
                throw new NullPointerException();
            }
        }, packer);
        assertThat(new JSONObject(new String(packer.toByteArray())).get("v")).isEqualTo(null);
    }

    @Test
    public void shouldIgnoreMapEntrySetSizeThrowingException() throws Exception {
        JSONBufferPacker packer = new JSONBufferPacker();
        JavaObjectsPacker.packMap(new HashMap() {
            @Override
            public Set<Entry> entrySet() {
                return new HashSet<Entry>() {
                    @Override
                    public int size() {
                        throw new NullPointerException();
                    }
                };
            }
        }, packer);
        assertThat(new JSONObject(new String(packer.toByteArray())).get("v")).isEqualTo(null);
    }

    @Test
    public void shouldIgnoreMapEntryKeyAndValueThrowingException() throws Exception {
        JSONBufferPacker packer = new JSONBufferPacker();
        JavaObjectsPacker.packMap(new HashMap() {
            @Override
            public Set<Entry> entrySet() {
                return new HashSet<Entry>() {
                    @Override
                    public Iterator<Entry> iterator() {
                        return Iterators.<Entry>singletonIterator(new Entry() {
                            @Override
                            public Object getKey() {
                                throw new NullPointerException();
                            }

                            @Override
                            public Object getValue() {
                                throw new NullPointerException();
                            }

                            @Override
                            public Object setValue(Object value) {
                                return null;
                            }
                        });
                    }

                    @Override
                    public int size() {
                        return 1;
                    }
                };
            }
        }, packer);

        JSONObject map = new JSONObject(new String(packer.toByteArray()));
        JSONArray array = map.getJSONArray("v");
        assertThat(array.getJSONObject(0).get("k")).isEqualTo(null);
        assertThat(array.getJSONObject(0).get("v")).isEqualTo(null);
    }
}