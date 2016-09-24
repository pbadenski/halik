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

StackTrace = require '../src/coffee/stack_trace'
FlowAPI = require '../src/coffee/api/flow_api'
expect = chai.expect
should = chai.should()

describe "Stack trace", ->
  it 'works fine for first step when first element in flow is at depth = 1', ->
    flow = [{sd: 1}]
    frames = new FlowAPI(flow).frames(0).value()
    
    frames.should.have.length 1
    frames[0].should.not.be.undefined

  it 'fills gaps with single undefined when first element in flow is at depth > 1', ->
    flow = [{sd: 3}]
    frames = new FlowAPI(flow).frames(0).value()
    
    frames.should.have.length 2
    should.not.exist frames[0]
    should.exist frames[1]

  it 'fills multiple gaps with single undefined', ->
    flow = [
      {sd: 3}
      {sd: 5}
    ]
    frames = new FlowAPI(flow).frames(1).value()
    
    frames.should.have.length 4
    should.not.exist frames[0]
    should.exist frames[1]
    should.not.exist frames[2]
    should.exist frames[3]
