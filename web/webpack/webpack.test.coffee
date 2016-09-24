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
glob   = require 'glob'
path   = require 'path'

common = require './webpack.common'

module.exports =
  entry: glob.sync('./test/*.coffee').map((each) -> 'mocha!' + each)
  externals: [
    "window": "window"
  ]
  resolve:
    root: [ path.resolve('./test/coffee') ]
    extensions: [ '', '.js', '.coffee' ]
    modulesDirectories: [ 'node_modules', 'bower_components' ]
  module: loaders: common.loaders
  plugins: common.plugins
