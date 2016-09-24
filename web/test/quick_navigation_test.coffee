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
chai.use(require 'chai-spies')

QuickNavigation = require '../src/coffee/quick_navigation'
FlowAPI = require '../src/coffee/api/flow_api'
expect = chai.expect
should = chai.should()

describe "Quick Navigation", ->
    it 'should work fine if no tags in the flow', ->
      root = $("<div><div class='list' /></div>")
      quickNavigation = new QuickNavigation root, undefined, {flowAPI: -> new FlowAPI([])}
      quickNavigation.assignFlow()

      root.find(".list > *").should.have.length 0

    it 'should create list item based on tag if a single tag', ->
      root = $("<div><div class='list' /></div>")
      quickNavigation = new QuickNavigation root, {}, {flowAPI: -> new FlowAPI([{tg: "junit"}])}
      quickNavigation.assignFlow()

      root.find(".list > *").should.have.length 1

    it 'should trigger move to a step if list item is clicked', ->
      root = $("<div><div class='list' /></div>")
      stepContext = {moveTo: (step) -> }
      quickNavigation = new QuickNavigation root, stepContext, {flowAPI: -> new FlowAPI([{tg: "junit", i: 1}])}
      quickNavigation.assignFlow()

      chai.spy.on(stepContext, "moveTo")

      root.find(".list li").click()
      stepContext.moveTo.should.have.been.called.with(1)

