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
require 'codemirror/mode/clike/clike'
require 'codemirror/mode/clojure/clojure'
CodeMirror = require 'codemirror'
alertify = require 'alertify.js'

CodeMirror.defineExtension 'centerOnLine', (line) ->
  h = @getScrollInfo().clientHeight
  coords = @charCoords({
    line: line
    ch: 0
  }, 'local')
  @scrollTo null, (coords.top + coords.bottom - h) / 2

class BreakpointView
  constructor: (breakpoint) ->
    breakpoint.view = this
    breakpoint.onDeleted () => @delete()
    @line = breakpoint.criterion.key.line

  delete: () =>
    @document.removeLineClass parseInt(@line) - 1, "gutter", "line-breakpoint"

  render: (document) =>
    @document = document
    @document.addLineClass parseInt(@line) - 1, "gutter", "line-breakpoint"

CodeMirror.decorateMode = (base, decorator) ->
  startState: base.startState
  token: (stream, state) ->
    (decorator.onToken or ((input) -> input)) base.token(stream, state), stream, state
  indent: (state, textAfter) ->
    (decorator.onIndent or ((input) -> input)) base.indent(state, textAfter), state, textAfter
  electricChars: base.electricChars
  blankLine: base.blankLine

module.exports = (body) -> class
  {ByReference, ByVariableName, ByLineInClass} = require './domain/filters'
  AnnotationsSetup = (require './annotations/setup')(body)

  constructor: (root, filters, stepContext, sessionId, @threadContext) ->
    @root = root
    @filters = filters
    @sessionId = sessionId
    @lastStepChangeUpdate = Promise.resolve()
    self = this
    _.each ["java", "kotlin", "clojure"], (language) ->
      CodeMirror.defineMode "text/x-#{language} + recorded", (config, parserConfig) ->
        CodeMirror.decorateMode(
          CodeMirror.getMode(config, "text/x-#{language}"),
          onToken: (token, stream, state) ->
            if token == "variable" and self.threadContext.sessionAPI().variableNames().includes stream.current()
              token += " recorded"
            token
        )
    @codeMirror = CodeMirror(root.empty().get(0),
      flattenSpans: false
      lineNumbers: true
      matchBrackets: true
      readOnly: "nocursor"
      mode: 'text/x-java + recorded')
    registerWatch = =>
      word = @codeMirror.doc.getSelection()
      if @threadContext.sessionAPI().variableNames().includes(word)
        thisForMethod = @threadContext.flowAPI().findThisForCurrentStep(stepContext.currentStep())
        if thisForMethod?
          ref = @threadContext.flowAPI().resolveNameInThis stepContext.currentStep(), word, thisForMethod.id
          if ref?
            @filters.register new ByReference(ref, word)
          else
            @filters.register new ByVariableName(word)
        else
          @filters.register new ByVariableName(word)

        @codeMirror.doc.undoSelection()
    @codeMirror.on "touchstart", () ->
      registerWatch()
    @codeMirror.on "dblclick", () ->
      registerWatch()
    @codeMirror.on "gutterClick", (instance, line) =>
      @__onGutterClick(line)
    @loadAnnotationsOnStepChange = (new AnnotationsSetup().configureEditorAnnotator stepContext, sessionId).onNewStep

  __onGutterClick: (zeroBasedLine) =>
    actualLine = zeroBasedLine + 1
    breakpoint = @filters.allOfType(ByLineInClass).find criterion: key: { line: actualLine, class: @currentClassName }
    if breakpoint?
      ga 'send', 'event', 'Breakpoint', 'delete'
      breakpoint.delete()
    else
      method = @threadContext.sessionAPI().findInClass(@currentClassName, actualLine)?.m
      if method
        ga 'send', 'event', 'Breakpoint', 'add'

        @filters.register(new ByLineInClass line: actualLine, class: @currentClassName).then (breakpoint) =>
          breakpointView = new BreakpointView breakpoint
          breakpointView.render @codeMirror.getDoc()


  assignFlow: () ->
    @codeMirror.setValue("")

  __loadEditorIfRequired: (previousStep, nextStep) =>
    previousClass = @threadContext.flowAPI().at(previousStep)?.c
    nextClass = @threadContext.flowAPI().at(nextStep).c
    if previousClass == nextClass
      Promise.resolve()
    else
      @__loadEditor nextClass

  __actionsOnStepChange: (nextStep) =>
    @codeMirror.addLineClass @threadContext.flowAPI().at(nextStep).l - 1, 'background', 'line-highlight'
    @codeMirror.centerOnLine @threadContext.flowAPI().at(nextStep).l - 1
    @loadAnnotationsOnStepChange nextStep

  __loadEditor: (className) =>
    @currentClassName = className
    classNameInSourceFormat = className.replace(/\//g, '.')
    mainClass = classNameInSourceFormat.split('$')[0]
    Promise.resolve($.get "/session/#{@sessionId}/sources/" + mainClass)
      .catch () =>
        # try to fall back to full name before failing - some times helps
        Promise.resolve($.get "/session/#{@sessionId}/sources/" + classNameInSourceFormat)
      .then (data) =>
        if mainClass.endsWith "Kt"
          language = "kotlin"
        else if /^\s*\(/.test(data) or /^\s*[;\(]/.test(data)
          language = "clojure"
        else
          language = "java"
        newMode = "text/x-#{language} + recorded"
        @codeMirror.operation =>
          if @codeMirror.getOption("mode") != newMode
            @codeMirror.setValue ""
            @codeMirror.setOption("mode", newMode)
          @codeMirror.setValue data
        @filters
          .allOfType(ByLineInClass)
          .each (breakpoint) =>
            if breakpoint.criterion.key.class == className
              breakpoint.view.render @codeMirror.getDoc()
      .catch (e) ->
        if e.status == 404
          # @ifdef PLUGIN
          errorMessage = "Source for `#{mainClass}` could not be loaded. Make sure IntelliJ is running, and your project is open."
          # @endif
          # @ifdef WEBSITE
          errorMessage = "Source for `#{mainClass}` could not be loaded."
          # @endif
          alertify
            .closeLogOnClick(true)
            .delay(0)
            .error errorMessage
        else
          console.error e
          alertify
            .closeLogOnClick(true)
            .delay(0)
            .error "Unknown error, please tweet us you're having this problem."

  onStepChange: (previousStep, nextStep) =>
    @lastStepChangeUpdate = @lastStepChangeUpdate.then =>
      @__loadEditorIfRequired(previousStep, nextStep)
    .then () =>
      if previousStep?
        @codeMirror.removeLineClass @threadContext.flowAPI().at(previousStep).l - 1, 'background', 'line-highlight'
      @__actionsOnStepChange(nextStep)
