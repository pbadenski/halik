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
module.exports = class StepContext
  constructor: (@threadContext) ->
    @context = $("<context></context>")
    @listeners = []

  assignFlow: () ->
    @context.removeData('currentStep')
    this

  currentStep: () ->
    @context.data('currentStep')

  moveTo: (step, {except} = {}) ->
    return unless step?
    @notifyListeners @context.data('currentStep'), step, except: except
    @context.data 'currentStep', step

  notifyListeners: (previousStep, nextStep, {except} = {}) ->
    $(".step-counter").text "Step #{nextStep + 1}"
    _(@listeners)
      .filter (l) -> l != except
      .each (listener) -> listener.onStepChange previousStep, nextStep

  stepOver: () =>
    @moveTo(@threadContext.stepperAPI().stepOver(@currentStep()))

  stepBack: () =>
    previousStep = @currentStep()
    nextStep = @threadContext.stepperAPI().stepBack(@currentStep())
    @moveTo nextStep

  stepIn: () =>
    @moveTo @threadContext.stepperAPI().stepIn(@currentStep())

  stepOut: () =>
    previousStep = @currentStep()
    nextStep = @threadContext.stepperAPI().stepOut(@currentStep())
    if nextStep?
      @context.data 'currentStep', nextStep
    @notifyListeners(previousStep, @currentStep())

  stepToNext: () =>
    nextStep = @currentStep() + 1
    return if nextStep > @threadContext.flowAPI().length() - 1
    @moveTo(nextStep)

  stepToPrevious: () =>
    previousStep = @currentStep() - 1
    if previousStep >= 0
      @moveTo(previousStep)

  addListener: (component) =>
    @listeners.push(component)

