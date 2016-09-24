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
chai   = require 'chai'
should = chai.should()


describe 'Annotation', ->

  describe '#query', ->
    it 'succeeds assuming collaborators dont throw syntax errors', (done) ->
      elasticsearch =
        indices: create: ->
        search: () ->
          Promise.resolve(hits: hits: [_source: ranges: "[]"])

      Annotation = (require '../domain/annotation')(elasticsearch)
      Annotation.query just: "test query"
        .then(
          -> done(),
          (err) -> done(err)
        )

  describe '#create', ->
    it 'succeeds assuming collaborators dont throw syntax errors', (done) ->
      elasticsearch =
        indices: create: ->
        create: () ->
          Promise.resolve()

      Annotation = (require '../domain/annotation')(elasticsearch)
      Annotation.create(ranges: [])
        .then(
          -> done(),
          (err) -> done(err)
        )

  describe '#update', ->
    it 'succeeds assuming collaborators dont throw syntax errors', (done) ->
      elasticsearch =
        indices: create: ->
        update: () ->
          Promise.resolve()

      Annotation = (require '../domain/annotation')(elasticsearch)
      Annotation.update(id: 1, text: "some")
        .then(
          -> done(),
          (err) -> done(err)
        )

  describe '#delete', ->
    it 'succeeds assuming collaborators dont throw syntax errors', (done) ->
      elasticsearch =
        indices: create: ->
        delete: () ->
          Promise.resolve()

      Annotation = (require '../domain/annotation')(elasticsearch)
      Annotation.delete(id: 1)
        .then(
          -> done(),
          (err) -> done(err)
        )
