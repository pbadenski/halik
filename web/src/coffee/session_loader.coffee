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
module.exports = (api, body) -> class
  alertify = require 'alertify.js'
  alertify.parent(body)

  loadFlows = (threads) ->
    toJsonArray = (json) ->
      return undefined if not json?
      if _.isArray json
        json
      else
        $.parseJSON(
          if json.startsWith "["
            json
          else
            "[#{json.replace(/,\n?$/, "")}]")

    upgrade = (flow) ->
      if flow[0]? and "idx" of flow[0]
        upgradeEntity = (entity) ->
          if _.isObject entity
            _.assign({},
              if "name"      of entity then { n: entity.name }                    else {},
              if "id"        of entity then { id: entity.id }                     else {},
              if "objectId"  of entity then { o: entity.objectId }                else {},
              if "#class"    of entity then { '#c': entity['#class'] }            else {},
              if "#toString" of entity then { '#toS': entity['#toString'] }       else {},
              if "type"      of entity then { t: entity.type[0].toUpperCase() }   else {},
              if "value"     of entity then { v: upgradeArray(entity.value) }     else {})
          else
            entity

        upgradeArray = (object) ->
          if _.isArray object
            _.map(object, upgradeEntity)
          else
            object

        # old format with long names - need to convert
        _.map flow, (entry) ->
          _.assign({
            t: entry.ts
            i: entry.idx
            c: entry.class
            m: entry.method
            l: entry.line
            sd: entry.stackTraceDepth
            sn: _.map(entry.snapshot, upgradeEntity)
          },
          if entry.tag? then {tg: entry.tag} else {})
      else
        flow

    combineWithSnapshot = (flow, snapshot) ->
      return flow if not snapshot?
      _.each snapshot, (each) ->
        if flow[each.i]?
          flow[each.i].sn = each.sn
      flow

    attachClassNames = (flow, classes) ->
      return flow if not classes?
      _.each flow, (each) ->
        each.c = classes[each.c]
      flow

    attachMethodNames = (flow, methods) ->
      return flow if not methods?
      _.each flow, (each) ->
        each.m = methods[each.c][each.m]
      flow

    attachIndexes = (flow) ->
      _.each flow, (each, idx) ->
        each.i = idx
      flow

    Promise.all(
      _.map threads, (thread) ->
        if thread.link?
          Promise.all([$.get(thread.link)])
        else
          Promise.all([$.get(thread.flow), $.get(thread.snapshot), $.get(thread.classes), $.get(thread.methods)])
    ).then (responses) ->
      _.map(responses, ([flow, snapshot, classes, methods], idx) ->
        thread: threads[idx],
        flow:
          combineWithSnapshot(
            attachIndexes(
              attachClassNames(
                attachMethodNames(
                  upgrade(toJsonArray(flow)),
                  toJsonArray(methods)),
                toJsonArray(classes))),
              toJsonArray(snapshot))
      )

  load: (sessionId) ->
    Promise.resolve(api.default.session.get sessionId)
      .catch (error) ->
        if error.status == 404
          alertify.delay(0).error "Session `#{sessionId}` doesn't exist."
        else
          alertify.delay(0).error "Unknown server error"
      .then (threads) ->
        Promise.resolve(api.default.session.getMetadata sessionId)
          .then((metadata) ->
            if not metadata.captureCollectionsAndMaps
              $(body).find(".watches-warning").show()
          ).catch (error) -> undefined # swallow
        Promise.all([
          loadFlows(threads)
          Promise.resolve(api.default.session.getThreadsInformation sessionId).catch () -> []
        ])
      .then ([threadsWithFlows, threadsInformation]) ->
        _(threadsWithFlows)
          .reject ({flow}) -> _.isEmpty flow
          .map( (threadWithFlow) ->
            if threadWithFlow.thread.number == 1
              threadName = "main"
            else
              threadName = _.findLast(threadsInformation, id: threadWithFlow.thread.number)?.name
            threadWithFlow.thread.name = threadName or threadWithFlow.thread.number
            threadWithFlow)
          .value()

