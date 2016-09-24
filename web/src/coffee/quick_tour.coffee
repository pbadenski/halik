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
hopscotch = require 'hopscotch'

module.exports = class
  constructor: () ->
    @tour = {
      id: "halik-intro",
      showPrevButton: true,
      steps: [
        {
          target: $(".menu-logo")[0],
          title: "Welcome to Halik"
          content: "This <b>30 seconds</b> tutorial will quickly introduce you to the basic features.",
          placement: "right"
        },
        {
          target: $(".timelines-panel h4")[0],
          content: "Halik captures the whole execution flow of your program.<br/><br/> <b>Timeline</b> helps you to navigate this flow.",
          placement: "left"
        },
        {
          target: "slider",
          content: "Try it now by moving the timeline slider!",
          placement: "bottom"
        },
        {
          target: ".line-highlight",
          content: "Current line is always highlighted using a bright yellow color.",
          placement: "left"
        },
        {
          target: ".stack-trace",
          content: "Frames show you the current stack frame. <br /><br /><b>Click</b> on one of the entries to navigate to specific location in your code.",
          placement: "right"
        },
        {
          target: ".watches-panel",
          content: "Watches help you observe how a variable changes during program execution.",
          placement: "left"
          onNext: () -> $(".CodeMirror")[0].CodeMirror.scrollTo(0, 0); hopscotch.showStep(hopscotch.getCurrStepNum())
        },
        {
          target: ".cm-variable.highlight-on-hover",
          content: "<b>Double click</b> on a variable name to start 'watching' it.",
          placement: "bottom"
        },
        {
          target: "slider",
          content: "Try moving the timeline slider again to see how the variable value changes!",
          placement: "bottom",
          onPrev: () -> $(".CodeMirror")[0].CodeMirror.scrollTo(0, 0); hopscotch.showStep(hopscotch.getCurrStepNum())
        },
        {
          target: ".search",
          content: "You can easily find all executed methods.<br /><br /> <b>Try it now!</b> It's enough to type only part of a method (eg. 'quick').",
          placement: "bottom"
        },
        {
          target: ".threads",
          content: "Sometimes your program executes in many threads.<br/><br/> You can switch between different flows by selecting another thread.",
          placement: "right"
        }
      ]
    }

  start: () ->
    hopscotch.startTour(@tour)

