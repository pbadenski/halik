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
module.exports = class StepperAPI
  constructor: (@flow) ->

  stepOver: (fromStep) ->
    currentFlowEntry = @flow[fromStep]
    # next step within current method
    nextStepWithinCurrentMethod = _(@flow)
      .drop fromStep + 1
      .find(_.pick(currentFlowEntry, "c", "m", "sd"))?.i

    # step out to the first line after original method call
    nextStepToTheFirstLineAfterThisMethodCall = _(@flow)
      .drop fromStep + 1
      .find(({sd}) -> sd < currentFlowEntry.sd)?.i

    if nextStepWithinCurrentMethod? and nextStepToTheFirstLineAfterThisMethodCall?
      nextStep = Math.min(nextStepWithinCurrentMethod, nextStepToTheFirstLineAfterThisMethodCall)
    else if nextStepWithinCurrentMethod?
      nextStep = nextStepWithinCurrentMethod
    else if nextStepToTheFirstLineAfterThisMethodCall?
      nextStep = nextStepToTheFirstLineAfterThisMethodCall

    if not nextStep?
      nextStep = @flow[fromStep + 1]?.i
    nextStep

  stepBack: (fromStep) ->
    currentFlowEntry = @flow[fromStep]
    return if (isFirstStep = => not @flow[fromStep - 1]?)()
    previousStepWithinCurrentMethod = _(@flow)
      .take fromStep
      .findLast(_.pick(currentFlowEntry, "sd", "c", "m"))?.i

    # step out to the first line after original method call
    previousStepToTheFirstLineAfterThisMethodCall = _(@flow)
      .take fromStep
      .findLast(({sd}) -> sd < currentFlowEntry.sd)?.i

    if previousStepWithinCurrentMethod? and previousStepToTheFirstLineAfterThisMethodCall?
      previousStep = Math.max(previousStepWithinCurrentMethod, previousStepToTheFirstLineAfterThisMethodCall)
    else if previousStepWithinCurrentMethod?
      previousStep = previousStepWithinCurrentMethod
    else if previousStepToTheFirstLineAfterThisMethodCall?
      previousStep = previousStepToTheFirstLineAfterThisMethodCall

    if previousStep?
      previousStep
    else
      @stepBack(@stepOut(fromStep))

  stepIn: (fromStep) ->
    @flow[fromStep + 1]?.i

  stepOut: (fromStep) ->
    currentFlowEntry = @flow[fromStep]
    _.findLast(_.take(@flow, fromStep), ({sd}) -> sd < currentFlowEntry.sd)?.i

