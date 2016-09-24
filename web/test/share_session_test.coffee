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

chai  = require 'chai'
chai.use(require 'chai-jquery')

expect = chai.expect
should = chai.should()

timeout = 5
afterAWhile = (done, f) ->
    setTimeout ( ->
      try
        f()
      catch err
        done(err)
    ), timeout

eventually = (done, f) ->
  setTimeout (->
    try
      f()
      done()
    catch err
      done(err)
  ), timeout

describe "Share session", ->
  describe 'when session doesnt exist', ->
    describe 'and they are off internet internet, and they click on "Share session" icon', ->
      stubBody = undefined
      root = undefined

      beforeEach ->
        stubBody = $("<div />")
        api =
          remote:
            session: get: -> jQuery.Deferred().reject(readyState: 0)
        ShareSession = (require '../src/coffee/share_session')(api, stubBody[0])

        root = $("<div />")
        threadContext =
          all: [{thread: {number: "1"}, flow: [c: "org/Foo"]}]
          sessionAPI: ->
            classes: -> ["org.Foo"]
            title: -> "test-title"
        new ShareSession root, "session-1", threadContext, body: stubBody

        root.click()

      it 'should tell the user to connect to the internet', (done) ->
        if window._phantom
          done()
          return

        eventually done, ->
          stubBody.find(".alertify-logs").text().should.equal "Network error. Please make sure you're connected to Internet."

    describe 'and user is not logged in, and they click on "Share session" icon', ->
      stubBody = undefined
      root = undefined

      beforeEach ->
        stubBody = $("<div />")
        api =
          remote:
            session: get: -> jQuery.Deferred().reject(status: 404)
            user: get: -> jQuery.Deferred().resolve({})
        ShareSession = (require '../src/coffee/share_session')(api, stubBody[0])

        root = $("<div />")
        threadContext =
          all: [{thread: {number: "1"}, flow: [c: "org/Foo"]}]
          sessionAPI: ->
            classes: -> ["org.Foo"]
            title: -> "test-title"
        new ShareSession root, "session-1", threadContext, body: stubBody

        root.click()

      it 'should tell the user to log in', (done) ->
        # fails on phantom, not sure why.. :(
        if window._phantom
          done()
          return
        eventually done, ->
          stubBody.find(".alertify-logs").text().should.equal "You need to log in first."

      it 'should keep share session icon enabled', (done) ->
        eventually done, ->
          root.should.not.have.class "disabled"

    describe 'and  user is logged in, and they click on "Share session" icon', ->
      root     = undefined
      stubBody = undefined
      api      = undefined

      beforeEach () ->
        stubBody = $("<div />")
        api =
          remote:
            user: get: -> jQuery.Deferred().resolve(login: "testuser")
            session:
              get: -> jQuery.Deferred().reject(status: 404)
              upload: -> jQuery.Deferred().resolve()
          local:
            session: getSource: (sessionId, className) -> jQuery.Deferred().resolve(className)
        ShareSession = (require '../src/coffee/share_session')(api, stubBody[0])

        root = $("<div />")
        threadContext =
          all: [{thread: {number: "1"}, flow: [c: "org/Foo"]}]
          sessionAPI: ->
            classes: -> ["org.Foo"]
            title: -> "test-title"
        new ShareSession root, "session-1", threadContext, body: stubBody

        root.click()

      it 'should disable share session icon', (done) ->
        eventually done, ->
          root.should.have.class "disabled"

      it 'should display share session dialog', (done) ->
        eventually done, ->
          root.should.have.class "disabled"
          stubBody.find(".share-session-dialog").should.exist

      it 'should tell user about the content to be uploaded', (done) ->
        eventually done, ->
          stubBody.find(".share-session-dialog ul").prev().text().should.equal "Content of 1 source files will be shared on halik.io:"
          stubBody.find(".share-session-dialog ul").text().should.equal "org.Foo"

      it 'should enable share session icon if user closed the share session dialog without sharing', (done) ->
        afterAWhile done, ->
          stubBody.find(".share-session-dialog .close-button").click()

          eventually done, ->
            root.should.not.have.class "disabled"

      describe 'and they decide to "Share" the session', ->
        it 'should share session', (done) ->
          api.remote.session.upload = (sessionId, title, content) ->
            try
              sessionId.should.equal "session-1"
              title.should.equal "test-title"
              done()
            catch err
              done(err)
              jQuery.Deferred().reject()
            jQuery.Deferred().resolve()
          afterAWhile done, ->
            stubBody.find(".share-session-dialog .button:contains('Share')").click()

        it 'should tell the user link to session', (done) ->
          if window._phantom
            done()
            return
          afterAWhile done, ->
            stubBody.find(".share-session-dialog .button:contains('Share')").click()

            eventually done, ->
              stubBody.find(".share-session-dialog .url").val().should.equal "http://development-browse.halik.io/browse/session-1"

        describe 'and when they click "OK"', ->
          it 'should enable "Share session" icon again', (done) ->
            if window._phantom
              done()
              return
            afterAWhile done, ->
              stubBody.find(".share-session-dialog .button:contains('Share')").click()
              
              afterAWhile done, ->
                stubBody.find(".share-session-dialog .button:contains('OK')").click()

                eventually done, ->
                  root.should.not.have.class "disabled"

