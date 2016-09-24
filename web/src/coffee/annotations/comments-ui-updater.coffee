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
require 'linkifyjs'
linkifyHtml = require('linkifyjs/html')

{escapeHtml} = require '../utils'

module.exports = (options = {}) ->
  console.error("stepContext is required") if not options.stepContext?
  console.error("sessionId is required") if not options.sessionId?
  console.error("target is required") if not options.target?

  annotationCreated: (annotation) ->
    if $(".annotation-#{annotation.id}").length
      return

    annotationElement =
      $("<div class='annotation annotation-#{annotation.id}' />")
        .click () ->
          options.stepContext.moveTo annotation.step
        .hover(
          (() -> $(".annotator-hl[data-annotation-id=#{annotation.id}]").addClass "highlighted"),
          (() -> $(".annotator-hl[data-annotation-id=#{annotation.id}]").removeClass "highlighted")
        )
        .attr "data-step", annotation.step
        .append("<div class='text'>#{linkifyHtml(escapeHtml(annotation.text))}</div>" +
                "<small style='float: right'>Step #{annotation.step + 1}</small>" +
                "<br style='clear: both' />")
    lastAnnotation =
      $(".annotations .annotation")
        .filter () -> parseInt($(this).attr("data-step")) <= annotation.step
        .last()
    if lastAnnotation.length == 0
      annotationElement.prependTo $(".annotations")
    else
      annotationElement.insertAfter lastAnnotation
    annotation

  annotationUpdated: (annotation) ->
    $(".annotation-#{annotation.id} .text").text(escapeHtml(annotation.text))
    annotation

  annotationDeleted: (annotation) ->
    $(".annotation-#{annotation.id}").remove()

  annotationsLoaded: (annotations) ->
    _.each annotations, (annotation) => @annotationCreated(annotation)

