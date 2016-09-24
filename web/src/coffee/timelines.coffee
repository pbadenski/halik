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

ReferenceFlow = require './domain/reference_flow'
UnifiedView = require './domain/unified_view'
Timeline = require './timeline'

module.exports = class Timelines
  constructor: (root, @threadContext, @stepContext, @flowAPI) ->
    @root = root.off()
    @timelines = []
    @referenceFlow = new ReferenceFlow @threadContext.all
    @unifiedView = new UnifiedView @threadContext.all

  assignThread: (currentThreadNumber) ->
    @timelines.forEach (t) -> t.onThreadChange()

  onStepChange: (previousStep, nextStep) =>
    @timelines.forEach (t) -> t.onStepChange previousStep, nextStep

  add: (filter) =>
    timeline = new Timeline
      threadContext: @threadContext
      stepContext: @stepContext
      referenceFlow: @referenceFlow
      unifiedView: @unifiedView
      filter: filter
      appliesAt: @threadContext.flowAPI().appliesAt
      editable: filter.nameIsEditable

    @timelines.push timeline
    filter.onDeleted () => _.remove @timelines, id: timeline.id
    timelineRow = timeline.render()
    @root.prepend timelineRow
    @onStepChange undefined, @stepContext.currentStep()

    return timeline
