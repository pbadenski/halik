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
settings = require './settings'

routes = (url) ->
  session:
    upload: (sessionId, title, content) ->
      $.ajax
        url: "#{url}/session/#{sessionId}?title=#{title}"
        type: 'PUT'
        xhrFields: withCredentials: true
        data: content
    get: (sessionId) ->
      $.get "#{url}/session/#{sessionId}"
    getMetadata: (sessionId) ->
      $.get "#{url}/session/#{sessionId}/metadata"
    getThreadsInformation: (sessionId) ->
      $.get "#{url}/session/#{sessionId}/threads"
    getSource: (sessionId, className) ->
      $.get "#{url}/session/#{sessionId}/sources/#{className}"
  user:
    get: ->
      $.ajax
        type: 'GET'
        url: "#{url}/user"
        dataType: 'json'
        xhrFields: withCredentials: true

module.exports =
  default: routes ""
  local: routes "http://localhost:33284"
  remote: routes settings.WEBSITE_URL

