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
module.exports = class StackTrace
  constructor: (root, @stepContext, @threadContext) ->
    @root = root.empty().off()

  assignFlow: () ->
    @root.empty().off()
    this

  onStepChange: (previousStep, nextStep) ->
    @root.empty()
    @threadContext.flowAPI().frames(nextStep)
      .reverse()
      .map (entry) ->
        if entry
          [packageName..., className] = entry.c.split("/")
          packageName = packageName.join(".")
          idx: entry.i, text: "#{entry.m}():#{entry.l}, #{className} <i>(#{packageName})</i>"
        else
          undefined
      .reject _.isUndefined
      .each ({idx, text}) =>
        stackTraceEntry = $("<div class='stack-trace--element'>#{text}</div>")
        if idx?
          stackTraceEntry.click (e) =>
            ga 'send', 'event', 'Stack trace', 'click'
            @stepContext.moveTo idx, except: this
            @root.find("div").removeClass "selected"
            $(e.target).closest(".stack-trace--element").addClass "selected"
          stackTraceEntry.dblclick (e) =>
            @stepContext.moveTo idx
        @root.append(stackTraceEntry)
      $(_.first(@root.find("div"))).addClass "selected"

