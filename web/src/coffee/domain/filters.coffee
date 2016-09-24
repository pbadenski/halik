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
alertify = require 'alertify.js'

{escapeHtml} = require '../utils'

module.exports = class Filters
  @createFilter: (base) ->
    randomId = _.random(0, 1, true).toString().replace("\.", "-")
    _.assign base, deletable(), id: randomId

  constructor: (@stepContext, @threadContext, @timelines, @watches) ->
    @watches.connectToFilters this
    @items = []

  deletable = () ->
    onDeletedFuncs: []
    onDeleted: (f) -> @onDeletedFuncs.push(f)
    delete: () -> _.each @onDeletedFuncs, (f) -> f()

  find: (props) -> _.find @items, props

  filter: (props) -> _(@items).filter props

  allOfType: (type) -> _(@items).filter (item) -> item instanceof type

  registerAll: (savedFilters) ->
    _.each savedFilters, ({name, criterionKey}) =>
      name  = criterionKey.name
      id    = criterionKey.id
      value = criterionKey.value
      cls   = criterionKey.class
      line  = criterionKey.line
      regex = criterionKey.regex
      if id? and name?
        filter = new module.exports.ByReferenceOrVariableName(id, name)
      else if id?
        filter = new module.exports.ByReference(id, name)
      else if name? and value?
        filter = new module.exports.ByVariableNameAndValue(name, value)
      else if name?
        filter = new module.exports.ByVariableName(name)
      else if line? and cls?
        filter = new module.exports.ByLineInClass({class: cls, line: line}, name: name)
      else if regex?
        filter = new module.exports.ByClassAndMethodRegexp(regex)
      @register filter

  applyHeuristics: (filter) ->
    if filter instanceof module.exports.ByVariableName
      @threadContext.flowAPI().occurrences(filter).then (occurrences) ->
        allEntities = _.chain(occurrences)
          .map("occurrence")
          .map (each) ->
            if each.t== "P"
              name: each.n
            else if each.id?
              id: each.id
            else
              undefined
          .reject(_.isNil)
          .uniqWith _.isEqual

        if allEntities.isEmpty().value()
          return filter

        # performance optimization
        # we only need to process maximum 2 elements to see if there's more than one references
        refersAlwaysToTheSameEntity = allEntities.take(2).value().length == 1
        entity = allEntities.first().value()
        isReference = entity.id?
        if refersAlwaysToTheSameEntity and isReference
          filter = new module.exports.ByReferenceOrVariableName entity.id, filter.name.short
        return filter
    else
      return Promise.resolve(filter)

  register: (filter) ->
    @applyHeuristics(filter).then (filter) =>
      if _.some _.map(@items, "criterion.key"), _.curry(_.isEqual)(filter.criterion.key)
        alertify.error "Watch `#{filter.name.long}` is already registered"
        return undefined

      filter = @constructor.createFilter filter
      @items.push filter
      filter.onDeleted () =>
        _.remove @items, criterion: key: filter.criterion.key

      if filter.hasWatch
        @watches.add filter, @stepContext
      if filter.hasTimeline
        @timelines.add filter

      return filter

module.exports.ByVariableName = class
  constructor: (name) ->
    console.error "name cannot be undefined" unless name?
    @name =
      short: name
      long:  name
    @criterion =
      predicate: n: name
      key: name: name
    self = this

  hasTimeline: true

  hasWatch: true

  linksToReference: true

  nameIsEditable: false

module.exports.ByReference = class
  constructor: (id, name) ->
    console.error "id cannot be undefined" unless id?
    @name =
      short: name
      long:  (name or "") + "##{id}"
    @criterion =
      predicate: id: id
      key: id: id
    self = this

  hasTimeline: true

  hasWatch: true

  linksToReference: false

  nameIsEditable: false

module.exports.ByReferenceOrVariableName = class
  constructor: (id, name) ->
    console.error "id cannot be undefined" unless id?
    console.error "name cannot be undefined" unless name?
    @name =
      short: name
      long:  name + "##{id}"
    @criterion =
      predicate: (target) -> target.id == id || target.n == name
      key: { id: id, name: name}
    self = this

  hasTimeline: true

  hasWatch: true

  linksToReference: false

  nameIsEditable: false

module.exports.ByLineInClass = class
  constructor: (props, name) ->
    name = name or "[Line: #{props.line}] #{_.last(props.class.split("/"))}"
    @name =
      short: name
      long:  name
    @criterion =
      predicate: (target) -> target.c == props.class and target.l == props.line
      key: _.pick props, "class", "line"
    self = this

  hasTimeline: true

  hasWatch: false

  nameIsEditable: true

module.exports.ByClassAndMethodRegexp = class
  constructor: (regex) ->
    try
      new RegExp(regex)
    catch error
      console.error "Error in regexp: `#{regex}`"
    name = "/#{escapeHtml(regex)}/"
    @name =
      short: name
      long:  name
    @criterion =
      predicate: regex: regex
      key: regex: regex
    @matches = _.memoize (flow) ->
      _(flow)
        .filter (each) -> (each.c + "." + each.m).match(new RegExp(regex))
        .groupBy (each) -> [each.c, each.m]
        .map _.first
        .value()
    self = this

  hasTimeline: true

  hasWatch: false

  nameIsEditable: false

module.exports.ByVariableNameAndValue = class
  constructor: (name, value) ->
    console.error "name cannot be undefined" unless name?
    console.error "value cannot be undefined" unless value?
    @name =
      short: name + ":" + value
      long:  name + ":" + value
    @criterion =
      predicate:
        name: name
        value: value
      key:
        name: name
        value: value
    self = this

  hasTimeline: true

  hasWatch: false

  nameIsEditable: false

module.exports.ByExpression = class
  constructor: (expression) ->
    console.error "expression cannot be undefined" unless expression?
    @name =
      short: expression
      long:  expression
    @criterion =
      predicate: expression: expression
      key: expression: expression
    self = this

  hasTimeline: true

  hasWatch: false

  nameIsEditable: false

