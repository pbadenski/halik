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

Filters = require '../src/coffee/domain/filters'
{ByVariableName, ByReference} = require '../src/coffee/domain/filters'
Watches = require '../src/coffee/watches'

expect = chai.expect
should = chai.should()

describe "Watches", ->
  stubFlowAPI = (value) ->
    valueSearch: (ignoreStep) ->
      value: -> value

    findThisForCurrentStep: -> undefined

  describe 'string value', ->
    root = $("<div />")
    watches = new Watches root, flowAPI: -> stubFlowAPI("string")

    watch = Filters.createFilter(new ByVariableName "foo")
    watches.connectToFilters {filter: () -> _([watch])}
    watches.add watch, {currentStep: _.noop }

    it 'is displayed properly with treeview', ->
      root.find(".watch--contents").should.exist
      root.find(".watch--contents").should.have.text "string"

  describe 'undefined value', ->
    root = $("<div />")
    watches = new Watches root, flowAPI: -> stubFlowAPI(undefined)

    watch = Filters.createFilter(new ByVariableName "foo")
    watches.connectToFilters {filter: () -> _([watch])}
    watches.add watch, {currentStep: _.noop }

    it 'is displayed properly with treeview', ->
      root.find(".watch--contents").should.exist
      root.find(".watch--contents").should.have.text "undefined"

  describe 'primitive value', ->
    root = $("<div />")
    watches = new Watches root, flowAPI: -> stubFlowAPI(0)

    watch = Filters.createFilter(new ByVariableName "foo")
    watches.connectToFilters {filter: () -> _([watch])}
    watches.add watch, {currentStep: _.noop }

    it 'is displayed properly with treeview', (done) ->
      watches.onStepChange().then ->
        root.find(".num").should.exist
        root.find(".num").should.have.text "0"
        done()

    it 'is displayed properly with table', (done) ->
      root.find(".watch .controls .table").click()
      watches.onStepChange().then ->
        root.find(".jh-type-number").should.exist
        root.find(".jh-type-number").should.have.text "0"
        done()

  describe 'object value', ->
    it 'does not display this', (done) ->
      root = $("<div />")
      watches = new Watches root, flowAPI: -> stubFlowAPI(this: 0)

      watch = Filters.createFilter(new ByVariableName "foo")
      watches.connectToFilters {filter: () -> _([watch])}
      watches.add watch, {currentStep: _.noop }
      watches.onStepChange().then ->
        root.find(".prop:contains('this')").should.not.exist
        done()

    it 'remembers tree toggle information with treeview', (done) ->
      if window._phantom
        done()
        return
      root = $("<div />")
      watches = new Watches root, flowAPI: -> stubFlowAPI(a: [{a: 0}, {a: 1}])

      watch = Filters.createFilter(new ByVariableName "foo")
      watches.connectToFilters {filter: () -> _([watch])}
      watches.add(watch, {currentStep: _.noop }).then ->
        root.find(".collapser ~ .prop:contains('a') ~ .obj.level1").siblings(".collapser")[0].click()
        root.find(".collapser ~ .prop:contains('0') ~ .obj.level2").siblings(".collapser")[0].click()

        root.find(".collapser ~ .prop:contains('a') ~ .obj.level1").should.not.have.css 'display', 'none'
        root.find(".collapser ~ .prop:contains('0') ~ .obj.level2").should.not.have.css 'display', 'none'
        root.find(".collapser ~ .prop:contains('1') ~ .obj.level2").should.have.css 'display', 'none'
        watches.onStepChange(0, 1)
      .then ->
        root.find(".collapser ~ .prop:contains('a') ~ .obj.level1").should.not.have.css 'display', 'none'
        root.find(".collapser ~ .prop:contains('0') ~ .obj.level2").should.not.have.css 'display', 'none'
        root.find(".collapser ~ .prop:contains('1') ~ .obj.level2").should.have.css 'display', 'none'
        done()
