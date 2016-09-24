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

module.exports = class
  constructor: () ->
    @root = $('.sign-in')

  load: () ->
    Promise.resolve(
      $.ajax
        type: 'GET'
        url: "#{settings.WEBSITE_URL}/user"
        dataType: 'json'
        xhrFields: withCredentials: true)
      .then (user) =>
        if user.login?
          @root.html $(
            """<ul class="dropdown menu menu-item" data-dropdown-menu>
              <li>
                <img width='32' height='32' src='https://github.com/#{user.login}.png?size=32'/>
                <ul class="menu">
                  <li style='border-bottom: 1px white solid'><a href="/sessions.html">My sessions</a></li>
                  <li><a href="#{settings.WEBSITE_URL}/logout?redirect_after_action=#{location.href}">Sign out</a></li>
                </ul>
              </li>
            </ul>"""
          ).foundation()
        else
          @root.html $("<a href='#{settings.WEBSITE_URL}/login?redirect_after_action=#{location.href}' class='menu-item'>Sign in</a>")

