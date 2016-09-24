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
Mousetrap = require 'mousetrap'
jexl      = require 'jexl'
require 'string.prototype.startswith'

{ByClassAndMethodRegexp, ByVariableNameAndValue, ByExpression} = require './domain/filters'

module.exports = (body) -> class
  alertify  = require 'alertify.js'
  alertify.parent(body)

  constructor: (root, filters) ->
    Mousetrap.bind("/", () -> root.focus(); false)
    root.off().keyup (event) ->
      keycode = if event.keyCode then event.keyCode else event.which
      focusOut = () -> $(event.target).val(""); root.blur()
      if keycode == 13
        ga 'send', 'event', 'Highlight', 'add'
        query = $(event.target).val().trim()
        if query.indexOf(":") != - 1
          [name, value] = query.split ":"
          filters.register new ByVariableNameAndValue name, value
        else if query.startsWith("$")
          expression = query[1..]
          jexl
            .eval(expression)
            .then () ->
              filters.register new ByExpression expression
            .catch (err) ->
              alertify.error "Invalid expression: `#{expression}`"
              throw err
        else
          try
            new RegExp(query)
            filters.register new ByClassAndMethodRegexp query
          catch error
            alertify.error error
        focusOut()
      if keycode == 27
        focusOut()
