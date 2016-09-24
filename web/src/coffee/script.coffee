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
require 'foundation-sites'
dateFormat = require 'dateformat'
StackTrace = require 'stacktrace-js'

WeakMap  = require 'es6-weak-map'
api      = require './api'
Explorer = (require './explorer')(document, api)
SignIn   = require './sign_in'

humanFileSize = (bytes, si) ->
  thresh = if si then 1000 else 1024
  if Math.abs(bytes) < thresh
    return bytes + ' B'
  units = if si then [
    'kB', 'MB', 'GB'
  ] else [
    'KiB', 'MiB', 'GiB'
  ]
  u = -1
  loop
    bytes /= thresh
    ++u
    unless Math.abs(bytes) >= thresh and u < units.length - 1
      break
  bytes.toFixed(1) + ' ' + units[u]

configureAlerts = ->
  alertify = (require 'alertify.js').maxLogItems(5).logPosition("top right")

configureErrorReporting = ->
  callback = (stackframes) ->
    stringifiedStack = stackframes.map((sf) ->
      sf.toString()
    ).join('\n')
    console.error stringifiedStack

  errback = (err) ->
    console.error err.message

  window.onerror = (msg, file, line, col, error) ->
    StackTrace.fromError(error).then(callback).catch(errback)

$ ->
  _.memoize.Cache = WeakMap
  configureAlerts()
  configureErrorReporting()

  if $(".top-bar .sign-in").length
    new SignIn().load()
  if $("body#explorer").length
    new Explorer().load()
  if $("body#users-sessions").length
    Promise.resolve($.get "/user/sessions")
      .then (sessions) ->
        if sessions.length == 0
          $(".sessions-list").append("<div>No sessions yet. Start exploring now using <a href='/installing-plugin.html'>IntelliJ plugin</a>.</div>")
        else
          $(".sessions-list").append(
            $("<table><thead><tr>" +
              "<th>Details</th>" +
              "<th>Date</th>" +
              "<th>Size</th>" +
            "</tr></thead></table>").append(
              "<tbody" +
              sessions.map(({id, title, date, size}) ->
                "<tr>" +
                  "<td><a href='/browse/#{id}'>#{title}</a></td>" +
                  "<td>#{if date then dateFormat(date) else "Unknown"}</td>" +
                  "<td>#{if size then humanFileSize(size) else "Unknown"}</td>" +
                  "</tr>") +
              "</tbody>"
            )
          )
      .catch (error) ->
        if error.status == 401
          $(".sessions-list").append $("<p>You need to <a href='/login?redirect_after_action=/sessions.html'>log in</a> first</p>")
        else
          console.error error
  if $("body .top-sessions").length
    Promise.resolve($.get "/session/top")
      .then (sessions) ->
        $(".top-sessions").append(
          sessions.map ({id, title}) -> $("<h4><a href='/browse/#{id}'>#{title}</a></h4>")
        )
