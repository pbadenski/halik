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
_            = require 'lodash'
path         = require 'path'
cors         = require 'cors'
toArray      = require 'stream-to-array'
base64       = require 'base64-stream'
sanitizeHtml = require 'sanitize-html'

gen     = require './gen'

allowPluginCors = origin: "http://localhost:33284"

module.exports = (app, s3, Session) ->
  protocol = if process.argv.env == 'production' then 'https' else 'http'

  app.get '/session', cors(allowPluginCors), (req, res) ->
    s3.list { delimiter: '/' }, (error, data) ->
      sessions = (data?['CommonPrefixes'] or []).map (each) ->
        hostname = req.headers.host
        sessionId = each['Prefix']
        id: sessionId
        link: "#{protocol}://" + hostname + '/session/' + sessionId
      res
        .status(200)
        .type('json')
        .json sessions

  app.get '/session/top', cors(allowPluginCors), (req, res) ->
    gen ->
      try
        sessions = yield Session.last(10)
        res.status(200).type('json').json(sessions)
      catch err
        console.error err
        res.status(500).end()

  uploadSessionCors = _.merge {}, allowPluginCors, credentials: true
  app.options '/session/:id', cors(uploadSessionCors)
  app.put '/session/:id', cors(uploadSessionCors), (req, res) ->
    unless req.get('origin')?.startsWith "http://localhost:33284"
      res.status(403).end()
      return

    unless req.isAuthenticated()
      res.status(401).end()
      return

    unless req.query.title
      res.status(400).send("title is required")
      return

    toArray req.pipe(base64.decode()), (error, zipContent) ->
      if zipContent[0].length > 1000000
        # max. approx. 1MB zip
        res
          .status(413)
          .end 'Session ZIP cannot be larger than 1MB'
        return

      Session.upload
        id: req.params.id
        title: sanitizeHtml(req.query.title, allowedTags: [], allowedAttributes: [])
        userId: req.user.id
        zipContent: zipContent
      .catch (error) ->
        console.error error
      res
        .status(202)
        .end()


  app.get '/session/:id', cors(allowPluginCors), (req, res) ->
    prefix = path.join(req.params.id, 'threads') + '/'
    s3.list { prefix: prefix }, (error, data) ->
      threads = data['Contents'].map (each) ->
        each['Key'].substring(prefix.length)
      .filter (filename) ->
        filename.endsWith '.dbg'
      .map (filename) ->
        filename.split('.')[0]
      .map (number) ->
        hostname = req.headers.host
        number: number
        link: "#{protocol}://" + hostname + '/session/' + req.params.id + '/threads/' + number
      if threads.length != 0
        res
          .status(200)
          .type('json')
          .json threads
      else
        res
          .status(404)
          .type('json')
          .json error: 'session not found: ' + req.params.id

  app.get '/session/:id/threads/:thread', cors(allowPluginCors), (req, res) ->
    filename = path.join(req.params.id, 'threads', req.params.thread + '.dbg')
    s3.get(filename).on('error', (err) ->
      console.error err
    ).on('response', (s3response) ->
      if s3response.statusCode == 200
        res.set 'Last-Modified', s3response.headers?['last-modified']
        res.set 'ETag', s3response.headers?.etag
        res.type('json')
        if req.fresh
          res.status(304).end()
        else
          res.writeHead 200
          s3response.pipe res
      else
        s3response.pipe process.stdout
        res.writeHead 404, 'Content-Type': 'text/plain'
        res.end 'file not found: ' + filename
    ).end()

  app.get '/session/:id/sources/:class', cors(allowPluginCors), (req, res) ->
    if req.params.class == undefined
      res.status(400).end 'incorrect syntax'
    filename = path.join(req.params.id, 'sources', req.params.class)
    s3.get(filename).on('error', (err) ->
      console.error err
    ).on('response', (s3response) ->
      if s3response.statusCode == 200
        res.set 'Last-Modified', s3response.headers?['last-modified']
        res.set 'ETag', s3response.headers?.etag
        res.type 'text/x-java-source'
        if req.fresh
          res.status(304).end()
        else
          res.writeHead 200
          s3response.pipe res
      else
        s3response.pipe process.stdout
        res.writeHead 404, 'Content-Type': 'text/plain'
        res.end 'file not found: ' + filename
    ).end()

