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
module.exports = class ChangeHeatMap
  HEAT_STEPS = 20

  constructor: (root) ->
    @root = root
    @changeHeatMap = []

  onChange: (watch, diff) ->
    _.each @changeHeatMap, (each) -> each.heat--
    _.remove @changeHeatMap, ({heat}) -> heat <= 0
    
    changeDiff = _.filter diff, ({kind}) -> kind == 'E' or kind == 'N'
    @changeHeatMap = @changeHeatMap.concat(_.map(changeDiff, (each) ->
      watch: watch
      diff: each.path
      heat: HEAT_STEPS - 1
    ))
    _.map @changeHeatMap, ({watch, diff, heat}) =>
      diffInJsonHuman =
        _.reduce(diff, ((changed, key) ->
            changed
              .children("table")
              .children("tbody")
              .children("tr")
              .children("th:contains(#{key})")
              .siblings("td")
        ),
        @root.find(".watch[data-id=#{watch}] .jh-root").parent())
      diffInJsonView =
        _.reduce(diff, ((changed, key) ->
            changed
              .children("ul.obj")
              .children("li")
              .children(".prop:contains(#{key})")
              .parent()
        ),
        @root.find(".watch[data-id=#{watch}] .jsonview"))
      $(diffInJsonView.toArray().concat(diffInJsonHuman.toArray()))
        .css 'background-color': "rgba(255,191,0,#{1 / (HEAT_STEPS - (heat))})"
