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
package io.halik.agent.capture.track.variables;

import io.halik.agent.capture.write.JavaObjectsPacker;
import org.msgpack.packer.Packer;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static io.halik.agent.capture.write.JavaObjectsPacker.isPrimitive;

public class Variable {
    private final SimpleVariableDefinition simpleVariableDefinition;
    private final Object value;

    public Variable(SimpleVariableDefinition simpleVariableDefinition, Object value) {
        this.simpleVariableDefinition = simpleVariableDefinition;
        this.value = value;
    }

    public void pack(Packer packer) throws IOException {
        Object variableValue = this.simpleVariableDefinition.convertBasedOnType(this.value);
        if (variableValue == null) {
            JavaObjectsPacker.packNamedPrimitive(null, this.simpleVariableDefinition.name, packer);
        }
        else if (variableValue.getClass().isArray()) {
            JavaObjectsPacker.packNamedArray(variableValue, this.simpleVariableDefinition.name, packer);
        } else if (isPrimitive(variableValue)) {
            JavaObjectsPacker.packNamedPrimitive(variableValue, this.simpleVariableDefinition.name, packer);
        } else if (variableValue instanceof Collection) {
            JavaObjectsPacker.packNamedCollection((Collection) variableValue, this.simpleVariableDefinition.name, packer);
        } else if (variableValue instanceof Map) {
            JavaObjectsPacker.packNamedMap((Map) variableValue, this.simpleVariableDefinition.name, packer);
        } else {
            JavaObjectsPacker.packNamedObject(variableValue, this.simpleVariableDefinition.name, packer);
        }
    }

    @Override
    public String toString() {
        String valueToString = null;
        try {
            if (this.value != null) {
                valueToString = this.value.toString();
            }
        } catch (Exception e) {
            valueToString = "Exception: " + e.toString();
        }
        return "Variable{" +
                "simpleVariableDefinition=" + simpleVariableDefinition +
                ", value=" + valueToString +
                '}';
    }
}
