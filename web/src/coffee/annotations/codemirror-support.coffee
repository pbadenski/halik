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

module.exports.TextSelector = class
  constructor: (element, options) ->
    this.onSelection = options.onSelection
    doc = element.CodeMirror.doc
    element.CodeMirror.on "beforeSelectionChange", (instance, {ranges}) =>
      nothingSelected = _.every ranges, ({anchor, head}) -> anchor == head
      if nothingSelected
        this.onSelection []
    $(element).mouseup (event) =>
      if not doc.somethingSelected()
        this.onSelection [], event
        return

      selections = _(doc.listSelections())
        .map (each) ->
          text: () -> ""
          serialize: () -> each
        .value()
      this.onSelection selections, event

module.exports.Highlighter = class extends annotator.ui.highlighter.Highlighter
  constructor: (element, options) ->
    super element, options
    @doc = element.CodeMirror.doc
    @metadata = {}

  reanchorRange: (range, rootElement) -> range

  highlightRange: (range, cssClass) ->
    id = _.random(0, 1, true).toString().replace(".", "")
    backwardsSelection =
      range.head.line < range.anchor.line or
      (range.head.line == range.anchor.line and range.head.ch < range.anchor.ch)
    if backwardsSelection
      marker = @doc.markText range.head, range.anchor, className: "annotator-hl annotation-#{id}"
    else
      marker = @doc.markText range.anchor, range.head, className: "annotator-hl annotation-#{id}"
    _.set @metadata, "annotation-#{id}.marker", marker
    $(".annotation-#{id}")

  extractAnnotationIdClass = (annotation) ->
    _(annotation.className.split(" ")).find (name) -> name.startsWith("annotation-")

  fixHighlights: () ->
    _($(@element)
      .find("[class*='annotation-']").not("[data-annotation-id]")
      .map (idx, annotation) -> extractAnnotationIdClass(annotation))
      .uniq()
      .map (idClass) =>
        @connectAnnotationToHighlights @metadata[idClass].annotation, $(@element).find(".#{idClass}")
      .commit()

  draw: (annotation) ->
    highlights = super annotation
    _.set @metadata,
      "#{extractAnnotationIdClass(_.first($(highlights)))}.annotation",
      annotation
    @fixHighlights()
    highlights

  redraw: (annotation) ->
    super annotation
    @fixHighlights()

  undraw: (annotation) ->
    super annotation
    @fixHighlights()

  undrawHighlights: (highlights) ->
    idClass = extractAnnotationIdClass(_.first($(highlights)))
    @metadata[idClass].marker.clear()
    delete @metadata[idClass]
