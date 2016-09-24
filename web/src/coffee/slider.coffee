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
Rx = require 'rxjs'

module.exports = class Slider
  constructor: (root, stepContext, @threadContext) ->
    @root = root.empty().off()
    Rx.Observable.fromEvent(root, 'input')
      .forEach (event) ->
        step = parseInt($(event.target).val())
        stepContext.moveTo step

  assignFlow: () ->
    @root.attr 'max', @threadContext.flowAPI().length() - 1

  onStepChange: (previousStep, nextStep) ->
    if previousStep != nextStep
      @root.val(nextStep)

