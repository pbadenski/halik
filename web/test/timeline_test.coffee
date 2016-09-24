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
require 'foundation-sites'

chai = require 'chai'
chai.use(require 'chai-jquery')

Timelines = require '../src/coffee/timelines'
ThreadContext = require '../src/coffee/thread_context'
StepContext = require '../src/coffee/domain/step_context'
ValueSearch = require '../src/coffee/api/value_search'
Filters = require '../src/coffee/domain/filters'
{ByVariableName, ByClassAndMethodRegexp} = require '../src/coffee/domain/filters'

expect = chai.expect
should = chai.should()

describe "Timelines", ->
  describe "with a single flow", ->
    it 'should be able to add a timeline for an existing variable', ->
      timelineRoot = $("<div />")
      threadContext = new ThreadContext [{thread: {number: 1}, flow: [{t: 0, sn: [{n: "aVariable"}]}]}]
      threadContext.assignThread 1
      timelines = new Timelines timelineRoot, threadContext, new StepContext
      timelines.assignThread 1
      timelines.add Filters.createFilter(new ByVariableName "aVariable")

      timelineRoot.find(".timeline-row").should.exist
      timelineRoot.find(".timeline").should.have.length 1

    it 'should add an empty timeline if variable doesnt exist', ->
      timelineRoot = $("<div />")
      threadContext = new ThreadContext [{thread: {number: 1}, flow: [{t: 0, sn: []}]}]
      threadContext.assignThread 1
      timelines = new Timelines timelineRoot, threadContext, new StepContext
      timelines.assignThread 1
      timelines.add Filters.createFilter(new ByVariableName "aVariable")

      timelineRoot.find(".timeline-row").should.exist
      timelineRoot.find(".timeline").should.have.length 1

    describe "after watch is registered", ->
      it 'should not show toggle for single/many threads display', ->
        timelineRoot = $("<div />")
        threadContext = new ThreadContext [{thread: {number: 1}, flow: []}]
        threadContext.assignThread 1
        timelines = new Timelines timelineRoot, threadContext, new StepContext
        timelines.assignThread 1
        timelines.add Filters.createFilter(new ByVariableName "aVariable")

        timelineRoot.find(".timeline-view--toggle-threads").should.not.exist

      it 'when step is changed externally should mark current timeline position', ->
        timelineRoot = $("<div />")
        flow = [
          {t: 0, sn: [{n: "aVariable"}]},
          {t: 1, sn: [{n: "aVariable"}]}
        ]
        threadContext = new ThreadContext [{thread: {number: 1}, flow: flow}]
        threadContext.assignThread 1
        timelines = new Timelines timelineRoot, threadContext, new StepContext
        timelines.assignThread 1
        timelines.add Filters.createFilter(new ByVariableName "aVariable")
        timelines.onStepChange 0, 1

        timelineRoot.find("rect.current").should.exist
        timelineRoot.find("rect.current").should.have.length 1
        timelineRoot.find("rect.current").should.have.attr("x", "1")

      it 'when "next" is clicked should inform stepContext to move to the next occurrence', (done) ->
        timelineRoot = $("<div />")
        flow = [
          {t: 0, sn: [{n: "aVariable"}], i: 0},
          {t: 0, sn: [{n: "aVariable"}], i: 1}
        ]
        stepContext = {currentStep: (() -> 0), moveTo: (step) -> }
        threadContext = new ThreadContext [{thread: {number: 1}, flow: flow}]
        threadContext.assignThread 1
        timelines = new Timelines timelineRoot, threadContext, stepContext
        timelines.assignThread 1
        chai.spy.on(stepContext, "moveTo")

        timelines.add Filters.createFilter(new ByVariableName "aVariable")
        timelineRoot.find(".timeline--header--right").click()

        setTimeout ( ->
          stepContext.moveTo.should.have.been.called.with(1)
          done()
        ), 10

      it 'when "previous" is clicked should inform stepContext to move to the previous occurrence', (done) ->
        timelineRoot = $("<div />")
        flow = [
          {t: 0, sn: [{n: "aVariable"}], i: 0},
          {t: 0, sn: [{n: "aVariable"}], i: 1}
        ]
        stepContext = currentStep: (() -> 1), moveTo: (step) ->
        threadContext = new ThreadContext [{thread: {number: 1}, flow: flow}]
        threadContext.assignThread 1
        timelines = new Timelines timelineRoot, threadContext, stepContext
        timelines.assignThread 1
        chai.spy.on(stepContext, "moveTo")

        timelines.add Filters.createFilter(new ByVariableName "aVariable")
        timelineRoot.find(".timeline--header--left").click()

        setTimeout ( ->
          stepContext.moveTo.should.have.been.called.with(0)
          done()
        ), 10

      it 'when "remove" is clicked should remove timeline', ->
        timelineRoot = $("<div />")
        flow = [
          {t: 0, sn: [{n: "aVariable"}], i: 0},
          {t: 0, sn: [{n: "aVariable"}], i: 1}
        ]
        stepContext = {currentStep: (() -> 1), moveTo: (step) -> }
        threadContext = new ThreadContext [{thread: {number: 1}, flow: flow}]
        threadContext.assignThread 1
        timelines = new Timelines timelineRoot, threadContext, stepContext
        timelines.assignThread 1

        timelines.add Filters.createFilter(new ByVariableName "aVariable")
        timelineRoot.find(".timeline--header--remove").click()

        timelineRoot.should.be.empty

    describe 'should show occurrences correctly', ->
      it "after two watches are registered", (done) ->
        timelineRoot = $("<div />")
        threadContext = new ThreadContext [{thread: {number: 1}, flow: [
          {t: 0, sn: [{n: "firstVariable", v: 0}]},
          {t: 0, sn: [{n: "secondVariable", v: 1}]}
        ]}]
        threadContext.assignThread 1
        timelines = new Timelines timelineRoot, threadContext, new StepContext
        timelines.assignThread 1
        firstVariableView = timelines.add Filters.createFilter(new ByVariableName "firstVariable")
        secondVariableView = timelines.add Filters.createFilter(new ByVariableName "secondVariable")

        setTimeout ( ->
          try
            firstVariableView.root.find("rect").should.have.length 2
            secondVariableView.root.find("rect").should.have.length 2
            done()
          catch err
            done(err)
        ), 10

      it 'after higlight to the same method of a different class is registered', (done) ->
        timelineRoot = $("<div />")
        threadContext = new ThreadContext [{thread: {number: 1}, flow: [
          {c: "Foo", m: "randomMethod", l: 1},
          {c: "Bar", m: "randomMethod", l: 1}
        ]}]
        threadContext.assignThread 1
        timelines = new Timelines timelineRoot, threadContext, new StepContext
        timelines.assignThread 1
        methodView = timelines.add Filters.createFilter(new ByClassAndMethodRegexp "randomMethod")

        setTimeout ( ->
          methodView.root.find("rect:not(.current)").should.have.length 2
          done()
        ), 10

      it """after variables of the same name are registered, and theres more changes
         to them by reference - only name-based changes should be reported""", (done) ->
        timelineRoot = $("<div />")
        threadContext = new ThreadContext [{thread: {number: 1}, flow: [
          {i: 0, t: 0, sn: [{n: "var", id: 1, t: "A", v: []}]},
          {i: 1, t: 0, sn: [{id: 1, t: "A", v: [1, 2]}]},
          {i: 2, t: 0, sn: [{n: "var", id: 2, t: "A", v: []}]},
          {i: 3, t: 0, sn: [{id: 2, t: "A", v: []}]}
        ]}]
        threadContext.assignThread 1
        timelines = new Timelines timelineRoot, threadContext, new StepContext
        timelines.assignThread 1
        methodView = timelines.add Filters.createFilter(new ByVariableName "var")

        setTimeout ( ->
          try
            console.log methodView.root.html()
            methodView.root.find("rect[data-step=0]").should.exist
            methodView.root.find("rect[data-step=2]").should.exist
            done()
          catch err
            done(err)
        ), 10

    describe 'when mutlitple lines for a method in a class are found', ->
      it 'it should show all first occurrences of the method', (done) ->
        timelineRoot = $("<div />")
        threadContext = new ThreadContext [{thread: {number: 1}, flow: [
          {c: "Foo", m: "randomMethod", l: 1}, # probably method entry
          {c: "Bar", m: "randomMethod", l: 5}, # probably method entry
          {c: "Foo", m: "randomMethod", l: 5},
          {c: "Other", m: "randomMethod", l: 5}, # probably method entry
          {c: "Foo", m: "randomMethod", l: 1} # probably method entry
        ]}]
        threadContext.assignThread 1
        timelines = new Timelines timelineRoot, threadContext, new StepContext
        timelines.assignThread 1
        methodView = timelines.add Filters.createFilter(new ByClassAndMethodRegexp "randomMethod")

        setTimeout ( ->
          try
            methodView.root.find("rect:not(.current)").should.have.length 4
            done()
          catch err
            done(err)
        ), 10

  describe "with multiple flows", ->
    it 'should be able to add a timeline for variable', ->
      timelineRoot = $("<div />")
      flow = [{sn: [{n: "aVariable"}], t: 0}]
      threadContext = new ThreadContext [{flow: flow, thread: {number: 1}}, {flow: flow, thread: { number: 2}}]
      threadContext.assignThread 1
      timelines = new Timelines timelineRoot, threadContext, {currentStep: () -> 0}, true
      timelines.assignThread 1

      timelines.add Filters.createFilter(new ByVariableName "aVariable")
      timelineRoot.find('.timeline-view--toggle-threads').click()

      timelineRoot.find(".timeline-row").should.exist
      timelineRoot.find(".timeline").should.have.length 2

    describe "after watch is registered", ->
      timelineRoot = $("<div />")
      flow = [
        {sn: [{n: "aVariable"}], t: 0},
        {sn: [{n: "aVariable"}], t: 1}
      ]
      threadContext = new ThreadContext [{flow: flow, thread: {number: 1}}, {flow: flow, thread: {number: 2}}]
      threadContext.assignThread 1
      timelines = new Timelines timelineRoot, threadContext, {currentStep: () -> 0}, true
      timelines.assignThread 1
      timelines.add Filters.createFilter(new ByVariableName "aVariable")
      timelineRoot.find('.timeline-view--toggle-threads').click()

      it 'should show toggle for single/many threads display', ->
        timelineRoot.find(".timeline-view--toggle-threads").should.exist

      it 'when step is changed externally should mark current timeline position', ->
        timelines.onStepChange 0, 1

        timelineRoot.find("rect.current").should.have.length 2
        timelineRoot.find("rect.current").should.have.attr("x", "1")

    describe "after two watches are registered", ->
      timelineRoot = $("<div />")
      flow = [
        {sn: [{n: "firstVariable"}], t: 0},
        {sn: [{n: "secondVariable"}], t: 1}
      ]
      threadContext = new ThreadContext [{flow: flow, thread: {number: 1}}, {flow: flow, thread: {number: 2}}]
      threadContext.assignThread 1
      timelines = new Timelines timelineRoot, threadContext, {currentStep: () -> 0}, true
      timelines.assignThread 1
      firstVariableView = timelines.add Filters.createFilter(new ByVariableName "firstVariable")
      secondVariableView = timelines.add Filters.createFilter(new ByVariableName "secondVariable")

      it 'and toggled many threads display, should expand single variable timeline', ->
        timelineRoot.find(".timeline-view--toggle-threads").should.exist

        firstVariableView.root.find('.timeline-view--toggle-threads').click()

        firstVariableView.root.find('.timeline').should.have.length 2
        secondVariableView.root.find('.timeline').should.have.length 1

