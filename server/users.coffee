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
_          = require 'lodash'
cors       = require 'cors'

gen     = require './gen'

module.exports = (app, Session)  ->
  app.get '/user', cors(origin: "http://localhost:33284", credentials: true), (req, res, next) ->
    user = if req.isAuthenticated() then {login: req.user.username} else {}

    res.status(200).type('json').json(user)

  app.get '/user/sessions', cors(origin: "http://localhost:33284", credentials: true), (req, res, next) ->
    unless req.isAuthenticated()
      res.status(401).end()
      return

    gen ->
      try
        sessions = yield Session.forUser(req.user.id)
        res.status(200).type('json').json(sessions)
      catch err
        console.error err
        res.status(500).end()

  app.get '/user/:id/sessions', cors(origin: "http://localhost:33284", credentials: true), (req, res, next) ->
    gen ->
      try
        sessions = yield Session.forUser(req.params.id)
        res.status(200).type('json').json(sessions)
      catch err
        console.error err
        res.status(500).end()

