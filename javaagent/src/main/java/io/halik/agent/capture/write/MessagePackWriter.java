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

import io.halik.agent.capture.track.fields.Field;
import io.halik.agent.capture.track.variables.Variable;
import org.msgpack.packer.BufferPacker;
import org.msgpack.packer.Packer;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class MessagePackWriter {

    private final Packer packer;

    public MessagePackWriter(Packer packer) {
        this.packer = packer;
    }

    public void writeField(Field field) {
        try {
            field.pack(packer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeArray(Object value) {
        try {
            if (value == null) {
                packer.writeNil();
            }
            else {
                JavaObjectsPacker.packArray(value, packer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeCollection(Object value) {
        try {
            if (value == null) {
                packer.writeNil();
            }
            else {
                JavaObjectsPacker.packCollection((Collection) value, packer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void writeMap(Object value) {
        try {
            if (value == null) {
                packer.writeNil();
            }
            else {
                JavaObjectsPacker.packMap((Map) value, packer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeVariable(Variable variable) {
        try {
            variable.pack(packer);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error while writing: " + variable.toString(),
                    e);
        }
    }
}
