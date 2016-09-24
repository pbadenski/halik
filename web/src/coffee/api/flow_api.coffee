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
async    = require 'async'
jexl     = require 'jexl'
require 'proxy-polyfill' # set up Proxy for older browers, eg. phantom

ValueSearch = require './value_search'
{
  ByVariableName, ByReference,
  ByReferenceOrVariableName
  ByLineInClass, ByClassAndMethodRegexp,
  ByVariableNameAndValue, ByExpression
} = require '../domain/filters'

module.exports = class FlowAPI
  constructor: (@flow) ->

  appliesAt: (filter) ->
    (flow, step) ->
      if filter instanceof ByVariableName
        Promise.resolve(_.find step.sn, filter.criterion.predicate)
      else if filter instanceof ByReference
        Promise.resolve(_.find step.sn, filter.criterion.predicate)
      else if filter instanceof ByReferenceOrVariableName
        Promise.resolve(_.find(step.sn, filter.criterion.predicate))
      else if filter instanceof ByReferenceOrVariableName
        Promise.resolve(filter.criterion.predicate(step))
      else if filter instanceof ByLineInClass
        Promise.resolve(filter.criterion.predicate(step))
      else if filter instanceof ByClassAndMethodRegexp
        Promise.resolve(_.some filter.matches(flow), l: step.l, c: step.c)
      else if filter instanceof ByVariableNameAndValue
        Promise.resolve(
          _.find step.sn, (obj) =>
              name  = obj.n
              value = obj.v
              name == filter.criterion.key.name and ((("" + value) == ("" + filter.criterion.key.value)) or (obj?["#toString"] == ("" + filter.criterion.key.value)))
        )
      else if filter instanceof ByExpression
        context = new Proxy({},
          get: (target, prop) ->
            entity = _.find(step.sn, n: prop)
            if not entity?
              undefined
            else if "#toS" of entity
              entity["#toS"]
            else
              entity.v
        )
        jexl.eval(filter.criterion.predicate.expression, context)
      else
        throw "filter unsupported: " + filter

  occurrences: (filter) ->
    new Promise (resolve, reject) =>
      async.reduce @flow, [],
        ((all, entry, callback) =>
          @appliesAt(filter)(@flow, entry, entry.idx).then (occurrence) ->
            if occurrence
              all.push entry: entry, occurrence: occurrence
            callback(null, all)),
        ((err, result) ->
          if err then reject(err) else resolve(result))

  at: (step) ->
    @flow[step]

  length: () ->
    @flow.length

  findThisForCurrentStep: (currentStep) ->
    methodEntry = _.findLast(
      _.slice(@flow, 0, currentStep + 1),
      (entry) => _.isMatch(entry, _.pick(@flow[currentStep], "sd", "c", "m")) and _.some(entry.sn, {n: "this"}))
    if methodEntry
      _.find methodEntry.sn, {n: "this"}
    else
      undefined

  resolveNameInThis: (currentStep, name, thisId) ->
    _(@flow)
      .take(currentStep)
      .reverse()
      .flatMap "sn"
      .find(n: name, o: thisId)?.id

  valueSearch: (step) ->
    new ValueSearch @flow, step

  frames: (step) =>
    currentStackTraceDepth = @flow[step].sd
    _(_.range(1, currentStackTraceDepth + 1))
      .map (idx) =>
        _.last(_(@flow).take(step + 1).filter(sd: idx).value())
      .sortedUniq()

  tags: () ->
    _.filter(@flow, "tg")

