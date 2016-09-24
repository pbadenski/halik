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
uuid = require 'uuid'

module.exports = ({target, sessionId}) ->
  configure: (registry) ->
    registry.registerUtility this, 'storage'

  start: (app) ->
    annotations = JSON.parse(localStorage.getItem "annotations")
    app.annotations.runHook "annotationsLoaded", [_.filter(annotations, target: target, "session-id": sessionId)]

  create: (annotation) ->
    if not annotation.id?
      annotation.id = uuid.v4()
    previousAnnotations = JSON.parse(localStorage.getItem "annotations")
    localStorage.setItem(
      "annotations",
      JSON.stringify((previousAnnotations or []).concat(annotation))
    )
    annotation

  update: (annotation) ->
    annotations = JSON.parse(localStorage.getItem "annotations")
    previousAnnotation = _.find annotations, id: annotation.id
    previousAnnotation.text = annotation.text
    localStorage.setItem "annotations", JSON.stringify(annotations)
    annotation

  delete: (annotation) ->
    annotations = JSON.parse(localStorage.getItem "annotations")
    _.remove annotations, id: annotation.id
    localStorage.setItem "annotations", JSON.stringify(annotations)
    annotation

  query: (filter) ->
    all = _.filter JSON.parse(localStorage.getItem("annotations")), filter
    results: all
    meta: {total: all.length}

