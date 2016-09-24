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

StepContext = require '../src/coffee/domain/step_context'
StepperAPI = require '../src/coffee/api/stepper_api'
FlowAPI = require '../src/coffee/api/flow_api'
expect = chai.expect
should = chai.should()

describe "Step context", ->
  describe 'step over', ->
    it 'continuous', ->
      flow = [
        {i: 0, c: "Foo", m: "x", sd: 1},
        {i: 1, c: "Foo", m: "x",  sd: 1}
      ]
      stepContext = new StepContext(stepperAPI: -> new StepperAPI(flow))
      stepContext.moveTo(0)

      stepContext.stepOver()

      stepContext.currentStep().should.equal 1

    it 'should jump over a "chasm"', ->
      flow = [
        {i: 0, c: "Foo", m: "x",  sd: 1},
        {i: 1, c: "Foo", m: "x",  sd: 2},
        {i: 2, c: "Foo", m: "x",  sd: 1}
      ]
      stepContext = new StepContext(stepperAPI: -> new StepperAPI(flow))
      stepContext.moveTo(0)

      stepContext.stepOver()

      stepContext.currentStep().should.equal 2

    it 'should step in if there is no other choice', ->
      flow = [
        {i: 0, c: "Foo", m: "x",  sd: 1},
        {i: 1, c: "Foo", m: "x",  sd: 2}
      ]
      stepContext = new StepContext(stepperAPI: -> new StepperAPI(flow))
      stepContext.moveTo(0)

      stepContext.stepOver()

      stepContext.currentStep().should.equal 1

    it 'should step out if theres an entry on the same depth level, but method has different name', ->
      flow = [
        {i: 0, c: "Foo", line: 5, m: "calls_b_and_c", sd: 1}
        {i: 1, c: "Bar", line: 10, m: "b", sd: 2},
        {i: 2, c: "Bar", line: 20, m: "c", sd: 2},
        {i: 3, c: "Foo", line: 6, m: "calss_b_and_c", sd: 1}
      ]
      stepContext = new StepContext(stepperAPI: -> new StepperAPI(flow))
      stepContext.moveTo(1)

      stepContext.stepOver()

      stepContext.currentStep().should.equal 3

    it 'should not do anything after last step', ->
      flow = [
        {i: 0, c: "Foo", m: "x",  sd: 1},
        {i: 1, c: "Foo", m: "x",  sd: 2}
      ]
      stepContext = new StepContext(stepperAPI: -> new StepperAPI(flow))
      stepContext.moveTo(1)

      stepContext.stepOver()

      stepContext.currentStep().should.equal 1

    it 'should step out to after where method was called if theres a step in the same method, but it happens after a call higher in the stack trace', ->
      flow = [
        {i: 0, c: "Foo", m: "bar",  sd: 1}
        {i: 1, c: "Foo", m: "foo",  sd: 2},
        {i: 2, c: "Foo", m: "bar",  sd: 1}
        {i: 3, c: "Foo", m: "foo",  sd: 2},
      ]
      stepContext = new StepContext(stepperAPI: -> new StepperAPI(flow))
      stepContext.moveTo(1)

      stepContext.stepOver()

      stepContext.currentStep().should.equal 2

  describe 'step in', ->
    it 'continuous', ->
      flow = [
        {i: 0, sd: 1},
        {i: 1, sd: 2}
      ]
      stepContext = new StepContext(stepperAPI: -> new StepperAPI(flow))
      stepContext.moveTo(0)

      stepContext.stepIn()

      stepContext.currentStep().should.equal 1

    it 'should to step over when cannot step in', ->
      flow = [
        {i: 0, sd: 1},
        {i: 1, sd: 1}
      ]
      stepContext = new StepContext(stepperAPI: -> new StepperAPI(flow))
      stepContext.moveTo(0)

      stepContext.stepIn()

      stepContext.currentStep().should.equal 1

    it 'should step over when cannot step in (avoid jumping over to the next "deeper" step)', ->
      flow = [
        {i: 0, sd: 1},
        {i: 1, sd: 1}
        {i: 2, sd: 2}
      ]
      stepContext = new StepContext(stepperAPI: -> new StepperAPI(flow))
      stepContext.moveTo(0)

      stepContext.stepIn()

      stepContext.currentStep().should.equal 1

  describe 'step back', ->
    it 'continuous', ->
      flow = [
        {i: 0, sd: 1},
        {i: 1, sd: 1}
      ]
      stepContext = new StepContext(stepperAPI: -> new StepperAPI(flow))
      stepContext.moveTo(1)

      stepContext.stepBack()

      stepContext.currentStep().should.equal 0

    it 'should be fine to stay at the first step', ->
      flow = [
        {i: 0, sd: 1}
      ]
      stepContext = new StepContext(stepperAPI: -> new StepperAPI(flow))
      stepContext.moveTo(0)

      stepContext.stepBack()

      stepContext.currentStep().should.equal 0

    it 'should jump over a "chasm"', ->
      flow = [
        {i: 0, sd: 1},
        {i: 1, sd: 2},
        {i: 2, sd: 1}
        {i: 3, sd: 1}
      ]
      stepContext = new StepContext(stepperAPI: -> new StepperAPI(flow))
      stepContext.moveTo(2)

      stepContext.stepBack()

      stepContext.currentStep().should.equal 0

    it 'should get out down the depth', ->
      flow = [
        {i: 0, sd: 1},
        {i: 1, sd: 2},
        {i: 2, sd: 1},
      ]
      stepContext = new StepContext(stepperAPI: -> new StepperAPI(flow))
      stepContext.moveTo(1)

      stepContext.stepBack()

      stepContext.currentStep().should.equal 0

    it 'should not get fooled by an entry at the same depth, but for a different method when stepping back', ->
      flow = [
        {i: 0, sd: 1, c: "Foo", m: "foo"},
        {i: 1, sd: 2, c: "Foo", m: "bar"},
        {i: 2, sd: 2, c: "Foo", m: "gamma"},
      ]
      stepContext = new StepContext(stepperAPI: -> new StepperAPI(flow))
      stepContext.moveTo(2)

      stepContext.stepBack()

      stepContext.currentStep().should.equal 0

    it 'should step out to before where method was called even if theres a step in the same method, but it happens later than a call higher in the stack trace', ->
      flow = [
        {i: 0, c: "Foo", m: "bar",  sd: 1}
        {i: 1, c: "Foo", m: "foo",  sd: 2},
        {i: 2, c: "Foo", m: "bar",  sd: 1}
        {i: 3, c: "Foo", m: "foo",  sd: 2},
      ]
      stepContext = new StepContext(stepperAPI: -> new StepperAPI(flow))
      stepContext.moveTo(3)

      stepContext.stepBack()

      stepContext.currentStep().should.equal 2

  describe 'step out', ->
    it 'continuous', ->
      flow = [
        {i: 0, sd: 1},
        {i: 1, sd: 2}
        {i: 2, sd: 1}
      ]
      stepContext = new StepContext(stepperAPI: -> new StepperAPI(flow))
      stepContext.moveTo(1)

      stepContext.stepOut()

      stepContext.currentStep().should.equal 0

    it 'continuous with depth difference > 1', ->
      flow = [
        {i: 0, sd: 1},
        {i: 1, sd: 3}
        {i: 2, sd: 1}
      ]
      stepContext = new StepContext(stepperAPI: -> new StepperAPI(flow))
      stepContext.moveTo(1)

      stepContext.stepOut()

      stepContext.currentStep().should.equal 0

    it 'smoothly goes only one step out', ->
      flow = [
        {i: 0, sd: 1},
        {i: 1, sd: 2},
        {i: 2, sd: 3}
        {i: 3, sd: 2},
        {i: 4, sd: 1}
      ]
      stepContext = new StepContext(stepperAPI: -> new StepperAPI(flow))
      stepContext.moveTo(2)

      stepContext.stepOut()

      stepContext.currentStep().should.equal 1

  describe 'moveTo', ->
    it 'should not propagate new position to "except" component', ->
      stepContext = new StepContext(stepperAPI: -> new StepperAPI(flow))
      ignoreComponent = {onStepChange: () ->}
      chai.spy.on(ignoreComponent, "onStepChange")
      stepContext.addListener ignoreComponent

      stepContext.moveTo 0, except: ignoreComponent

      ignoreComponent.onStepChange.should.have.been.called.exactly(0)

  describe 'stepToNext', ->
    it 'should not go beyond the flow', ->
      flow = [
        {i: 0, sd: 1},
        {i: 1, sd: 2},
      ]
      stepContext = new StepContext
        stepperAPI: -> new StepperAPI(flow)
        flowAPI: -> new FlowAPI(flow)
      stepContext.moveTo(0)

      stepContext.stepToNext()
      stepContext.stepToNext()

      stepContext.currentStep().should.equal 1
