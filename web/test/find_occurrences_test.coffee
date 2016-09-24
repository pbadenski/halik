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
{ByClassAndMethodRegexp, ByVariableNameAndValue} = require '../src/coffee/domain/filters'
FindOccurrences = (require '../src/coffee/find_occurrences')(document.body)

describe "Find occurrences", ->
  stubFilters = () ->
    new Filters undefined, undefined, {add: _.noop}, {add: _.noop, connectToFilters: _.noop}

  it 'gains focus when / is pressed', () ->
    filters = stubFilters()

    root = $("<a href='#' />")
    $(document.body).append(root)
    findMethodCalls = new FindOccurrences root, filters

    Mousetrap.trigger('/')

    root.should.have.focus()

  it 'loses focus when ESC is pressed', () ->
    filters = stubFilters()

    root = $("<div />")
    findMethodCalls = new FindOccurrences root, filters

    root.focus()
    root.trigger type: 'keyup', which: 27, keyCode: 27

    root.should.not.have.focus()

  it 'registers method call filter', (done) ->
    filters = stubFilters()

    root = $("<div />")
    findMethodCalls = new FindOccurrences root, filters

    root.val "test"
    root.trigger type: 'keyup', which: 13, keyCode: 13

    setTimeout (() ->
      filters.find().should
        .contain.an.instanceof(ByClassAndMethodRegexp)
      filters.find()
        .should.have.deep.property("criterion.predicate").that.deep.equals regex: "test"
      done()
    ), 10

  it 'registers primitive query', (done) ->
    filters = stubFilters()

    root = $("<div />")
    findMethodCalls = new FindOccurrences root, filters

    root.val "var:1"
    root.trigger type: 'keyup', which: 13, keyCode: 13

    setTimeout (() ->
      filters.find().should
        .contain.an.instanceof(ByVariableNameAndValue)
      filters.find()
        .should.have.deep.property("criterion.predicate").that.deep.equals name: "var", value: "1"
      done()
    ), 10

  it 'does not register invalid regexp and tells the user there was an error', () ->
    filters = stubFilters()

    stubBody = $("<div />")
    root = $("<div />")

    FindOccurrences = (require '../src/coffee/find_occurrences')(stubBody[0])
    findMethodCalls = new FindOccurrences root, filters

    root.val("?")
    root.trigger type: 'keyup', which: 13, keyCode: 13

    expect(filters.find()).to.be.undefined
    stubBody.find(".alertify-logs").text().should.match /SyntaxError: Invalid regular expression.*/
    stubBody.find(".alertify-logs").empty()
