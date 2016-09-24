# Copyright (C) 2016 Pawel Badenski
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
module.exports = class ReferenceFlow
  buildReferenceFlow = (flows) ->
    _(_.mergeWith.apply(
        _,
        _.map(flows, (flow) -> _.countBy(flow, "t")).concat((a, b) -> Math.max(a or -Infinity, b or -Infinity))))
      .map (v, k) -> _.times(v, () -> parseInt(k))
      .flatten()
      .sortBy()
      .value()

  constructor: (threadsWithFlows) ->
    console.assert _.every(_.map(threadsWithFlows, "flow"), _.isArray), "all flows must be arrays"
    @referenceFlow = buildReferenceFlow(_.map(threadsWithFlows, "flow"))

  length: () ->
    @referenceFlow.length

  timestampAt: (index) ->
    return undefined if not @referenceFlow[index]?
    parseInt(@referenceFlow[index])

  toRefLookupFunc =
    _.memoize((flow, referenceFlow) ->
      flowToReferenceFlowIndex = []
      i = j = 0
      while i < referenceFlow.length and flow[j]?
        if flow[j].t == referenceFlow[i]
          flowToReferenceFlowIndex[j++] = i
        i++
      flowToReferenceFlowIndex)

  toRefLookup: (flow) ->
    toRefLookupFunc(flow, @referenceFlow)

  fromRefLookupFunc =
    _.memoize((flow, referenceFlow) ->
      flowToReferenceFlowIndex = []
      i = j = 0
      while i < referenceFlow.length
        break unless flow[j]?
        if flow[j].t == referenceFlow[i]
          flowToReferenceFlowIndex[i] = j++
        i++
      flowToReferenceFlowIndex)

  fromRefLookup: (flow) ->
    fromRefLookupFunc(flow, @referenceFlow)
