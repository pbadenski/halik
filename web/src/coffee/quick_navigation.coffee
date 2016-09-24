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
module.exports = class
  constructor: (@root, @stepContext, @threadContext) ->

  assignFlow: ->
    @root.hide()
    @root.find("list").empty()
    _.each @threadContext.flowAPI().tags(), (entry) =>
      @root.show()
      @root.find(".list").append(
        $("<li><a href='#'>#{entry.m} (JUnit Test)</a></li>").click () =>
          @stepContext.moveTo entry.i
          @root.find("list").removeClass('js-dropdown-active')
      )
    @root.foundation()
