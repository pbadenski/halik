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
_             = require 'lodash'
uuid          = require 'uuid'

module.exports = (http, Annotation) ->
  io = require('socket.io')(http)

  io.origins "development-browse.halik.io:* browse.halik.io:* preview.halik.io:*"
  io.on 'connection', (socket) ->
    socket.on 'load', (options, loadAnnotations) ->
      socket.join options.sessionId
      Annotation.query('session-id': options.sessionId).then ((annotations) ->
        loadAnnotations annotations
      ), (error) ->
        console.error error.message

    socket.on 'create', (annotation) ->
      Annotation.create(annotation).then (->
        io.sockets.in(annotation['session-id']).broadcast.emit 'created', annotation
      ), (error) ->
        console.error error.message

    socket.on 'update', (annotation) ->
      Annotation.update(annotation).then (->
        io.sockets.in(annotation['session-id']).broadcast.emit 'updated', annotation
      ), (error) ->
        console.error error.message

    socket.on 'delete', (annotation) ->
      Annotation.delete(annotation).then (->
        io.sockets.in(annotation['session-id']).broadcast.emit 'deleted', annotation
      ), (error) ->
        console.error error.message

