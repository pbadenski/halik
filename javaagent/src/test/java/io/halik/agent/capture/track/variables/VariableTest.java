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

import org.junit.Test;
import org.objectweb.asm.Type;

import static org.assertj.core.api.Assertions.assertThat;


public class VariableTest {
    @Test
    public void shouldPrintValuableToStringEvenIfValueToStringThrowsNPE() {
        Variable variable = new Variable(
                new SimpleVariableDefinition("var", Type.getType(Object.class), 0, 0),
                toStringThrowsNPE());

        assertThat(variable.toString()).isEqualTo(
                "Variable{simpleVariableDefinition=SimpleVariableDefinition" +
                "{name='var', type=Ljava/lang/Object;, fromLabel=0, index=0}" +
                ", value=Exception: java.lang.NullPointerException}");
    }

    private Object toStringThrowsNPE() {
        return new Object() {
            @Override
            public String toString() {
                throw new NullPointerException();
            }
        };
    }

}