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
chai = require 'chai'
chai.use(require 'chai-jquery')


describe "Explorer", ->
  it 'should load (first step to refactoring towards explorer end to end tests)', () ->
    return if window._phantom
    this.timeout(5000)

    root = $("<div></div>")
      .append("<div id='slider' />")
      .append("<div class='timelines' />")
      .append("<div class='watches-panel' />")
      .append("<div class='watches' />")
      .append("<div class='stack-trace' />")
      .append("<div class='editor' />")
    doc =
      location:
        href: "http://browse.halik.io/browse/1"
        pathname: "/browse/1"
      body: root[0]
    api = default:
      session:
        get: -> undefined
        getMetadata: -> undefined
        getThreadsInformation: -> undefined

    Explorer = (require '../src/coffee/explorer')(doc, api)
    new Explorer(root).load()

