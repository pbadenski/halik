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
package io.halik.agent.transform.utils;

public class ShouldTransformClass {
    public static boolean anyIntellijClass(String className) {
        return className.startsWith("com/intellij");
    }

    public static boolean autoGeneratedClass(String className) {
        return className.contains("_$$_");
    }

    public static boolean builtinJavaClass(String className) {
        return className.startsWith("java") || className.startsWith("javax")
                || className.startsWith("sun") || className.startsWith("com/sun")
                || className.startsWith("oracle");
    }

    /*
         * Fix for an exception below (probably related to System.out call in the Flow class):
         *
         * *** java.lang.instrument ASSERTION FAILED ***: "!errorOutstanding" with message transform method call failed at JPLISAgent.c line: 844
         * Exception: java.lang.StackOverflowError thrown from the UncaughtExceptionHandler in thread "PoolCleaner[1482968390:1443194385834]"
         */
    public static boolean runningFromIntelliJWhichMightCauseStackOverflowError(String className) {
        return className.startsWith("com/intellij/rt/execution");
    }

    public static boolean classLoaderIsNullWhichMightCauseNoClassDefFoundError(ClassLoader loader) {
        return loader == null;
    }

    public static boolean classBelongsToJavaAgent(String className) {
        return className.startsWith("io/halik/agent");
    }
}
