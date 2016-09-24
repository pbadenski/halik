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
package io.halik.agent;

import io.halik.agent.capture.FlowFacade;
import io.halik.agent.transform.classTransformers.CaptureCollectionOrMapStateTransformer;
import io.halik.agent.transform.classTransformers.CaptureFlowAndStateClassTransformer;
import io.halik.agent.transform.classTransformers.CaptureStackTraceDepthTransformer;
import io.halik.agent.transform.classTransformers.CaptureThreadNameTransformer;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.*;

import static java.util.Arrays.asList;

public class AgentMain {
    public static String host;
    public static String sessionsDirectory;

    public static void agentmain(String agentArguments,
                                 Instrumentation instrumentation) {
        premain(agentArguments, instrumentation);
    }

    public static void premain(String agentArguments,
                               Instrumentation instrumentation) {
        boolean debug = false;
        List<String> include = new ArrayList<>();
        List<String> exclude = new ArrayList<>();
        boolean captureCollections = false;
        Map<String, String> args = parseArguments(agentArguments);
        boolean testMode = false;
        if (args.get("include") != null) {
            include = parseFilters(args.get("include"));
        }
        if (args.get("exclude") != null) {
            exclude = parseFilters(args.get("exclude"));
        }
        if (args.get("debug") != null) {
            debug = Boolean.valueOf(args.get("debug"));
        }
        if (args.get("experimentalCollections") != null) {
            captureCollections = Boolean.valueOf(args.get("experimentalCollections"));
        }
        if (args.get("browserUrl") != null) {
            host = args.get("browserUrl");
        }
        if (args.get("sessionsDir") != null) {
            sessionsDirectory = args.get("sessionsDir");
        }
        if (args.get("testMode") != null) {
            testMode = Boolean.valueOf(args.get("testMode"));
        }

        Class[] copyOfAllLoadedClasses = instrumentation.getAllLoadedClasses().clone();
        instrumentation.addTransformer(
                new CaptureStackTraceDepthTransformer(testMode, debug),
                instrumentation.isRetransformClassesSupported());
        if (captureCollections) {
            instrumentation.addTransformer(new CaptureCollectionOrMapStateTransformer(debug, include, exclude));
        }
        instrumentation.addTransformer(new CaptureFlowAndStateClassTransformer(debug, include, exclude));
        instrumentation.addTransformer(
                new CaptureThreadNameTransformer(testMode, debug),
                instrumentation.isRetransformClassesSupported());
        FlowFacade.captureMetadata(captureCollections);
        if (!testMode) {
            for (Class eachClass : copyOfAllLoadedClasses) {
                if (instrumentation.isRetransformClassesSupported() && instrumentation.isModifiableClass(eachClass)) {
                    try {
                        instrumentation.retransformClasses(eachClass);
                    } catch (UnmodifiableClassException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static List<String> parseFilters(String filters) {
        if (filters.isEmpty()) {
            return Collections.emptyList();
        }
        return asList(filters.split("\\|"));
    }

    private static Map<String, String> parseArguments(String argumentString) {
        Map<String, String> parsedArguments = new HashMap<>();
        if (argumentString != null) {
            String[] args = argumentString.split(",");
            for (String pair : args) {
                String[] parsedPair = pair.split("\\:", 2);
                if (parsedPair.length == 1) {
                    parsedArguments.put(parsedPair[0], null);
                }
                if (parsedPair.length == 2) {
                    parsedArguments.put(parsedPair[0], parsedPair[1]);
                }

            }
        }
        return parsedArguments;
    }
}
