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
Cookies  = require 'js-cookie'
Route    = require 'route-parser'
hasher   = require 'hasher'

settings = require './settings'

class History
  constructor: (@stepContext, @filters) ->

  load: (newHash, oldHash) =>
    if not newHash
      @stepContext.moveTo 0
    else
      context = JSON.parse(atob(newHash))
      @stepContext.moveTo context.s
      @filters.registerAll _.map(context.f, ({n, c}) -> name: n, criterionKey: c)
      if context.c then $(".toggle-annotations").click()

  save: ->
    hasher.changed.active = false
    context =
      s: @stepContext.currentStep()
      f: @filters.filter().map( (each) -> n: each.name.long, c: each.criterion.key ).value()
      c: $(".toggle-annotations").is(".selected")
    hasher.setHash btoa(JSON.stringify(context))
    hasher.changed.active = true

module.exports = (document, api) -> class
  alertify = require 'alertify.js'
  alertify.parent(document.body)

  SessionLoader    = (require './session_loader')(api, document.body)
  StepContext      = require './domain/step_context'
  FlowAPI          = require './api/flow_api'
  StepperAPI       = require './api/stepper_api'

  Editor           = (require './editor')(document.body)
  ThreadContext    = require './thread_context'
  Timelines        = require './timelines'
  Watches          = require './watches'
  Slider           = require './slider'
  StackTrace       = require './stack_trace'
  FindOccurrences  = (require './find_occurrences')(document.body)
  QuickNavigation  = require './quick_navigation'
  ThreadView       = require './thread_view'
  ThreadList       = require './thread_list'
  # @ifdef PLUGIN
  ShareSession     = (require './share_session')(api, document.body)
  # @endif
  QuickTour        = require './quick_tour'
  Filters          = require './domain/filters'
  AnnotationsSetup = require './annotations/setup'

  configureToggleAnnotations = ->
    $(document.body).find(".toggle-annotations").click () ->
      $(document.body).find(".off-canvas").toggleClass "reveal-for-all"
      $(document.body).find(".toggle-annotations").toggleClass "selected"

  configurePermalink = (history) ->
    button = $(".permalink")
    displayPermalinkInformation = (dialog, sessionId) ->
      dialog.css "width", "600px"
      dialog.empty()
      dialog.append [
        $("<input class='url' type='text' readonly value='#{document.location.href}'></input>").click( -> $(this).select())
        $("<a class='button'>OK</a></div>").click(-> button.removeClass("disabled"); $(this).parent().remove())
      ]
    button.click () ->
      history.save()
      button.addClass("disabled")
      $(document.body).append displayPermalinkInformation($("<div class='share-session-dialog' style='width: 400px'>"))

  runIntroOnFirstVisit = () ->
    isFirstVisit = not Cookies.get("isFirstVisit")?

    if isFirstVisit
      new QuickTour().start()
    
    wayInTheFuture = new Date(new Date().getTime() + 7776000000) # 200 years
    Cookies.set "isFirstVisit", "no", { expires: wayInTheFuture }

  load: ->
    configureToggleAnnotations()

    {sessionId} = new Route('/browse/:sessionId').match(document.location.pathname)
    new SessionLoader().load(sessionId)
      .then (threadsWithFlows) ->
        threadContext = new ThreadContext threadsWithFlows
        stepContext   = new StepContext threadContext

        timelines  = new Timelines $(document.body).find(".timelines"), threadContext, stepContext, threadContext
        $(document.body).find(".watches-panel").foundation()
        watches    = new Watches $(document.body).find(".watches"), threadContext
        filters    = new Filters stepContext, threadContext, timelines, watches
        stackTrace = new StackTrace $(document.body).find(".stack-trace"), stepContext, threadContext
        editor     = new Editor $(document.body).find(".editor"), filters, stepContext, sessionId, threadContext
        slider     = new Slider $(document.body).find("#slider"), stepContext, threadContext

        history = new History stepContext, filters
        hasher.changed.add history.load
        hasher.initialized.add history.load
        configurePermalink(history)

        _.each [watches, editor, timelines, stackTrace, slider], stepContext.addListener

        new FindOccurrences $(document.body).find(".search.find-occurrences input"), filters
        quickNavigation = new QuickNavigation $(document.body).find(".quick-navigation"), stepContext, threadContext

        # @ifdef PLUGIN
        new ShareSession $(document.body).find(".share-session"), sessionId, threadContext
        # @endif

        threadContext.addListener
          onThreadChange: ({flow, thread: {number}}) ->
            timelines.assignThread number

            slider.assignFlow()
            stepContext.assignFlow()
            editor.assignFlow()
            stackTrace.assignFlow()
            quickNavigation.assignFlow()

            if _.isEmpty flow
              alertify
                ._$$alertify.notify "Nothing recorded in this thread.", "warn"
            else
              stepContext.moveTo 0
        
        new ThreadList ThreadView, stepContext, threadContext

        threadToLoad = threadsWithFlows[0]
        if _.isEmpty threadToLoad?.flow
          alertify
            .delay(5000)
            .closeLogOnClick(true)
            ._$$alertify.notify "Nothing recorded in this thread. Check other threads or see <a href='/docs/recording.html' target='_blank'>the documentation</a>", "warn"
        else
          new ThreadView threadsWithFlows[0].thread.number, stepContext, threadContext

          document.title = "Explore: #{_.last(threadToLoad?.flow[0]?.c.split("/"))}"
          ga('send', 'pageview')

          hasher.init()

        runIntroOnFirstVisit()

