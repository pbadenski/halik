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
annotator = require 'annotator'
uuid = require 'uuid'

codemirror_support = require './codemirror-support'

commentsUIUpdater = require './comments-ui-updater'
# @ifdef PLUGIN
annotatorLocalStorage = require './local-storage'
# @endif
# @ifdef WEBSITE
socketIOStorage = require './socketio-storage'
# @endif

module.exports = (body) -> class
  stepInformationAdder = (options = {}) ->
    console.error("stepContext is required") if not options.stepContext?

    beforeAnnotationCreated: (annotation) ->
      annotation.step = options.stepContext.currentStep()

  metadataAdder = (options = {}) ->
    beforeAnnotationCreated: (annotation) ->
      _.merge annotation, options

  configureAnnotator = (mainOptions, target, stepContext, sessionId) ->
    instance = new annotator.App()
    instance.include annotator.ui.main, mainOptions
    instance.include commentsUIUpdater, stepContext: stepContext, target: target, sessionId: sessionId
    instance.include metadataAdder, target: target, "session-id": sessionId
    instance.include stepInformationAdder, stepContext: stepContext
    # @ifdef PLUGIN
    instance.include annotatorLocalStorage, sessionId: sessionId, target: target
    # @endif
    # @ifdef WEBSITE
    instance.include socketIOStorage, sessionId: sessionId, target: target
    # @endif
    instance.start()
    instance: instance
    onNewStep: (step) ->
      $(body).find(mainOptions.element).find(".annotator-hl").each (idx, item) -> $(body).find(item).replaceWith(item.childNodes)
      instance.annotations.load step: step, target: target, "session-id": sessionId

  configureStandardAnnotator: (root, target, stepContext, sessionId) ->
    {instance, onNewStep} = configureAnnotator {element: root[0]}, target, stepContext, sessionId
    stepContext.addListener onStepChange: (previousStep, nextStep) -> onNewStep(nextStep)
    instance

  configureEditorAnnotator: (stepContext, sessionId) ->
    mainOptions =
      element: $(body).find(".CodeMirror")[0],
      textselector: codemirror_support.TextSelector
      highlighter: codemirror_support.Highlighter
    configureAnnotator mainOptions, "editor", stepContext, sessionId

