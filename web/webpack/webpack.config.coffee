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

argv = process.argv
distDir =
  (if argv.env == 'production' then 'release/' else 'dist/') + argv.target + '/'

root = __dirname + "/../"
module.exports =
  entry: glob.sync(root + './src/coffee/**/*.coffee').concat(
    if argv.env == 'development' then [ 'webpack/hot/dev-server' ] else []
  )
  externals: [
    "window": "window"
  ]
  resolveLoader:
    modulesDirectories: [ path.resolve(root, 'node_modules') ]
  resolve:
    root: [ path.resolve(root, './src/coffee') ]
    extensions: [
      ''
      '.js'
      '.coffee'
    ]
    modulesDirectories: [
      'node_modules'
      'bower_components'
    ]
  module:
    preLoaders: [
      {
        test: /\.coffee$/,
        exclude: /node_modules/,
        loader: "coffeelint-loader"
      }
    ]
    loaders: common.loaders
  coffeelint:
    configFile: path.resolve(root, 'coffeelint.json')
  plugins: common.plugins
  output:
    filename: '[name].bundle.js'
    path: root + '/' + distDir + '/assets'
    publicPath: '/assets/'
