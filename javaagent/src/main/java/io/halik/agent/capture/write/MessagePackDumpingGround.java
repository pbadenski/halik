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

import org.msgpack.MessagePack;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.unpacker.Unpacker;
import org.msgpack.util.json.JSON;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;

public class MessagePackDumpingGround {
    public static AbstractTemplate<BigInteger> bigIntegerTemplate() {
        return new AbstractTemplate<BigInteger>() {
            @Override
            public void write(Packer packer, BigInteger target, boolean required) throws IOException {
                if (target == null) {
                    if (required) {
                        throw new MessageTypeException("Attempted to write null");
                    }
                    packer.writeNil();
                    return;
                }
                packer.write(target.toString());
            }

            @Override
            public BigInteger read(Unpacker unpacker, BigInteger bigInteger, boolean required) throws IOException {
                if (!required && unpacker.trySkipNil()) {
                    return null;
                }
                return new BigInteger(unpacker.readString());
            }
        };
    }

    public static ByteBuffer toByteBuffer(byte[] message, MessagePack messagePack) {
        ByteBuffer byteBuffer;
        if (messagePack instanceof JSON) {
            byteBuffer = ByteBuffer.allocate(message.length + 2);
            byteBuffer.put(message);
            byteBuffer.put(new byte[] { ',', '\n'});
            byteBuffer.flip();
        } else {
            byteBuffer = ByteBuffer.wrap(message);
        }
        return byteBuffer;
    }
}
