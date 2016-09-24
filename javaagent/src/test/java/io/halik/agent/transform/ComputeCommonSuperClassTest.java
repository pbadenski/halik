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
package io.halik.agent.transform;

import io.halik.agent.transform.utils.ComputeCommonSuperClass;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ComputeCommonSuperClassTest {

    @Test
    public void shouldFindParentSharedAncestor() {
        Assertions.assertThat(ComputeCommonSuperClass.computeCommonSuperClass("io/halik/agent/transform/testdata/TestClassC", "io/halik/agent/transform/testdata/TestClassD"))
                .isEqualTo("io/halik/agent/transform/testdata/TestClassB");
    }

    @Test
    public void shouldFindDistantSharedAncestor() {
        Assertions.assertThat(ComputeCommonSuperClass.computeCommonSuperClass("io/halik/agent/transform/testdata/TestClassC", "io/halik/agent/transform/testdata/TestClassE"))
                .isEqualTo("io/halik/agent/transform/testdata/TestClassA");
    }

    @Test
    public void shouldFindAncestorWhenIsIncludedClass() {
        Assertions.assertThat(ComputeCommonSuperClass.computeCommonSuperClass("io/halik/agent/transform/testdata/TestClassB", "io/halik/agent/transform/testdata/TestClassC"))
                .isEqualTo("io/halik/agent/transform/testdata/TestClassB");
    }

    @Test
    public void shouldNotHaveOrderMatter() {
        Assertions.assertThat(ComputeCommonSuperClass.computeCommonSuperClass("io/halik/agent/transform/testdata/TestClassC", "io/halik/agent/transform/testdata/TestClassB"))
                .isEqualTo(ComputeCommonSuperClass.computeCommonSuperClass("io/halik/agent/transform/testdata/TestClassB", "io/halik/agent/transform/testdata/TestClassC"));
    }
}