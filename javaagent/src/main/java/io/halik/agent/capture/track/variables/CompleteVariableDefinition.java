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

import io.halik.agent.capture.DumpingGround;
import org.objectweb.asm.Type;

public class CompleteVariableDefinition {
    private final int classIndex;
    private final int methodIndex;
    private final Type type;
    private final String variableName;
    private final int index;
    private int fromLabel;
    private int toLabel;

    public CompleteVariableDefinition(int classIndex, int methodIndex,
                                      Type type,
                                      int fromLabel, int toLabel,
                                      String variableName,
                                      int index) {
        this.classIndex = classIndex;
        this.methodIndex = methodIndex;
        this.type = type;
        this.fromLabel = fromLabel;
        this.toLabel = toLabel;

        this.variableName = variableName;
        this.index = index;
    }

    public Type getType() {
        return type;
    }

    public String getVariableName() {
        return variableName;
    }

    public int getIndex() {
        return index;
    }

    public long uniqueMethodIdentifier() {
        return DumpingGround.uniqueMethodIdentifier(classIndex, methodIndex);
    }

    public int getFromLabel() {
        return fromLabel;
    }

    public int getToLabel() {
        return toLabel;
    }
}
