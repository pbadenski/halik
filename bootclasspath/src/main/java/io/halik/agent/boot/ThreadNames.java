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
package io.halik.agent.boot;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ThreadNames {
    private static OnThreadNameChange listener;

    public interface OnThreadNameChange {
        void process(ThreadNameChange threadNameChange);
    }

    public static class ThreadNameChange {
        public final Long id;
        public final String name;

        public ThreadNameChange(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    private static List<ThreadNameChange> threadNameChanges = new CopyOnWriteArrayList<ThreadNameChange>();

    public static void registerThreadNameChangeListener(OnThreadNameChange l) {
        if (listener != null) {
            return;
        }
        listener = l;
        for (ThreadNameChange threadNameChange : threadNameChanges) {
            listener.process(threadNameChange);
        }
    }

    public static void recordThreadName(Thread currentThread) {
        ThreadNameChange threadNameChange = new ThreadNameChange(currentThread.getId(), currentThread.getName());
        if (listener != null) {
            listener.process(threadNameChange);
        } else {
            threadNameChanges.add(threadNameChange);
        }
    }
}
