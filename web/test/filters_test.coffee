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
expect = chai.expect

Filters = require '../src/coffee/domain/filters'
{ByVariableName, ByReference, ByReferenceOrVariableName, ByVariableNameAndValue, ByExpression} = require '../src/coffee/domain/filters'
FlowAPI = require '../src/coffee/api/flow_api'

describe "Filters", ->
  createFilters = (flow) ->
    new Filters undefined, {flowAPI: -> new FlowAPI(flow)}, {add: _.noop}, {add: _.noop, connectToFilters: _.noop}

  describe "'by variable name -> by reference and variable name' heuristic", () ->
    it 'should register simple variable name filter in basic scenario', () ->
      flow = [
        {sn: [n: "primitiveVar", t: "P"]}
      ]
      filters = createFilters(flow)

      filters.register(new ByVariableName("primitiveVar")).then () ->
        filters.allOfType(ByVariableName).value().should.have.length 1

    it 'should register simple variable name filter if there is a primitive and reference with selected name', () ->
      flow = [
        {sn: [n: "var", t: "P"]}
        {sn: [n: "var", t: "O", id: 1]}
      ]
      filters = createFilters(flow)

      filters.register(new ByVariableName("var")).then () ->
        filters.allOfType(ByVariableName).value().should.have.length 1

    it 'should register by reference filter if there is only a single reference with selected name', () ->
      flow = [
        {sn: [n: "objectVar", t: "O", id: 1]}
      ]
      filters = createFilters(flow)

      filters.register(new ByVariableName("objectVar")).then () ->
        filters.find().should.contain.an.instanceof(ByReferenceOrVariableName)

    it 'should register simple by name filter if there is two references with selected name', () ->
      flow = [
        {sn: [n: "objectVar", t: "O", id: 1]}
        {sn: [n: "objectVar", t: "O", id: 2]}
      ]
      filters = createFilters(flow)

      filters.register(new ByVariableName("objectVar")).then () ->
        filters.find().should.contain.an.instanceof(ByVariableName)

    it 'should register by reference or variable name filter if there is a reference with selected name and some undefined values', () ->
      flow = [
        {sn: [n: "objectVar", t: "O", id: 1]}
        {sn: [n: "objectVar", t: "O", v: null]}
      ]
      filters = createFilters(flow)

      filters.register(new ByVariableName("objectVar")).then () ->
        filters.find().should.contain.an.instanceof(ByReferenceOrVariableName)

    it 'should register by reference or variable name filter even if there is no ocurrences (in case occurrences are in different thread)', () ->
      filters = createFilters([])

      filters.register(new ByVariableName("objectVar")).then () ->
        filters.find().should.contain.an.instanceof(ByVariableName)

    it 'should register by name and value filter if asked for name and value filter', () ->
      filters = createFilters()

      filters.register(new ByVariableNameAndValue("intVar", 2)).then () ->
        filters.find().should.contain.an.instanceof(ByVariableNameAndValue)

  describe 'by name and value filter', ->
    it 'should find occurrences by name and value filter', () ->
      flow = [
        {sn: [n: "intVar", t: "P", v: 1]}
        {sn: [n: "intVar", t: "P", v: 2]}
      ]

      new FlowAPI(flow).occurrences(new ByVariableNameAndValue("intVar", 2), flow).then (objects) ->
        objects.should.have.length 1

        object = objects[0]
        object.should.have.deep.property("occurrence.n").which.is.equal "intVar"
        object.should.have.deep.property("occurrence.v").which.is.equal 2

  describe 'by name filter', ->
    it 'should find occurrences by name filter', () ->
      flow = [
        {sn: [n: "intVar", t: "P", v: 1]}
        {sn: [n: "intVar", t: "P", v: 2]}
      ]

      new FlowAPI(flow).occurrences(new ByVariableName("intVar"), flow).then (objects) ->
        objects.should.have.length 2

        object = objects[0]
        object.should.have.deep.property("occurrence.n").which.is.equal "intVar"
        object.should.have.deep.property("occurrence.v").which.is.equal 1

        object = objects[1]
        object.should.have.deep.property("occurrence.n").which.is.equal "intVar"
        object.should.have.deep.property("occurrence.v").which.is.equal 2

  describe 'by expression filter', ->
    return if window._phantom # phantom doesn't support ES6 Proxy
    it 'should find occurrences in simple case', () ->
      flow = [
        {sn: [n: "intVar", t: "P", v: 1]}
        {sn: [n: "intVar", t: "P", v: 2]}
        {sn: [n: "intVar", t: "P", v: 3]}
      ]

      new FlowAPI(flow).occurrences(new ByExpression("intVar <= 2"), flow).then (objects) ->
        objects.should.have.length 2

        object = objects[0]
        object.should.have.deep.property("occurrence").which.is.equal true

        object = objects[1]
        object.should.have.deep.property("occurrence").which.is.equal true

    it 'should find occurrences when some might be undefined', () ->
      flow = [
        {sn: [n: "intVar", t: "P", v: 3]}
        {sn: []}
        {sn: [n: "intVar", t: "P", v: 1]}
      ]

      new FlowAPI(flow).occurrences(new ByExpression("intVar <= 2"), flow).then (objects) ->
        objects.should.have.length 1

        object = objects[0]
        object.should.have.deep.property("occurrence").which.is.equal true

    it 'should find occurrences with #toS if applicablew', () ->
      flow = [
        {sn: [n: "enumField", t: "O", "#toS": "ACTIVE"]}
        {sn: []}
        {sn: [n: "enumField", t: "O", "#toS": "PASSIVE"]}
      ]

      new FlowAPI(flow).occurrences(new ByExpression("enumField == 'ACTIVE'"), flow).then (objects) ->
        objects.should.have.length 1

        object = objects[0]
        object.should.have.deep.property("occurrence").which.is.equal true

