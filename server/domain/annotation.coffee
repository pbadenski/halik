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
_            = require 'lodash'

module.exports = (elasticsearch) ->
  annotationsIndex = if process.argv.env == 'production' then 'annotations' else 'annotations-development'
  elasticsearch.indices.create index: annotationsIndex

  query: (query) ->
    elasticsearch.search(
      index: annotationsIndex
      type: 'annotation'
      body: query: bool: must: _.map(query, (v, k) ->
        filter = {}
        filter[k] = v
        { match: filter }
      )).then (body) ->
        _.map body.hits.hits, (each) ->
          annotation = each._source
          annotation.ranges = JSON.parse(annotation.ranges)
          annotation
  create: (annotation) ->
    annotation.ranges = JSON.stringify(annotation.ranges)
    elasticsearch.create
      index: annotationsIndex
      type: 'annotation'
      id: annotation.id
      body: annotation
  update: (annotation) ->
    elasticsearch.update
      index: annotationsIndex
      type: 'annotation'
      id: annotation.id
      body: doc: text: annotation.text
  delete: (annotation) ->
    elasticsearch.delete
      index: annotationsIndex
      type: 'annotation'
      id: annotation.id

