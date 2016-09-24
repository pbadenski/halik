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
module.exports = class
  buildUnifiedView = (threadsWithFlows) ->
    result = []
    totalFlowsLength = _(threadsWithFlows).flatten().size()
    flowIterators = _.map(_.times(threadsWithFlows.length, _.constant(0)), (i, idx) -> {idx: idx, i: i})
    i = 0
    while not _(flowIterators).isEmpty()
      currentTimestamp = _(flowIterators)
        .map (iter) -> threadsWithFlows[iter.idx].flow[iter.i]?.t
        .min()
      break if currentTimestamp == undefined
      for iter in flowIterators
        while threadsWithFlows[iter.idx].flow[iter.i]?.t == currentTimestamp
          result.push i: i++, n: threadsWithFlows[iter.idx].thread.number, e: threadsWithFlows[iter.idx].flow[iter.i]
          iter.i++
      flowIterators = _.filter(flowIterators, (iter) -> threadsWithFlows[iter.idx].flow[iter.i]?)
    return result

  constructor: (threadsWithFlows) ->
    @flow = buildUnifiedView(threadsWithFlows)
