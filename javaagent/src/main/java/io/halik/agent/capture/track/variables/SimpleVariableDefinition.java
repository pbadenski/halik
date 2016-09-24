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

import org.objectweb.asm.Type;

public class SimpleVariableDefinition {
    public final String name;
    public final Type type;
    public final int fromLabel;
    public final int index;

    public SimpleVariableDefinition(String name, Type type, int fromLabel, int index) {
        this.name = name;
        this.type = type;
        this.fromLabel = fromLabel;
        this.index = index;
    }

    public Object convertBasedOnType(Object value) {
        try {
            if (type == Type.BOOLEAN_TYPE) {
                return (int)value == 1;
            }

            if (type == Type.CHAR_TYPE) {
                return (char) ((int) value);
            }
            return value;
        } catch (ClassCastException e) {
            return null;
        }
    }

    public Variable variable(Object value) {
        return new Variable(this, value);
    }

    @Override
    public String toString() {
        return "SimpleVariableDefinition{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", fromLabel=" + fromLabel +
                ", index=" + index +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleVariableDefinition that = (SimpleVariableDefinition) o;

        if (fromLabel != that.fromLabel) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return type != null ? type.equals(that.type) : that.type == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + fromLabel;
        return result;
    }
}
