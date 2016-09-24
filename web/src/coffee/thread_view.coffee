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
Mousetrap = require 'mousetrap'
alertify = (require 'alertify.js')

configureStepNavigationWithKeyboard = (stepContext) ->
  Mousetrap.unbind('i').bind('i', stepContext.stepBack)
  Mousetrap.unbind('k').bind('k', stepContext.stepOver)
  Mousetrap.unbind('j').bind('j', stepContext.stepOut)
  Mousetrap.unbind('l').bind('l', stepContext.stepIn)
  Mousetrap.unbind('right').bind 'right',  ->
    unless $(".slider").is(":focus")
      stepContext.stepToNext()
  Mousetrap.unbind('left').bind 'left',  ->
    unless $(".slider").is(":focus")
      stepContext.stepToPrevious()

module.exports = class ThreadView
  constructor: (currentThreadNumber, stepContext, threadContext) ->
    currentThread = threadContext.assignThread currentThreadNumber
    if threadContext.flowAPI().length() > 100000
      alertify
        .delay(0)
        .closeLogOnClick(true)
        ._$$alertify.notify "Recorded session is very large (> 100k steps). You may experience slowness with the UI.", "warn"

    configureStepNavigationWithKeyboard(stepContext)
