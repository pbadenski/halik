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
_    = require('lodash')
argv = require('yargs').choices('env', [
  'development'
  'production'
]).default('env', 'development').argv
argv.target = 'website'
_.merge(process.argv, argv)

express       = require 'express'
session       = require 'express-session'
flash         = require 'connect-flash'
uuid          = require 'uuid'
path          = require 'path'
fs            = require 'fs'
knox          = require 'knox'
elasticsearch = require 'elasticsearch'
ElasticsearchStore = require('connect-elasticsearch')(session)

app     = express()
http    = require('http').Server(app)

s3 = knox.createClient
  endpoint: 's3-us-west-2.amazonaws.com'
  key: process.env.AWS_ACCESS_KEY_ID
  secret: process.env.AWS_SECRET_ACCESS_KEY
  bucket: process.env.S3_BUCKET_NAME + (if process.argv.env == "production" then "" else "-development")

if argv.env == 'production'
  elasticsearch = new elasticsearch.Client(
    hosts: process.env.ELASTICSEARCH_URL
    connectionClass: require('http-aws-es')
    amazonES:
      region: 'us-west-2'
      accessKey: process.env.AWS_ACCESS_KEY_ID
      secretKey: process.env.AWS_SECRET_ACCESS_KEY)
else
  elasticsearch = new elasticsearch.Client
    host: 'localhost:9200'

app.use(flash())
app.use(session
  genid: (req) ->
    uuid.v4()
  secret: process.env.SESSION_SECRET
  resave: false
  saveUninitialized: false
  store: new ElasticsearchStore(elasticsearch)
)

app.get '/', (req, res) ->
  if req.flash('error')?.length > 0
    console.log "Flash errors", req.flash('error')

  if req.query.id?
    res.redirect 301, "/browse/#{req.query.id}"
    return

  res.sendFile path.join(__dirname + "/dist/sessions.html")

app.get '/browse', (req, res) ->
  res.redirect "/"

app.get '/browse/:id', (req, res) ->
  res.type('html').sendFile path.join(__dirname + "/dist/index.html")

app.use express.static('dist/')

if argv.env == "development"
  webpack              = require("webpack")
  webpackDevMiddleware = require("webpack-dev-middleware")
  compiler = webpack(require('../web/webpack/webpack.config'))
  app.use webpackDevMiddleware(compiler,
    publicPath: "/assets/"
    stats: colors: true
  )

passport = (require './auth')(app, elasticsearch)
Session    = (require './domain/session')(elasticsearch, s3)
Annotation = (require './domain/annotation')(elasticsearch)
(require './sessions')(app, s3, Session)
(require './annotations')(http, Annotation)
(require './users')(app, Session)

port = process.env.PORT or 80
server = http.listen port, ->
  host = server.address().address
  port = server.address().port
  console.log 'Listening at http://%s:%s', host, port
