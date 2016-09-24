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
window = require 'window'
window.JSHINT = (require 'jshint').JSHINT
require 'foundation-sites'
require 'codemirror/mode/javascript/javascript'
require 'codemirror/addon/lint/lint'
require 'codemirror/addon/lint/javascript-lint'
require 'codemirror/addon/lint/json-lint'
settings = require './settings'

CodeMirror = require 'codemirror'
uuid       = require 'uuid'

module.exports = class
  CODE_TEMPLATE =
    "return {\n" +
    "  // Setup method is executed once - when data is rendered for the first time\n" +
    "  // target - jQuery element, date needs be rendered inside of it\n" +
    "  // value - value of the watch when it's first rendererd \n" +
    "  // returns object that will be passed to render function\n" +
    "  setup: function (target, value) {\n    // ...\n    return {};\n},\n" +
    "  // target - jQuery element, date needs be rendered inside of it\n" +
    "  // value - current value of the watch\n" +
    "  // extra - object returned from setup() function\n" +
    "  render: function (target, value, extra) {\n    // ...\n  }\n" +
    "};"

  constructor: () ->
    @root = $("#customVisualisationEditor").foundation()

  create: (value) ->
    @showDialog undefined, value

  show: (renderer, value) ->
    @showDialog renderer, value, true

  edit: (renderer, value) ->
    if renderer.builtin
      @show renderer, value
    else
      @showDialog renderer, value

  wrapCode = (code) ->
    return "f = function () { " + code + " }"

  showDialog: (renderer, value, readOnly = false) ->
    if readOnly
      @root.find(".save").hide()
      @root.find(".title").addClass('disabled')
    else
      @root.find(".save").show()
      @root.find(".title").removeClass('disabled')
      
    @root.find(".preview").empty()
    @root.foundation('open')
    codemirror = CodeMirror(@root.find(".code").empty().get(0),
      gutters: ["CodeMirror-lint-markers"]
      lint: lintOnChange: true
      lineNumbers: true
      matchBrackets: true
      mode: 'javascript'
      readOnly: readOnly)
    new Promise (resolve, reject) =>
      @root.find(".title").val(renderer?.title or "")
      codemirror.setValue(renderer?.code or CODE_TEMPLATE)
      @root.find(".run").off('click').on 'click', =>
        try
          iframe = $("<iframe sandbox='allow-scripts' src='#{settings.SANDBOX_URL}/sandbox.html' style='width: 100%; height: 100%; border: none'></iframe>")
          @root.find(".preview").html iframe
          targetWindow = iframe[0].contentWindow
          iframe.load ->
            targetWindow.postMessage JSON.stringify(command: "code", value: codemirror.getValue()), "*"
            targetWindow.postMessage JSON.stringify(command: "setup", value: value), "*"
            targetWindow.postMessage JSON.stringify(command: "render", value: value), "*"
        catch err
          @root.find(".preview").html err
      @root.find(".save").off('click').on 'click', =>
        title = @root.find(".title").val()
        if renderer?
          renderer.title = title
        else
          renderer =
            id:    uuid.v4()
            title: title or uuid.v4()
        renderer.code = codemirror.getValue()
        @root.foundation('close')
        resolve(renderer)

