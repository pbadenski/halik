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
settings = require './settings'

JSZip = require 'jszip'

module.exports = (api, body) -> class
  alertify = (require 'alertify.js')
  alertify.parent(body)

  constructor: (root, sessionId, threadContext, {body} = {body: $(document.body)}) ->
    classes = threadContext.sessionAPI().classes()

    uploadSession =  () ->
      zip = new JSZip()
      _.each threadContext.all, (threadWithFlow) ->
        zip.folder("threads").file("#{threadWithFlow.thread.number}.dbg", JSON.stringify(threadWithFlow.flow))
      Promise
        .all(_.map(classes, (className) -> api.local.session.getSource sessionId, className ))
        .then ([responses]...) ->
          _.map responses, (response, idx) ->
            zip.folder("sources").file(classes[idx], response)
        .then ->
          zip.generateAsync(type: "base64", compression: "DEFLATE")
        .then (content) ->
          Promise.resolve api.remote.session.upload(sessionId, threadContext.sessionAPI().title(), content)
        .then ->
          displaySharedSessionInformation(body.find('.share-session-dialog'), sessionId)
        .catch (error) ->
          root.find(".share-session").removeClass "disabled"
          if error.status == 401
            errorMessage = "You need to <a href='#{settings.WEBSITE_URL}/login?redirect_after_action=#{location.href}'>log in</a> first."
          else if error.readyState == 0
            errorMessage = "Network error. Please make sure you're connected to Internet."
          else
            console.error error
            errorMessage = "Unknown server error"
          alertify
            .closeLogOnClick(true)
            .error errorMessage

    createShareSessionDialog = () ->
      $("<div class='share-session-dialog' style='width: 400px'>")
        .append(
          $("""<button class="close-button" type="button"><span>&times;</span></button>""")
          .click () ->
            $(this).parent().remove()
            root.removeClass "disabled"
        )
        .append $("<br style='clear: both' />")
        .append $("<div>Content of #{classes.length} source files will be shared on halik.io:</div>")
        .append(
          $("<ul />")
            .append(
              _.map(
                classes,
                (className) -> $("<li>#{className}</li>")
              ))
        )
        .append $("<div style='font-size: 80%; padding: 0.5rem 0'>Comments you added to an offline session will not be shared. This feature is on the way. Sorry. <br/> You can safely add comments to the session shared on halik.io.</div>")
        .append $("<a class='button'>Share</a></div>").click () -> uploadSession()

    displaySharedSessionInformation = (dialog, sessionId) ->
      publicUrl = "#{settings.WEBSITE_URL}/browse/#{sessionId}"
      dialog.css "width", "500px"
      dialog.empty()
      dialog.append [
        $("<input class='url' type='text' readonly value='#{publicUrl}'></input>").click( -> $(this).select())
        $("<a class='button'>OK</a></div>").click(-> root.removeClass("disabled"); $(this).parent().remove())
      ]

    root.click ->
      Promise.resolve(api.remote.session.get(sessionId))
        .then ->
          root.addClass "disabled"
          body.append displaySharedSessionInformation(createShareSessionDialog(), sessionId)
        .catch (error) ->
          if error.status == 404
            Promise.resolve(api.remote.user.get())
              .then (user) ->
                if not user.login?
                  alertify
                    .closeLogOnClick(true)
                    .log "You need to <a href='#{settings.WEBSITE_URL}/login?redirect_after_action=#{location.href}'>log in</a> first."
                else
                  ga 'send', 'event', 'Share session', 'click'
                  root.addClass "disabled"
                  body.append createShareSessionDialog()
          else
            throw error
        .catch (error) ->
          if error.readyState == 0
            alertify
              .closeLogOnClick(true)
              .error "Network error. Please make sure you're connected to Internet."
          else
            throw error
