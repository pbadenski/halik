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
require '../src/coffee/watches'

describe "Deep omit", ->
  it 'should return same object for empty object', ->
    _.deepOmit({}).should.deep.equal {}

  it 'should return same object for any object if no filter is defined', ->
    _.deepOmit({a: 1}).should.deep.equal {a: 1}

  it 'should return empty object when the only existing property is required to be omitted', ->
    _.deepOmit({a: 1}, "a").should.deep.equal {}

  it 'should return object without omitted property', ->
    _.deepOmit({a: 1, b: 2}, "a").should.deep.equal {b: 2}

  it 'should return object without omitted property - all the way recursively', ->
    _.deepOmit({a: 1, b: {a: 1, b: 2}}, "a").should.deep.equal {b: { b: 2}}
