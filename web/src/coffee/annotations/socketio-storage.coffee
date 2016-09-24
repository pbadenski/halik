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
# @ifdef WEBSITE
uuid = require 'uuid'
socket = io()

module.exports = ({sessionId, target}) ->
  console.error("sessionId is required") if not sessionId?
  console.error("target is required") if not target?
  storage = []

  start: (app) ->
    socket.emit 'load', {sessionId: sessionId}, (annotations) ->
      app.annotations.runHook "annotationsLoaded", [_.filter(annotations, target: target)]
    socket.on 'created', (annotation) ->
      if annotation.target != target
        return
      app.annotations.runHook "annotationCreated", [annotation]
    socket.on 'updated', (annotation) ->
      if annotation.target != target
        return
      localAnnotation = _.find storage, id: annotation.id
      localAnnotation.text = annotation.text
      app.annotations.runHook "annotationUpdated", [localAnnotation]
    socket.on 'deleted', (annotation) ->
      if annotation.target != target
        return
      localAnnotation = _.find storage, id: annotation.id
      app.annotations.runHook "annotationDeleted", [localAnnotation]

  annotationsLoaded: (annotations) ->
    storage = _.uniqBy storage.concat(annotations), "id"

  annotationCreated: (annotation) ->
    storage = storage.concat(annotation)

  annotationUpdated: (annotation) ->
    (_.find storage, id: annotation.id).text = annotation.text

  annotationDeleted: (annotation) ->
    _.remove storage, id: annotation.id

  configure: (registry) ->
    registry.registerUtility this, 'storage'
  create: (annotation) ->
    if not annotation.id?
      annotation.id = uuid.v4()
    socket.emit 'create', annotation
    annotation
  update: (annotation) ->
    socket.emit 'update', annotation
    annotation
  delete: (annotation) ->
    socket.emit 'delete', annotation
    annotation
  query: (filter) ->
    all = _.filter storage, filter
    results: all
    meta: {total: all.length}
# @endif

