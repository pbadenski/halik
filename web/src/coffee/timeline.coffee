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
require 'jquery.scrollto'
d3 = require 'd3'
async = require 'async'

ReferenceFlow = require './domain/reference_flow'
{ByVariableNameAndValue} = require './domain/filters'

CURRENT_MARKER_WIDTH=4

module.exports = class Timeline
  constructor: ({filter, appliesAt, threadContext, stepContext, referenceFlow, unifiedView, onRemove = _.identity, editable = false}) ->
    @threadContext = threadContext
    @referenceFlow = referenceFlow
    @unifiedView = unifiedView
    @stepContext = stepContext

    @id = "timeline-" + filter.id
    unifiedViewAsNormalFlow = _.map(unifiedView.flow, "e")
    @allOccurrences =
      new Promise (resolve, reject) ->
        async.filter unifiedView.flow, ((entry, callback) ->
          appliesAt(filter)(unifiedViewAsNormalFlow, entry.e).then (occurrence) ->
            callback(null, if occurrence then entry else undefined)
        ), ((err, results) -> if err then reject(err) else resolve(results))
    @filter = filter
    @filter.onDeleted @delete
    @nameIsEditable = editable

    @root = $("<div class='timeline-row #{@id}'></div")

  __canToggleConcurrencyView: () ->
    @threadContext.others.length

  __configureTimelineLanes: (parentTimeline, {all} = {all: false}) =>
    parentTimeline.find(".timeline-labels").empty()
    parentTimeline.find(".timeline-view").empty()

    configureTimelineLane = (timelineLaneView, threadNumber) =>
      parentTimeline.find(".timeline-view").append(timelineLaneView)
      svg = d3.select(timelineLaneView[0])
      svg.attr("width", @referenceFlow.length() + CURRENT_MARKER_WIDTH)
      @threadContext.flowAPI(threadNumber).occurrences(@filter).then (occurrences) =>
        flow = _.find(@threadContext.all, {thread: {number: threadNumber}}).flow
        _(occurrences)
          .map "entry"
          .map ({i}) => @referenceFlow.toRefLookup(flow)[i]
          .each (step) => @__addHighlight(svg, step)

    if all
      _.each @threadContext.all, ({thread}, idx) =>
        label = $("<div>#{_.truncate(thread.name)}</div>")
        if thread.number == @threadContext.current.thread.number
          label.css('font-weight', 'bold')
        parentTimeline.find(".timeline-labels")
          .append(label)

      _.each @threadContext.all, ({thread}, idx) =>
        role = if thread.number == @threadContext.current.thread.number then "primary" else "secondary"
        timelineLaneView =
          $("<svg class='timeline' style='display: block'></svg>")
            .attr("data-id", @id)
            .attr("data-idx", idx)
            .attr("data-role", role)
        configureTimelineLane timelineLaneView, thread.number
    else
      timelineLaneView =
        $("<svg class='timeline' style='display: block'></svg>")
          .attr("data-id", @id)
          .attr("data-role", "primary")
      configureTimelineLane timelineLaneView, @threadContext.current.thread.number

  __addHighlight: (svg, step) ->
    svg.append("rect")
      .attr("data-step", step)
      .attr("width", "1px")
      .attr("height", "100%")
      .attr("x", step)

  __type: () ->
    if @filter.hasWatch or @filter instanceof ByVariableNameAndValue
      "modification"
    else
      "occurrence"

  delete: () =>
    @root.remove()

  __createConcurrencyViewToggle: () =>
    toggleThreads = $("<i class='fa fa-sm'/>").addClass("timeline-view--toggle-threads noselect fa-plus-square-o")
    toggleThreads.on "click",
      ({target}) =>
        toggled = @root.find(target).toggleClass("all-threads").hasClass("all-threads")
        @__configureTimelineLanes(@root, all: toggled)
        if toggled
          @root.find(target).removeClass("fa-plus-square-o").addClass("fa-minus-square-o")
        else
          @root.find(target).removeClass("fa-minus-square-o").addClass("fa-plus-square-o")
        @onStepChange undefined, @stepContext.currentStep()

  __createNavigateControlsButton = () ->
    $("<i class='top fa noselect'></span>")

  __createNavigateLeftButton: () =>
    left = __createNavigateControlsButton().addClass("fa-chevron-left timeline--header--left")
    left.on "click",
      () =>
        ga 'send', 'event', 'Timeline', 'click', 'left'
        @allOccurrences.then (entries) =>
          unifiedIndex = _(@unifiedView.flow)
            .find(n: @threadContext.current.thread.number, e: i: @stepContext.currentStep()).i
          previousEntry = _.findLast(entries, ({i}) => i < unifiedIndex)
          if previousEntry?
            @threadContext.switchByNumber previousEntry.n
            @stepContext.moveTo(previousEntry.e.i)

  __createNavigateRightButton: () =>
    right = __createNavigateControlsButton().addClass("fa-chevron-right timeline--header--right")
    right.on "click",
      () =>
        ga 'send', 'event', 'Timeline', 'click', 'right'
        @allOccurrences.then (entries) =>
          unifiedIndex = _(@unifiedView.flow)
            .find(n: @threadContext.current.thread.number, e: i: @stepContext.currentStep()).i
          nextEntry = _.find(entries, ({i}) => i > unifiedIndex)
          if nextEntry?
            @threadContext.switchByNumber nextEntry.n
            @stepContext.moveTo(nextEntry.e.i)

  __createRemoveButton: () =>
    remove = $("<i class='fa fa-sm'/>").addClass("timeline--header--remove fa-remove")
    remove.on "click", () => @filter.delete()

  onThreadChange: () ->
    isToggleThreadsOn = @root.find(".timeline-view--toggle-threads").hasClass("all-threads")
    @__configureTimelineLanes(@root, all: isToggleThreadsOn)

  render: () ->
    timelineHeader = $("<div class='header'></div>")
    variableName = $("<div class='title float-left' contenteditable='#{@nameIsEditable}'>#{@filter.name.long}</div>")
    numberOfOccurrences = $("<span class='timeline--header--number-of-modifications' />")

    toggleThreads = @__createConcurrencyViewToggle() if @__canToggleConcurrencyView()

    left  = @__createNavigateLeftButton()
    right = @__createNavigateRightButton()
    remove = @__createRemoveButton()

    @root.append(
      timelineHeader
        .append(remove)
        .append(variableName)
        .append(
          $("<span class='timeline--header--navigate'/>")
            .append(left)
            .append(right))
        .append(numberOfOccurrences)
        .append("<br style='clear: both' />"))
      .append(toggleThreads)

    Promise.all(
      _.map @threadContext.all, ({thread: {number}}) =>
        @threadContext.flowAPI(number).occurrences(@filter)
    ).then (occurrences) =>
      numberOfAllOccurrences = _(occurrences).map("length").reduce(_.add)
      @root.find('.timeline--header--number-of-modifications').html("#{numberOfAllOccurrences} #{@__type()}s")

    timelineView = $("<div class='timeline-view' style='overflow: auto'></div>")
    @root.append $("<div class='timeline-labels' style='float: left'></div>")
    @root.append(timelineView)
    @root.foundation()
    @__configureTimelineLanes(@root)

    return @root

  __createCurrentMarker: (timelineLaneView, step) ->
    height=12
    timelineLaneView.append("rect")
      .attr("class", "current")
      .attr("height", height)
      .attr("x", step)
      .attr("y", 18 - height)
      .attr("width", "#{CURRENT_MARKER_WIDTH}px")

  __updatePrimaryTimelineLane: (nextStepReferenceIndex) =>
    self = this
    d3.select(@root[0]).selectAll(".timeline[data-role='primary']").each () ->
      self.__createCurrentMarker(d3.select(this), nextStepReferenceIndex)
        .style("fill", "rgba(0, 140, 186, 1")

  __updateSecondaryTimelineLanes: (nextStepReferenceIndex) ->
    self = this
    d3.select(@root[0]).selectAll(".timeline[data-role='secondary']").each () ->
      currentMarker = self.__createCurrentMarker(d3.select(this), nextStepReferenceIndex)
      thisFlow = self.threadContext.all[parseInt($(this).attr('data-idx'))].flow
      if self.referenceFlow.fromRefLookup(thisFlow)[nextStepReferenceIndex]?
        currentMarker.classed("active", true)
      else
        nextAvailableTimestamp = thisFlow[_(self.referenceFlow.fromRefLookup(thisFlow)).drop(nextStepReferenceIndex).find()]?.ts
        if nextAvailableTimestamp?
          currentMarker.classed("waiting", true)

          currentTimestamp = self.referenceFlow.timestampAt(nextStepReferenceIndex)
          howFarFromNextExecution = nextAvailableTimestamp - currentTimestamp
          d3.select(this).append("text")
            .attr("class", "current")
            .attr("x", nextStepReferenceIndex + 10)
            .attr("y", 10)
            .style("font-size", "75%")
            .text("#{howFarFromNextExecution}ms -->")
        else
          currentMarker.classed("finished", true)

  onStepChange: (previousStep, nextStep) =>
    # delete current marker in all timeline lanes
    d3.select(@root[0]).selectAll(".timeline .current").remove()

    nextStepReferenceIndex = @referenceFlow.toRefLookup(@threadContext.current.flow)[nextStep]
    @__updatePrimaryTimelineLane(nextStepReferenceIndex)
    @__updateSecondaryTimelineLanes(nextStepReferenceIndex)
    
    # scroll timeline view to current marker
    @root.find(".timeline-view").each(() -> $(this).scrollTo $(this).find(".current"))

