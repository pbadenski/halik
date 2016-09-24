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
_          = require 'lodash'
cors       = require 'cors'
passport   = require 'passport'
xssFilters = require 'xss-filters'

gen        = require './gen'

module.exports = (app, elasticsearch)  ->
  usersIndex = if process.argv.env == 'production' then 'users' else 'users-development'

  app.use(passport.initialize())
  app.use(passport.session())
  GitHubStrategy = require('passport-github').Strategy

  GITHUB_CLIENT_ID     = process.env.GITHUB_CLIENT_ID
  GITHUB_CLIENT_SECRET = process.env.GITHUB_CLIENT_SECRET

  passport.serializeUser (user, done) ->
    done(null, user.id)

  passport.deserializeUser (id, done) ->
    gen ->
      try
        user = yield elasticsearch.get
          index: usersIndex
          type: 'user'
          id: id
        done(null, user._source)
      catch err
        console.error err
        done(null, null)
        return

  passport.use new GitHubStrategy {
    sessionKey: "oauth2:github"
    clientID: GITHUB_CLIENT_ID
    clientSecret: GITHUB_CLIENT_SECRET
    state: true
  }, (accessToken, refreshToken, profile, done) ->
    gen ->
      try
        body = yield elasticsearch.get
          index: usersIndex
          type: 'user'
          id: profile.id
      catch err
        if err.status != 404
          console.error err
          done(null, null)
          return

      try
        if not body?
          yield elasticsearch.create
            index: usersIndex
            type: 'user'
            id: profile.id
            body: _.pick(profile, "id", "username", "provider", "displayName")
      catch err
        console.error err
        done(null, null)
        return

      done(null, profile)

  sanitizeRedirect = (redirectUrl) ->
    if redirectUrl?
      if redirectUrl.startsWith('http://localhost:33284') or
         redirectUrl.startsWith('http://development-browse.halik.io') or
         redirectUrl.startsWith('https://browse.halik.io') or
         redirectUrl.startsWith('https://preview.halik.io') or
         redirectUrl.startsWith('/')
        redirectUrl
      else
        '/' + redirectUrl
    else
      redirectUrl

  app.get '/auth/github', ((req, res, next) ->
      next()
    ), passport.authenticate('github')

  app.get '/auth/github/callback', passport.authenticate('github', failureRedirect: '/', failureFlash: true), (req, res, next) ->
    res.cookie 'signedInUser', req.user.username
    res.redirect (req.session.redirect_after_action or '/')

  app.get '/login', (req, res) ->
    req.session.redirect_after_action = sanitizeRedirect(req.query.redirect_after_action)
    res.redirect '/auth/github'

  app.get '/logout', (req, res) ->
    req.logout()
    res.redirect (sanitizeRedirect(req.query.redirect_after_action) or '/')

  return passport
