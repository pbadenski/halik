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
package io.halik.agent.capture.track.fields;

import io.halik.agent.capture.write.JavaObjectsPacker;
import org.msgpack.packer.Packer;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static java.lang.System.identityHashCode;
import static io.halik.agent.capture.write.JavaObjectsPacker.isPrimitive;

public class Field {

    final Integer parentObjectId;
    final String name;
    final Object value;

    public Field(Object object, String name, Object value) {
        this(identityHashCode(object), name, value);
    }

    public Field(Integer parentObjectId, String name, Object value) {
        assert parentObjectId != null;

        this.parentObjectId = parentObjectId;
        this.name = name;
        this.value = value;
    }


    public void pack(Packer packer) throws IOException {
        if (value == null) {
            JavaObjectsPacker.packFieldPrimitive(parentObjectId, null, name, packer);
        } else if (this.value.getClass().isArray()) {
            JavaObjectsPacker.packFieldArray(parentObjectId, value, name, packer);
        } else if (isPrimitive(this.value)) {
            JavaObjectsPacker.packFieldPrimitive(parentObjectId, value, name, packer);
        } else if (this.value instanceof Collection) {
            JavaObjectsPacker.packFieldCollection(parentObjectId, (Collection) value, name, packer);
        } else if (this.value instanceof Map) {
            JavaObjectsPacker.packFieldMap(parentObjectId, (Map) value, name, packer);
        } else {
            JavaObjectsPacker.packFieldObject(parentObjectId, value, name, packer);
        }
    }


    private Integer objectId() {
        return identityHashCode(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Field field = (Field) o;

        if (name != null ? !name.equals(field.name) : field.name != null) return false;
        if (value != null ? !objectId().equals(field.objectId()) : field.objectId() != null) return false;
        if (!parentObjectId.equals(field.parentObjectId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = parentObjectId.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (value != null ? objectId() : 0);
        return result;
    }
}
