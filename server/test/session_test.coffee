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

describe 'Session', ->

  describe '#upload', ->
    zippedFile = 'UEsDBAoAAAAAAM6Uv0jGNbk7BQAAAAUAAAADABwAZm9vVVQJAAMUzE1XeMtNV3V4CwABBPUBAAAEAAAAAHRlc3QKUEsBAh4DCgAAAAAAzpS/SMY1uTsFAAAABQAAAAMAGAAAAAAAAQAAAKSBAAAAAGZvb1VUBQADFMxNV3V4CwABBPUBAAAEAAAAAFBLBQYAAAAAAQABAEkAAABCAAAAAAA='
    
    it 'succeeds assuming collaborators dont throw syntax errors', (done) ->
      elasticsearch =
        indices: create: ->
        create: (entry) ->
          entry.should.have.property("id")
          Promise.resolve()
      s3 =
        put: () -> new require('stream').Duplex write: (->), read: (->)
      Session = (require '../domain/session')(elasticsearch, s3)

      Session
        .upload {id: "1", zipContent: [new Buffer(zippedFile, 'base64')]}
        .then(
          -> done(),
          (err) -> done(err)
        )

  describe '#forUser', ->
    it 'should copy _source contents and _id from the responses', (done) ->
      elasticsearch =
        indices: create: ->
        search: () ->
          Promise.resolve hits: hits: [_source: { some: 1 }, _id: 2]
      Session = (require '../domain/session')(elasticsearch)

      Session.forUser 1
        .then(
           (list) ->
             try
               list[0].should.have.property("some").and.be.equal(1)
               list[0].should.have.property("id").and.be.equal(2)
               done()
             catch err
               done(err)
          (err) -> done(err)
        )

    it 'skips the user property in the result', (done) ->
      elasticsearch =
        indices: create: ->
        search: () ->
          Promise.resolve hits: hits: [ _source: user: "testuser"]
      Session = (require '../domain/session')(elasticsearch)

      Session.forUser 1
        .then(
          (list) ->
            try
              list[0].should.not.have.property "user"
              done()
            catch err
              done(err)
          (err) -> done(err)
        )

  describe '#last', ->
    it 'should copy _source contents and _id from the responses', (done) ->
      elasticsearch =
        indices: create: ->
        search: () ->
          Promise.resolve hits: hits: [_source: { some: 1 }, _id: 2]
      Session = (require '../domain/session')(elasticsearch)

      Session.last 1
        .then(
           (list) ->
             try
               list[0].should.have.property("some").and.be.equal(1)
               list[0].should.have.property("id").and.be.equal(2)
               done()
             catch err
               done(err)
          (err) -> done(err)
        )
