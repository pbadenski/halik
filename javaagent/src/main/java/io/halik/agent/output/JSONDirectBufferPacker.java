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

import org.msgpack.MessagePack;
import org.msgpack.util.json.JSONPacker;

import java.io.IOException;
import java.nio.ByteBuffer;

public class JSONDirectBufferPacker extends JSONPacker {

    public static final int BUFFER_SIZE = 1048576;

    public JSONDirectBufferPacker(MessagePack msgpack, final FlushBuffer flushBuffer) {
        super(msgpack, new ByteBufferOutput(allocateDirectBuffer(), expandBufferCallback(flushBuffer)));
    }

    private static ByteBufferOutput.ExpandBufferCallback expandBufferCallback(final FlushBuffer flushBuffer) {
        return new ByteBufferOutput.ExpandBufferCallback() {
            @Override
            public ByteBuffer call(ByteBuffer buffer, int len) throws IOException {
                buffer.flip();
                flushBuffer.flush(buffer);
                return allocateDirectBuffer();
            }
        };
    }

    private static ByteBuffer allocateDirectBuffer() {
        return ByteBuffer.allocateDirect(BUFFER_SIZE);
    }

    public void endJSONLine() throws IOException {
        ((ByteBufferOutput)out).writeLineEnd();
    }

    public interface FlushBuffer {
        void flush(ByteBuffer buffer);
    }
}
