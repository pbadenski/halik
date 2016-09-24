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
require 'jquery-jsonview'
require 'select2'

DeepDiff   = require 'deep-diff'
JsonHuman  = require 'json-human'
alertify   = require 'alertify.js'

window = require 'window'
iframeResizer = (require 'iframe-resizer').iframeResizer

defaultRenderers = require './default_renderers'
settings         = require './settings'
ChangeHeatMap    = require './change_heat_map'
{ByReference}    = require './domain/filters'
RendererEditor   = require './renderer_editor'

_.mixin deepOmit: (obj, iteratee, context) ->
  copy = _.omit(obj, iteratee, context)
  _.each obj, (val, key) ->
    if typeof val == 'object'
      copy[key] = _.deepOmit(val, iteratee, context)
  copy

renderers =
  TreeView: class
    render: (target, watch) ->
      if not watch.value?
        target.html "undefined"
        return

      if _.isString(watch.value)
        target.html JsonHuman.format watch.value
      else
        valueWithoutThis = if _.isObject watch.value then _.deepOmit(watch.value, "this") else watch.value
        target.JSONView valueWithoutThis, collapsed: true
      # rebuild toggled nodes
      for cls in (watch.toggled or [])
        target.JSONView "expand", target.find(cls)
      target.find(".collapser").click (evt) ->
        # remember toggled nodes
        watch.toggled =
          for elem in target.find(".collapser:contains('-') ~ .prop ~ .obj")
            ".prop:contains('#{$(elem).siblings('.prop').text().replace(/"/g, '')}') ~ .#{elem.className.replace(/\ /g, '.')}"

  Table: class
    render: (target, watch) ->
      if not watch.value?
        target.html "undefined"
        return

      bringFocusToThis = (objectIdToHtml) ->
        if watch.context['this']?
          $(objectIdToHtml[watch.context['this'].id]).addClass "highlight"
      jsonInHtml = JsonHuman.format(watch.value, { dontShowProperties: ["this"], thisProperty: "this"})
      bringFocusToThis jsonInHtml.objectMap
      target.html jsonInHtml

  Custom: class
    constructor: ->
      @rendererEditor = new RendererEditor
      @allCustomRenderers =
        JSON.parse(localStorage.customRenderers or "[]").concat(defaultRenderers)

    setupRenderer = (renderer, target, watch) ->
      watch.extra = {}
      watch.customRenderer = renderer
      iframe = $("<iframe id='sandbox-#{watch.id}' sandbox='allow-scripts' src='#{settings.SANDBOX_URL}/sandbox.html' style='border: none'></iframe>")
      target.html iframe
      watch.iframe = iframe[0]
      iframe.load ->
        iframeResizer {checkOrigin: false }, iframe[0]
        watch.iframe.contentWindow.postMessage JSON.stringify(command: "id", value: watch.id), "*"
        watch.iframe.contentWindow.postMessage JSON.stringify(command: "code", value: watch.customRenderer.code), "*"
        watch.iframe.contentWindow.postMessage JSON.stringify(command: "setup", value: watch.value), "*"
        watch.iframe.contentWindow.postMessage JSON.stringify(command: "render", value: watch.value), "*"

    configureHeader = (header, renderer) ->
      if renderer.builtin
        header.find(".edit").hide()
        header.find(".show").show()
      else
        header.find(".edit").show()
        header.find(".show").hide()

    setup: (body, header, watch) ->
      afterRendererEdited = (renderer) =>
        @allCustomRenderers =
          _(@allCustomRenderers)
            .reject id: renderer.id
            .concat renderer
            .value()
        localStorage.customRenderers =
          JSON.stringify(_.reject(@allCustomRenderers, builtin: true))
        configureHeader header, renderer
        setupRenderer renderer, body, watch
        header.find(".select-custom-renderer")
          .select2().empty()
          .select2
            data:
              _(@allCustomRenderers)
              .map ({id, title}) -> id: id, text: title
              .value()
          .val(renderer.id).trigger('change')
      header.append [
        $("<select class='select-custom-renderer float-left' style='width: 70%; margin-top: 0.25rem; float: left' />")
        $("<div class='text text-right' />")
          .append $("<a class='new' style='padding: 0 0.25rem'>New</a>").click () =>
            @rendererEditor.create watch.value
              .then (renderer) -> afterRendererEdited renderer
          .append $("<a class='edit' style='padding: 0 0.25rem'>Edit</a>").click () =>
            @rendererEditor.edit watch.customRenderer, watch.value
              .then (renderer) -> afterRendererEdited renderer
          .append $("<a class='show' style='display: none; padding: 0 0.25rem'>Show</a>").click () =>
            @rendererEditor.show watch.customRenderer, watch.value
        $("<br style='clear: both' />")
      ]
      self = this
      header.find(".select-custom-renderer")
        .select2
          placeholder: "Select a renderer",
          allowClear: true
          data:
            _(@allCustomRenderers)
            .map ({id, title}) -> id: id, text: title
            .value()
        .val(watch.customRenderer?.id or '')
        .trigger('change')
        .on "change", (e) ->
          watch.customRenderer = _.find(self.allCustomRenderers, id: $(this).val())
          configureHeader header, watch.customRenderer
          setupRenderer watch.customRenderer, body, watch
          configureHeader header, watch.customRenderer
      if watch.customRenderer?
        configureHeader header, watch.customRenderer
        setupRenderer(watch.customRenderer, body, watch)

    render: (target, watch) ->
      if watch.customRenderer?
        watch.iframe.contentWindow.postMessage JSON.stringify(command: "render", value: watch.value), "*"
      else
        target.empty()

module.exports = class Watches
  constructor: (root, @threadContext) ->
    @root = root.empty().off()
    @changeHeatMap = new ChangeHeatMap $(".watches")
    @activeCustomRenderers = []

  connectToFilters: (filters) ->
    @filters = filters

  visuals = [
    {type: "treeview", icon: "list",      renderer: new renderers.TreeView}
    {type: "table",    icon: "table",     renderer: new renderers.Table}
    {type: "custom",   icon: "bar-chart", renderer: new renderers.Custom}
  ]

  add: (watch, stepContext) =>
    ga 'send', 'event', 'Watch', 'add'
    watchElement = $("<div class='watch' data-id='#{watch.id}' style='position: relative'>")
        .append $("<div class='l-box label'>#{watch.name.short}</div>")
        .append($("<i class='watch--controls--remove fa fa-remove fa-sm'></i>").click () ->
          watch.delete()
        )
        .append(
          _.reduce(
            _.map(visuals, ({type, icon, renderer}) =>
              $("<i class='#{type} fa fa-#{icon} fa-sm'></i>")
                .click () =>
                  @root.find(".watch[data-id=#{watch.id}] .controls .selected").removeClass("selected")
                  @root.find(".watch[data-id=#{watch.id}] .controls .#{type}").addClass("selected")
                  body = @root.find(".watch[data-id=#{watch.id}] .watch--contents").empty()
                  header = @root.find(".watch[data-id=#{watch.id}] .watch--header").empty()
                  if renderer.setup?
                    renderer.setup body, header, watch
                  @onStepChange stepContext.currentStep(), stepContext.currentStep()),
            (controls, each) ->  controls.append(each),
            $("<div class='controls'>"))
          )
        .append $("<div class='watch--header'></div>")
        .append $("<div class='watch--ref-link'></div>")
        .append $("<div class='watch--contents'></div>")
    watchElement.find(".controls .#{_.first(visuals).type}").addClass("selected")
    @root.prepend watchElement

    watch.onDeleted () -> watchElement.remove()
    @onStepChange stepContext.currentStep(), stepContext.currentStep()

  __renderWatchValue: (watch, valueAtNextStep, nextStep) =>
    ref = valueAtNextStep?.this
    refLink = @root
      .find(".watch[data-id=#{watch.id}] .watch--ref-link")
      .empty()
    if ref?
      if watch.linksToReference
        refLink
          .append(
            $("<small style='cursor: hand; text-decoration: underline'>Pin this reference</small>")
              .click () => @filters.register new ByReference(ref, watch.name.long)
          )

    watchContents = @root.find(".watch[data-id=#{watch.id}] .watch--contents")
    if @root.find(".watch[data-id=#{watch.id}] .treeview.selected").length > 0
      visual = _.find(visuals, type: "treeview")
    else if @root.find(".watch[data-id=#{watch.id}] .table.selected").length > 0
      visual = _.find(visuals, type: "table")
    else if @root.find(".watch[data-id=#{watch.id}] .custom.selected").length > 0
      visual = _.find(visuals, type: "custom")

    watch.value = valueAtNextStep
    watch.context = {'this': @threadContext.flowAPI().findThisForCurrentStep(nextStep)}
    visual.renderer.render(watchContents, watch)
      
  onStepChange: (previousStep, nextStep) =>
    valueSearchAtPreviousStep = @threadContext.flowAPI().valueSearch(previousStep)
    valueSearchAtNextStep = @threadContext.flowAPI().valueSearch(nextStep)
    Promise.all(
      @filters
        .filter hasWatch: true
        .each (watch) =>
          @root.find(".watch[data-id=#{watch.id}] .highlight").removeClass "highlight"
          Promise.all([
            valueSearchAtNextStep.value(watch.criterion.predicate),
            valueSearchAtPreviousStep.value(watch.criterion.predicate)
          ]).then ([valueAtNextStep, valueAtPreviousStep]) =>
              @__renderWatchValue watch, valueAtNextStep, nextStep
              @changeHeatMap.onChange watch.id, DeepDiff.diff(valueAtPreviousStep, valueAtNextStep)
            .catch (err) ->
              alertify.error "Error while evaluating value of `#{watch.name.long}`."
              throw err
    )

