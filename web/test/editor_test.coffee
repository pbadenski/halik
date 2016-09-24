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

Editor = (require '../src/coffee/editor')(document)
Filters = require '../src/coffee/domain/filters'
FlowAPI = require '../src/coffee/api/flow_api'

describe "Editor", ->
  stubFilters = () ->
    new Filters undefined, undefined, {add: _.noop}, {add: _.noop, connectToFilters: _.noop}

  it 'should set line breakpoint color when line gutter is clicked', () ->
    if window._phantom
      return

    root = $("<div style='width: 100px; height: 100px'/>")
    $("#mocha-report").append root
    flow = [c: "Foo", l: 1, m: "bar"]
    threadContext =
      all: [flow]
      flowAPI: -> new FlowAPI(flow)
      sessionAPI: ->
        variableNames: -> []
        findInClass: -> flow[0]
    editor = new Editor root, stubFilters(), {}, "someSessionId", threadContext
    editor.assignFlow flow

    $.get = -> jQuery.Deferred().resolve("line 1")
    editor.onStepChange undefined, 0
      .then -> editor.__onGutterClick 0
      .then ->
        root.find(".CodeMirror-gutter-wrapper").should.have.class "line-breakpoint"

        root.remove()

  it 'should remove line breakpoint color when line gutter is clicked twice', () ->
    if window._phantom
      return
    root = $("<div style='width: 100px; height: 100px'/>")
    $("#mocha-report").append root
    flow = [c: "Foo", l: 1, m: "bar"]
    threadContext =
      all: [flow]
      flowAPI: -> new FlowAPI(flow)
      sessionAPI: ->
        variableNames: -> []
        findInClass: -> flow[0]
    editor = new Editor root, stubFilters(), {}, "someSessionId", threadContext
    editor.assignFlow flow

    $.get = -> jQuery.Deferred().resolve("line 1")
    editor.onStepChange undefined, 0
      .then -> editor.__onGutterClick 0
      .then -> editor.__onGutterClick 0
      .then ->
        $(".CodeMirror-gutter-wrapper").should.not.have.class "line-breakpoint"

        root.remove()

