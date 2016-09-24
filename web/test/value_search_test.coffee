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
chai.config.truncateThreshold = 0

ValueSearch = require '../src/coffee/api/value_search'
expect = chai.expect
should = chai.should()

describe "Value search", ->
  describe 'valueAtStep', ->
    it 'finds object if one exists', ->
      flow = [
        {sn: [{n: "obj", id: 1}]}
      ]
      valueSearch = new ValueSearch flow, 0

      object = valueSearch._valueAtStep {n: "obj"}
      object.n.should.equal 'obj'
      object.id.should.equal 1

    it 'finds primitive value if one exists', ->
      flow = [
        {sn: [{n: "variable", v: 1}]}
      ]
      valueSearch = new ValueSearch flow, 0

      object = valueSearch._valueAtStep {n: "variable"}
      object.should.deep.equal
        n: "variable"
        v: 1

    it 'finds latest integer value', ->
      flow = [
        {sn: [{n: "variable", v: 1}]},
        {sn: [{n: "variable", v: 2}]}
      ]
      valueSearch = new ValueSearch flow, 0

      object = valueSearch._valueAtStep {n: "variable"}
      object.n.should.equal 'variable'
      object.v.should.equal 1

      valueSearch = new ValueSearch flow, 1
      object = valueSearch._valueAtStep {n: "variable"}
      object.should.deep.equal
        n: "variable"
        v: 2

    it 'finds latest boolean value', ->
      flow = [
        {sn: [{n: "variable", v: true}]},
        {sn: [{n: "variable", v: false}]}
      ]
      valueSearch = new ValueSearch flow, 0

      object = valueSearch._valueAtStep {n: "variable"}
      object.n.should.equal 'variable'
      object.v.should.equal true

      valueSearch = new ValueSearch flow, 1

      object = valueSearch._valueAtStep {n: "variable"}
      object.should.deep.equal
        n: "variable"
        v: false

    it 'combines array modifications by using both name and reference', ->
      flow = [
        {sn: [{n: "arr", id: 0, v: []}]},
        {sn: [{id: 0, v: [1]}]}
      ]
      valueSearch = new ValueSearch flow, 1

      array = valueSearch._valueAtStep {n: "arr"}
      array.should.deep.equal
        n: "arr"
        id: 0
        v: [1]

    it 'handles search for array modifications before it was declared', ->
      flow = [
        {},
        {sn: [{n: "arr", id: 0, v: []}]}
      ]
      valueSearch = new ValueSearch flow, 0

      array = valueSearch._valueAtStep {n: "arr"}
      should.not.exist array


  describe '_render', ->
    it 'integer value', ->
      valueSearch = new ValueSearch [], 0

      value = valueSearch._render {n: "variable", v: 1}, {}
      value.should.equal 1

    it 'boolean value', ->
      valueSearch = new ValueSearch [], 0

      value = valueSearch._render {n: "variable", v: false}, {}
      value.should.equal false

    describe 'array value', ->
      it 'simple case', ->
        valueSearch = new ValueSearch [], 0

        value = valueSearch._render {n: "array", id: 0, t: 'A', v: [0, 1]}, {}
        value.should.deep.equal _.extend([0, 1], this: 0, '#ref': 0)

      it 'array of arrays', ->
        valueSearch = new ValueSearch [sn: [{id: 1, t: 'A', v: [false, true]}]], 0

        value = valueSearch._render {n: "array", id: 0, t: 'A', v: [{id: 1, t: 'A'}]}, {}
        value.should.deep.equal _.extend([_.extend([false, true], this: 1, '#ref': 1)], this: 0, '#ref': 0)

    it 'object value', ->
      valueSearch = new ValueSearch [], 0

      object = valueSearch._render {n: "object", id: 1, t: "O"}, {}

      object.should.have.property("this", 1)
      object.should.have.property("#ref", 1)

    it 'map value', ->
      valueSearch = new ValueSearch [], 0

      map = valueSearch._render {n: "map", id: 1, t: "M", v: [{k: {id: 2, t:"O"}, v:{v:2, t: "P"}}] }, {}
      map.should.deep.equal _.assign [{k: {this: 2, '#ref': 2, "#cls": undefined}, v: 2}], this: 1, '#ref': 1

    describe 'object value', ->
      it 'by using latest field values', ->
        flow = [
          {sn: [{n: "intValue", v: 1, o: 1, t: "P"}]},
          {sn: [{n: "intValue", v: 2, o: 1, t: "P"}]}
        ]
        valueSearch = new ValueSearch flow, 0
        object = valueSearch._render {n: "object", id: 1, t: "O"}, {}
        object.should.have.property("intValue", 1)

        valueSearch = new ValueSearch flow, 1
        object = valueSearch._render {n: "object", id: 1, t: "O"}, {}
        object.should.have.property("intValue", 2)

      it 'with fields of different primitive types', ->
        flow = [
          {sn: [
            {n: "booleanValue", v: false, o: 1, t: "P"},
            {n: "intValue", v: 1, o: 1, t: "P"},
            {n: "charValue", v: 'a', o: 1, t: "P"},
            {n: "stringValue", v: "a", o: 1, t: "P"}]}
        ]
        valueSearch = new ValueSearch flow, 1

        object = valueSearch._render {n: "object", id: 1, t: "O"}, {}
        object.should.have.property("intValue", 1)
        object.should.have.property("charValue", 'a')
        object.should.have.property("booleanValue", false)
        object.should.have.property("stringValue", "a")

      it 'with fields of reference type', ->
        flow = [
          {sn: [
            {n: "intField", v: 2, o: 2, t: "P"}
            {n: "refField", id: 2, o: 1, t: "O"}
          ]}
        ]
        valueSearch = new ValueSearch flow, 1

        object = valueSearch._render {n: "object", id: 1, t: "O"}, {}
        object.should.have.property("refField").which.has.property("intField").equal(2)
        object.should.have.property("refField").which.has.property("this").equal(2)

      for iterableType in [ "C", "A" ]
        it "with fields of #{iterableType} type", ->
          flow = [
            {sn: [
              {n: "object", id: 1, t: "O"},
              {n: "iterableField", id: 2, o: 1, t: iterableType}
            ]}
            {sn: [{id: 2, v: ["foo"], o: 1, t: iterableType}]}
          ]

          valueSearch = new ValueSearch flow, 1

          object = valueSearch._render {n: "object", id: 1, t: "O"}, {}
          object.should.deep.equal {iterableField: _.assign(["foo"], this: 2, '#ref': 2), this: 1, '#ref': 1, '#cls': undefined}

        it "with fields of #{iterableType} type containing references", ->
          flow = [
            {sn: [
              {n: "object", id: 1, t: "O"},
              {n: "refObject", id: 3, t: "O"}
            ]},
            {sn: [
              {n: "refObjectField", v: 1, o: 3, t: "P"}
              {n: "iterableField", id: 2, o: 1, t: iterableType}
            ]}
            {sn: [
              {id: 2, v: [{id: 3, t: "O"}], o: 1, t: iterableType}
            ]}
          ]

          valueSearch = new ValueSearch flow, 2

          object = valueSearch._render {n: "object", id: 1, t: "O"}, {}
          object.should.deep.equal {iterableField: _.assign([{refObjectField: 1, this: 3, '#ref': 3, '#cls': undefined}], this: 2, '#ref': 2), this: 1, '#ref': 1, '#cls': undefined}

        it "with fields of #{iterableType} right after #{iterableType} is declared", ->
          flow = [
            {sn: [
              {n: "object", id: 1, t: "O"}
              {n: "iterableField", id: 2, o: 1, v: [], t: iterableType}
            ]}
          ]

          valueSearch = new ValueSearch flow, 2

          object = valueSearch._render {n: "object", id: 1, t: "O"}, {}
          object.should.deep.equal {iterableField: _.assign([], this: 2, '#ref': 2), this: 1, '#ref': 1, '#cls': undefined}

        it "handle search for #{iterableType} field modifications by using both name and reference", ->
          flow = [
            {sn: [
              {n: "object", id: 1, t: "O"},
              {n: "iterableField", id: 2, o: 1, v: [], t: iterableType}
            ]},
            {sn: [id:2, v: [0, 1], t: iterableType]}
          ]

          valueSearch = new ValueSearch flow, 2

          object = valueSearch._render {n: "object", id: 1, t: "O"}, {}
          object.should.deep.equal {iterableField: _.assign([0, 1], this: 2, '#ref': 2), this: 1, '#ref': 1, '#cls': undefined}

    describe 'recursive structures', ->

      it 'should not blow up when object -> collection -> that object', ->
        flow = [
          {sn: [
            {n: "object", id: 1, t: "O"},
            {n: "collectionField", id: 2, o: 1, v: [], t: "C"}
          ]},
          {sn: [id:2, v: [{id: 1, t: "O"}], t: "C"]}
        ]

        valueSearch = new ValueSearch flow, 2

        object = valueSearch._render {n: "object", id: 1, t: "O"}, {}

      it 'should not blow up when object -> another object -> that object', ->
        flow = [
          {sn: [
            {n: "object", id: 1, t: "O"},
            {n: "anotherObjectField", id: 2, o: 1, v: [], t: "O"}
            {n: "thatObjectField", id: 1, o: 2, v: [], t: "O"}
          ]}
        ]

        valueSearch = new ValueSearch flow, 2

        object = valueSearch._render {n: "object", id: 1, t: "O"}, {}

      it 'should not report recursive structure when visited a node, but on the same level', ->
        flow = [
          {sn: [
            {n: "object", id: 1, t: "O"},
            {n: "object", id: 3, t: "O", o: 1},
            {n: "collectionField", id: 2, o: 1, v: [], t: "C"}
          ]},
          {sn: [id:2, v: [{id: 3, t: "O"}], t: "C"]}
        ]

        valueSearch = new ValueSearch flow, 2

        object = valueSearch._render {n: "object", id: 1, t: "O"}, {}

        object.should
          .have.property('collectionField')
          .which.has.property("0")
          .which.has.not.property('#CYCLE')
