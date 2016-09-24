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
module.exports = (fn) ->
  new Promise (resolve, reject) ->
    generator = fn()

    putInGenerator = (method) -> (val) ->
      try
        handlePromise generator[method](val)
      catch error
        reject error

    handlePromise = ({value, done}) ->
      if done
        resolve value
      else if value and value.then
        value.then putInGenerator('next'), putInGenerator('throw')
      else
        reject "Value isn't a promise!"

    handlePromise generator.next()

