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
_       = require 'lodash'
path    = require 'path'
yauzl   = require 'yauzl'
gen     = require '../gen'

module.exports = (elasticsearch, s3) -> new class
  usersIndex = if process.argv.env == 'production' then 'users' else 'users-development'
  elasticsearch.indices.create index: usersIndex

  upload: ({id, title, userId, zipContent}) ->
    self = this
    new Promise (resolve, reject) ->
      numOfPutRequests = 0
      totalSize = 0
      yauzl.fromBuffer Buffer.concat(zipContent), (error, zipfile) ->
        zipfile.on 'entry', (entry) ->
          zipfile.openReadStream entry, (error, readStream) ->
            isFile = !entry.fileName.endsWith('/')
            if isFile
              numOfPutRequests++
              if numOfPutRequests > 10000
                reject new Error
                  sessionId: id
                  message: "num of put requests exceeded 10k during session upload"

              totalSize += entry.uncompressedSize
              readStream
                .pipe(s3.put(path.join(id, entry.fileName), 'Content-Length': entry.uncompressedSize))
                .on('response', (response) -> response.pipe process.stdout)
        zipfile.on 'end', () ->
          gen ->
            try
              yield self.create
                id: id
                title: title
                totalSize: totalSize
                userId: userId
              resolve()
            catch err
              reject(err)

  create: ({id, title, totalSize, userId}) ->
    gen ->
      yield elasticsearch.create
        index: usersIndex
        type: 'session'
        id: id
        body:
          user: userId
          title: title
          totalSize: totalSize
          uploadedAt: new Date

  forUser: (userId) ->
    gen ->
      try
        sessions = yield elasticsearch.search
          index: usersIndex
          type: 'session'
          body: query: match: user: userId
        _.map sessions.hits.hits, (each) ->
          _.merge(
            {},
            _.omit(each._source, "user", "totalSize", "uploadedAt"),
            id: each._id,
            size: each._source.totalSize,
            date: each._source.uploadedAt)
      catch err
        throw err

  last: (limit) ->
    gen ->
      try
        sessions = yield elasticsearch.search
          index: usersIndex
          type: 'session'
          sort: "uploadedAt:desc"
          size: limit
        _.map sessions.hits.hits, (each) ->
          _.merge {}, each._source, id: each._id
      catch err
        if err.status == 400
          return []
        else
          throw err

