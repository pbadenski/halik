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
Lazy = require 'lazy.js'
sizeof = (require 'sizeof').sizeof

module.exports = class ValueSearch
  constructor: (flow, step) ->
    @flow = flow
    @step = step
    @fields = _(flow)
      .take step + 1
      .map (each) -> each.sn or []
      .flatten()
      .filter (each) -> each.o? and each.n?
      .reduce(
        ((fields, obj) ->
          if not fields[obj.o]
            fields[obj.o] = {}
          fields[obj.o][obj.n] = obj
          fields
        ), {})
    @objects = _(flow)
      .take step + 1
      .map (each) -> each.sn or []
      .flatten()
      .filter (each) -> each.id?
      .reduce(
        ((objects, obj) ->
          objects[obj.id] = obj
          objects
        ), {})

  _allUntilStepReversed: (condition) =>
    Lazy(@flow)
      .take @step + 1
      .reverse()
      .map (each) -> each.sn or []
      .flatten()
      .filter if _.isFunction(condition) then condition else (each) -> _(each).isMatch(condition)

  _renderObject: (value, visitedObjects) =>
    id = value?.id
    uniqueFields = _.values(@fields[id])

    Lazy(
      Lazy(uniqueFields)
        .map (field) =>
          switch field.t
            when "P", "O"
              @fields[field.o][field.n]
            when "M", "C", "A"
              id: field.id
              n: field.n
              v: @objects[field.id].v
              t: field.t
            else
              throw new Error("unknown type `#{field.t}`")
        .map (field) => [field.n, @_render(field, _.clone(visitedObjects))]
        .toObject())

  _render: (value, visitedObjects) =>
    return value if not value?
    return value if not _.isObject value
    return value.v if _.isObject(value) and value.t == 'P'

    return {'#ref': value.id, '#CYCLE': '!!!'} if visitedObjects[value.id]
    visitedObjects[value.id] = true

    switch value.t
      when "O"
        object = @_renderObject value, _.clone(visitedObjects)
          .extend this: value.id, '#ref': value.id, "#cls": value['#c']
        if ('#toS' of value)
          object = object.extend '#toString': value['#toS']
        object.value()
      when "M"
        if not("v" of value)
          value.v = @objects[value.id].v

        _(value.v)
          .map ({k, v}) =>
            {k: @_render(k, _.clone(visitedObjects)), v: @_render(v, _.clone(visitedObjects)) }
          .extend this: value.id, '#ref': value.id
          .value()
      when "C", "A"
        if not("v" of value)
          value.v = @objects[value.id].v

        _(value.v)
          .map (el) => @_render(el, _.clone(visitedObjects))
          .extend this: value.id, '#ref': value.id
          .value()
      else
        value.v

  _valueAtStep: (condition) =>
    value = @_allUntilStepReversed(condition).find (obj) -> obj?
    if value? and "id" of value
      valueOverridenFromReferenceSearch = @_allUntilStepReversed(id: value.id).find (obj) -> obj?
      copyOfValue = _.clone value
      copyOfValue.v = valueOverridenFromReferenceSearch.v
      value = copyOfValue
    value

  value: (condition) =>
    new Promise (resolve, reject) =>
      try
        resolve(@_render(@_valueAtStep(condition), {}))
      catch err
        reject(err)

