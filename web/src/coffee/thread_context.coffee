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

FlowAPI = require './api/flow_api'
StepperAPI = require './api/stepper_api'
SessionAPI = require './api/session_api'

module.exports = class
  constructor: (@all) ->
    @listeners = []
    @sessionAPI = memoize(@sessionAPI.bind(@))

  addListener: (component) =>
    @listeners.push(component)

  notifyListeners: (newThreadWithFlow) ->
    _(@listeners)
      .each (listener) -> listener.onThreadChange newThreadWithFlow

  assignThread: (currentThreadNumber) ->
    [[@current], @others] = _.partition(@all, {thread: {number: currentThreadNumber}})
    @notifyListeners(@current)
    return @current

  sessionAPI: ->
    new SessionAPI(@all)

  flowAPI: (threadNumber = @current.thread.number) ->
    new FlowAPI(_.find(@all, {thread: { number: threadNumber}}).flow)

  stepperAPI: (flow = @current.flow) ->
    new StepperAPI(flow)

  switchByNumber: (number) ->
    if @current.thread.number != number
      @assignThread(number)
    return @current

