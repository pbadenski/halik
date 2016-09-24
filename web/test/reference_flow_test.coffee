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

ReferenceFlow = require '../src/coffee/domain/reference_flow'
expect = chai.expect
should = chai.should()

describe "Reference Flow", ->
    it 'combines from entries with higher timestamp', ->
      flowFirst = [{t: 10}, {t: 10}]
      flowSecond = [{t: 10}]

      referenceFlow = new ReferenceFlow [{flow: flowFirst}, {flow: flowSecond}]
      referenceFlow.toRefLookup(flowFirst).length.should.equal 2

      referenceFlow = new ReferenceFlow [flow: flowSecond, flow: flowFirst]
      referenceFlow.toRefLookup(flowFirst).length.should.equal 2

      referenceFlow = new ReferenceFlow [flow: flowSecond, flow: flowFirst]
      referenceFlow.toRefLookup(flowSecond).length.should.equal 1

    it 'works with numerical sort (rather than lexograpgical) - this is regression test', ->
      flowFirst = [{t: 900}, {t: 1000}]
      flowSecond = [{t: 50}]

      referenceFlow = new ReferenceFlow [{flow: flowFirst}, {flow: flowSecond}]
      referenceFlow.toRefLookup(flowFirst).length.should.equal 2

      referenceFlow = new ReferenceFlow [flow: flowFirst, flow: flowSecond]
      referenceFlow.toRefLookup(flowSecond).length.should.equal 1

    describe 'works when flows have identical timestamps', ->
      flowFirst = [{t: 11}]
      flowSecond = [{t: 11}]
      referenceFlow = new ReferenceFlow [{flow: flowFirst}, {flow: flowSecond}]

      it 'toRefLookup', ->
        referenceFlow.toRefLookup(flowFirst).should.deep.equal [0]
        referenceFlow.toRefLookup(flowSecond).should.deep.equal [0]

      it 'fromRefLookup', ->
        referenceFlow.fromRefLookup(flowFirst).should.deep.equal [0]
        referenceFlow.fromRefLookup(flowSecond).should.deep.equal [0]

    describe 'works when flows have slightly different timestamps', ->
      flowFirst = [{t: 11}, {t: 12}]
      flowSecond = [{t: 11}, {t: 13}]
      referenceFlow = new ReferenceFlow [{flow: flowFirst}, {flow: flowSecond}]

      it 'toRefLookup', ->
        referenceFlow.toRefLookup(flowFirst).should.deep.equal [0, 1]
        referenceFlow.toRefLookup(flowSecond).should.deep.equal [0, 2]

      it 'fromRefLookup', ->
        referenceFlow.fromRefLookup(flowFirst).should.deep.equal [0, 1]
        referenceFlow.fromRefLookup(flowSecond).should.deep.equal `[0, , 1]`

    describe 'works when flows have mutually exclusive timestamps', ->
      flowFirst = [{t: 11}]
      flowSecond = [{t: 12}]
      referenceFlow = new ReferenceFlow [{flow: flowSecond}, {flow: flowFirst}]

      it 'toRefLookup', ->
        referenceFlow.toRefLookup(flowFirst).should.deep.equal [0]
        referenceFlow.toRefLookup(flowSecond).should.deep.equal [1]

      it 'fromRefLookup', ->
        referenceFlow.fromRefLookup(flowFirst).should.deep.equal [0]
        referenceFlow.fromRefLookup(flowSecond).should.deep.equal `[,0]`

    describe 'timestampAt', ->
      it 'returns correct for existing timestamp', ->
        referenceFlow = new ReferenceFlow [flow: [{t: 10}]]
        
        referenceFlow.timestampAt(0).should.equal 10

      it 'returns undefined for non-existing timestamp', ->
        referenceFlow = new ReferenceFlow [flow: [{t: 10}]]
        
        expect(referenceFlow.timestampAt(1)).to.be.undefined
