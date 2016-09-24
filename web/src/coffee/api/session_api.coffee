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
memoize = require('memoizee')

module.exports = class SessionAPI
  constructor: (@allThreadsWithFlows) ->
    @variableNames = memoize(@variableNames.bind(@))
    @classes = memoize(@classes.bind(@))

  title: ->
    firstFlowEntry = _.find(@allThreadsWithFlows, thread: number: 1).flow[0]
    [packageName..., className] = firstFlowEntry.c.split("/")
    packageName = packageName.join(".")
    return "#{className} (#{packageName})"

  variableNames: ->
    _(@allThreadsWithFlows)
      .map("flow")
      .flatten()
      .filter("sn")
      .map("sn")
      .flatten()
      .map ({n}) -> n
      .uniq()

  classes: ->
    _(@allThreadsWithFlows)
      .map("flow")
      .flatten()
      .map("c")
      .uniq()
      .map (className) -> className.replace(/\//g, '.').split('$')[0]
      .uniq()
      .value()

  findInClass: (cls, line) ->
    _(@allThreadsWithFlows)
      .map("flow")
      .flatten()
      .find(l: line, c: cls)
