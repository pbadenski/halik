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
package test.halik;

import com.google.common.collect.AbstractIterator;
import io.halik.agent.capture.FlowFacade;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CallWithinAgentCode {
    public static void main() {
        List list = new ArrayList() {
            @Override
            public Iterator iterator() {
                return new AbstractIterator() {
                    @Override
                    protected Object computeNext() {
                        // This code will be invoked, because we use the iterator
                        // from within agent code to capture the list state.
                        // Point is no code from within this scope should have
                        // any effects on Flow - it all should be ignored.
                        makeSureNoFlowFacadeCallsAreInvokedFromWithinAgentCode();

                        endOfData();
                        return null;
                    }
                };
            }
        };

    }

    private static void makeSureNoFlowFacadeCallsAreInvokedFromWithinAgentCode() {
        FlowFacade.captureTag(null);

        FlowFacade.captureArrayModification(null);
        FlowFacade.captureMapModification(null);
        FlowFacade.captureCollectionModification(null);

        FlowFacade.captureFieldModification(null, null, null);
        FlowFacade.captureFieldModification(null, null, false);
        FlowFacade.captureFieldModification(null, null, 0D);
        FlowFacade.captureFieldModification(null, null, 0F);
        FlowFacade.captureFieldModification(null, null, 0L);
        FlowFacade.captureFieldModification(null, null, 0x0);
        FlowFacade.captureFieldModification(null, null, ' ');
        FlowFacade.captureFieldModification(null, null, 0);

        FlowFacade.captureVariableModification(null, 0);
        FlowFacade.captureVariableModification(0D, 0);
        FlowFacade.captureVariableModification(0F, 0);
        FlowFacade.captureVariableModification(0L, 0);
        FlowFacade.captureVariableModification(0, 0);
    }
}
