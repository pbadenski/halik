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
webpack = require 'webpack'
SingleModuleInstancePlugin = require('single-module-instance-webpack-plugin')

argv = process.argv

module.exports.loaders = [
  {
    test: /\.css$/
    loader: 'style-loader!css-loader'
  }
  {
    test: /\.png/
    loader: 'url-loader?limit=100000'
  }
  {
    test: /\.coffee$/
    loader: 'coffee!preprocess?' + argv.target.toUpperCase() + '&' + argv.env.toUpperCase()
  }
  {
    test: require.resolve('foundation-sites')
    loader: 'imports?jQuery=>$'
  }
  {
    test: require.resolve('jquery-jsonview')
    loader: 'imports?jQuery=>$'
  }
]

module.exports.plugins = [
  new (webpack.ResolverPlugin)(new (webpack.ResolverPlugin.DirectoryDescriptionFilePlugin)('bower.json', [ 'main' ]))
  new (webpack.ProvidePlugin)(
    $: 'jquery'
    jQuery: 'jquery'
    'window.jQuery': 'jquery'
    '_': 'lodash')
  new SingleModuleInstancePlugin
  new webpack.HotModuleReplacementPlugin
].concat(if argv.env == 'development' then new (webpack.SourceMapDevToolPlugin) else [])
.concat(if argv.env == 'production' then new (webpack.optimize.UglifyJsPlugin) else [])


