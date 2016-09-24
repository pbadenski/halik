/*
 * Implemented based on "org.msgpack.io.ByteBufferOutput"
 *
 * Changes the behaviour of reserve & flush.
 *
 * Original: Copyright (C) 2009 - 2013 FURUHASHI Sadayuki
 * Modifications: Copyright (C) 2014-2015 Pawel Badenski
 *
 * Original source code is licensed under the Apache License, Version 2.0
 * You may obtain a copy of the License at: http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package io.halik.agent.output;

import org.msgpack.io.Output;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class ByteBufferOutput implements Output {
    private int lastLineEndPosition = 0;

    public interface ExpandBufferCallback {
        ByteBuffer call(ByteBuffer buffer, int len) throws IOException;
    }

    private ByteBuffer buffer;
    private ExpandBufferCallback callback;

    public ByteBufferOutput(ByteBuffer buffer) {
        this(buffer, null);
    }

    public ByteBufferOutput(ByteBuffer buffer, ExpandBufferCallback callback) {
        this.buffer = buffer;
        this.callback = callback;
    }

    private void reserve(int len) throws IOException {
        if (len <= buffer.remaining()) {
            return;
        }
        if (callback == null) {
            throw new BufferOverflowException();
        }
        buffer = flushToEndOfLineAndTransferRestOverToNewBuffer(len);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        reserve(len);
        buffer.put(b, off, len);
    }

    @Override
    public void write(ByteBuffer bb) throws IOException {
        reserve(bb.remaining());
        buffer.put(bb);
    }

    @Override
    public void writeByte(byte v) throws IOException {
        reserve(1);
        buffer.put(v);
    }

    @Override
    public void writeShort(short v) throws IOException {
        reserve(2);
        buffer.putShort(v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        reserve(4);
        buffer.putInt(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        reserve(8);
        buffer.putLong(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        reserve(4);
        buffer.putFloat(v);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        reserve(8);
        buffer.putDouble(v);
    }

    @Override
    public void writeByteAndByte(byte b, byte v) throws IOException {
        reserve(2);
        buffer.put(b);
        buffer.put(v);
    }

    @Override
    public void writeByteAndShort(byte b, short v) throws IOException {
        reserve(3);
        buffer.put(b);
        buffer.putShort(v);
    }

    @Override
    public void writeByteAndInt(byte b, int v) throws IOException {
        reserve(5);
        buffer.put(b);
        buffer.putInt(v);
    }

    @Override
    public void writeByteAndLong(byte b, long v) throws IOException {
        reserve(9);
        buffer.put(b);
        buffer.putLong(v);
    }

    @Override
    public void writeByteAndFloat(byte b, float v) throws IOException {
        reserve(5);
        buffer.put(b);
        buffer.putFloat(v);
    }

    @Override
    public void writeByteAndDouble(byte b, double v) throws IOException {
        reserve(9);
        buffer.put(b);
        buffer.putDouble(v);
    }

    public void writeLineEnd() throws IOException {
        writeByte((byte) ',');
        writeByte((byte) '\n');
        lastLineEndPosition = buffer.position();
    }

    @Override
    public void flush() throws IOException {
        flushToEndOfLineAndTransferRestOverToNewBuffer(-1);
    }

    private ByteBuffer flushToEndOfLineAndTransferRestOverToNewBuffer(int bytesToReserve) throws IOException {
        ByteBuffer toFlush = this.buffer;
        ByteBuffer toCopyOver = this.buffer.duplicate();

        toCopyOver.position(lastLineEndPosition);
        toCopyOver.limit(toFlush.position());

        toFlush.limit(lastLineEndPosition);

        ByteBuffer newBuffer = callback.call(toFlush, bytesToReserve);

        newBuffer.put(toCopyOver);

        lastLineEndPosition = 0;
        return newBuffer;
    }

    @Override
    public void close() throws IOException {

    }

}
