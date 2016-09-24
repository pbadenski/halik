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
chai.use(require('chai-string'))
sinon  = require 'sinon'

request = require 'supertest'

describe 'GET /session', ->
  app  = undefined

  beforeEach ->
    app    = (require 'express')()
    app.set 'port', 33445

  it 'should return 200 and empty list when s3 returns nothing', (done) ->
    s3mock =
      list: (options, callback) -> callback(null, null)
    (require '../sessions')(app, s3mock)

    request(app)
      .get "/session"
      .expect []
      .expect 200, done

  it 'should return 200 and list of sessions when s3 returns sessions', (done) ->
    s3mock =
      list: (options, callback) ->
        callback null, CommonPrefixes: [Prefix: 1]
    (require '../sessions')(app, s3mock)

    request(app)
      .get "/session"
      .end (err, res) ->
        res.statusCode.should.equal 200
        json = res.body

        json.length.should.equal 1
        json[0].id.should.equal 1
        json[0].link.should.endWith "session/1"
        done()

describe 'GET /session/top', ->
  app  = undefined

  beforeEach ->
    app    = (require 'express')()
    app.set 'port', 33445

  it 'should return 200 and empty results for no results from db', (done) ->
    sessionMock =
      last: () -> Promise.resolve([])
    (require '../sessions')(app, null, sessionMock)

    request(app)
      .get "/session/top"
      .end (err, res) ->
        res.statusCode.should.equal 200
        json = res.body

        json.should.deep.equal []
        done()

  it 'should return 200 and results for existing results from db', (done) ->
    sessionMock =
      last: () -> Promise.resolve([1, 2])
    (require '../sessions')(app, null, sessionMock)

    request(app)
      .get "/session/top"
      .end (err, res) ->
        res.statusCode.should.equal 200
        json = res.body

        json.length.should.equal 2
        done()

describe 'PUT /session/:id', ->
  app  = undefined
  zippedFile = 'UEsDBAoAAAAAAM6Uv0jGNbk7BQAAAAUAAAADABwAZm9vVVQJAAMUzE1XeMtNV3V4CwABBPUBAAAEAAAAAHRlc3QKUEsBAh4DCgAAAAAAzpS/SMY1uTsFAAAABQAAAAMAGAAAAAAAAQAAAKSBAAAAAGZvb1VUBQADFMxNV3V4CwABBPUBAAAEAAAAAFBLBQYAAAAAAQABAEkAAABCAAAAAAA='

  beforeEach ->
    app    = (require 'express')()
    app.set 'port', 33445

  it 'should block requests with empty origin', (done) ->
    (require '../sessions')(app, null, null)

    request(app)
      .put "/session/1"
      .expect 403, done

  it 'should block requests with some internet origin', (done) ->
    (require '../sessions')(app, null, null)

    request(app)
      .put "/session/1"
      .set 'origin', 'google.com'
      .expect 403, done

  it 'should return 202 when origin matches, user has been authenticated, and title provided', (done) ->
    app.use (req, res, next) ->
      req.isAuthenticated = () -> true
      req.user = "mr.tester"
      next()
    (require '../sessions')(app, null, upload: ->)

    request(app)
      .put "/session/1?title=Foo"
      .set 'origin', 'http://localhost:33284'
      .send zippedFile
      .expect 202, done

describe 'GET /session/:id', ->
  app  = undefined

  beforeEach ->
    app    = (require 'express')()
    app.set 'port', 33445

  it 'should return 200 when existing session is requested', (done) ->
    (require '../sessions')(
      app,
      list: (options, callback) -> callback null, Contents: [Key: "ab/threads/1.dbg"])

    request(app)
      .get "/session/ab"
      .end (err, res) ->
        res.statusCode.should.equal 200
        json = res.body

        json.length.should.equal 1
        json[0].number.should.equal "1"
        json[0].link.should.endWith "session/ab/threads/1"
        done()

  it 'should return 404 when non-existing session is requested', (done) ->
    (require '../sessions')(
      app,
      list: (options, callback) -> callback null, Contents: [])

    request(app)
      .get "/session/ab"
      .expect 404, done

describe 'GET /session/:id/threads/:thread', ->
  app  = undefined

  beforeEach ->
    app    = (require 'express')()
    app.set 'port', 33445

  it 'should return 404 when non-existing thread is requested', (done) ->
    Readable = require('stream').Readable
    s3response = new Readable
    s3response.statusCode = 404
    s3response.push null
    s3mock =
      on: (type, handler)->

        if type == 'response' then handler(s3response)
        s3mock
      end: ->

    (require '../sessions')(
      app,
      get: -> s3mock)

    request(app)
      .get "/session/ab/threads/1"
      .end (err, res) ->
        res.statusCode.should.equal 404
        done()

  it 'should return 200 when existing thread is requested', (done) ->
    Readable = require('stream').Readable
    s3response = new Readable
    s3response.statusCode = 200
    s3response.push "[]"; s3response.push null
    s3mock =
      on: (type, handler)->

        if type == 'response' then handler(s3response)
        s3mock
      end: ->

    (require '../sessions')(
      app,
      get: -> s3mock)

    request(app)
      .get "/session/ab/threads/1"
      .end (err, res) ->
        res.body.should.deep.equal []
        res.statusCode.should.equal 200
        done()

describe 'GET /session/:id/sources/:class', ->
  app  = undefined

  beforeEach ->
    app    = (require 'express')()
    app.set 'port', 33445

  it 'should return 404 when non-existing source is requested', (done) ->
    Readable = require('stream').Readable
    s3response = new Readable
    s3response.statusCode = 404
    s3response.push null
    s3mock =
      on: (type, handler)->

        if type == 'response' then handler(s3response)
        s3mock
      end: ->

    (require '../sessions')(
      app,
      get: -> s3mock)

    request(app)
      .get "/session/ab/sources/com.Foo"
      .end (err, res) ->
        res.statusCode.should.equal 404
        done()

