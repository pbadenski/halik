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
argv = require('yargs')
        .choices('env', [ 'development', 'production' ])
        .choices('target', [ 'plugin', 'website' ])
        .default('env', 'development')
        .default('target', 'plugin').argv
_.merge process.argv, argv

gulp       = require('gulp')
gutil      = require('gulp-util')
sass       = require('gulp-sass')
plugins    = require('gulp-load-plugins')(
  pattern: [
    'gulp-*'
    'gulp.*'
    'main-bower-files'
  ]
  replaceString: /\bgulp[\-.]/)
browserify  = require('browserify')
buffer      = require('vinyl-buffer')
source      = require('vinyl-source-stream')
glob        = require('glob')
streamqueue = require('streamqueue')
gulpWebpack = require('webpack-stream')
webpack     = require('webpack')
path        = require('path')

distDir =
  (if argv.env == 'production' then 'release/' else 'dist/') + argv.target + '/'

gulp.task 'compile.src', ->
  gulpWebpack(require('./webpack/webpack.config.coffee'))
    .pipe gulp.dest(distDir + '/assets')

gulp.task 'compile.test', ->
  if argv.env == 'production'
    console.log 'Skipping compile.test for production build...'
    return

  gulpWebpack(require('./webpack/webpack.test.coffee'))
  .pipe plugins.concat('test.bundle.js')
  .pipe gulp.dest(distDir + '/assets')

gulp.task 'css', ->
  extCss = [
    'src/ext/jquery-color-picker/css/colorPicker.css'
    'node_modules/codemirror/lib/codemirror.css'
    'node_modules/jquery-jsonview/dist/jquery.jsonview.css'
    'node_modules/hopscotch/dist/css/hopscotch.css'
    'node_modules/hover.css/css/hover.css'
    'node_modules/c3/c3.css'
    'node_modules/json-human/css/json.human.css'
    'node_modules/select2/dist/css/select2.css'
    'node_modules/codemirror/addon/lint/lint.css'
  ]
  streamqueue(
    { objectMode: true },
    gulp.src(plugins.mainBowerFiles().concat(extCss))
      .pipe(plugins.filter('*.css')),
    gulp.src([ 'src/scss/*.scss' ])
      .pipe(sass(includePaths: [ 'node_modules/foundation-sites/scss' ])))
  .pipe plugins.concat('all.css')
  .pipe gulp.dest(distDir + '/assets/css')

gulp.task 'images', ->
  gulp.src([ 'node_modules/hopscotch/dist/img/*.png', './src/images/*' ])
    .pipe gulp.dest(distDir + '/assets/img/')

gulp.task 'fonts', ->
  gulp.src(plugins.mainBowerFiles())
    .pipe(plugins.filter([ '*.woff', '*.woff2' ]))
    .pipe gulp.dest(distDir + '/assets/fonts/')

gulp.task 'static', ->
  context = {}
  context[argv.target.toUpperCase()] = true
  context[argv.env.toUpperCase()] = true
  if argv.target == "website"
    context["HOME_URL"] = "https:\/\/halik.io"
  else
    context["HOME_URL"] = "http:\/\/localhost:33284"
  
  gulp.src([ 'src/*.html' ])
    .pipe(plugins.preprocess(context: context))
    .pipe gulp.dest(distDir)

  gulp.src([ 'src/favicon.ico' ])
    .pipe gulp.dest(distDir)

  gulp.src([
    'node_modules/json-human/src/json.human.js'
  ])
    .pipe gulp.dest(distDir + "/assets")

  gulp.src([
    'node_modules/json-human/css/json.human.css'
  ])
    .pipe gulp.dest(distDir + "/assets/css")

gulp.task 'build', [
  'compile.src', 'compile.test', 'css', 'images', 'fonts', 'static'
]

gulp.task 'default', [ 'build' ], ->
  gulp.watch [ 'bower.json', 'src/coffee/**/*.coffee' ], [ 'compile.src', 'compile.test' ]
  gulp.watch [ 'src/css/*.css', 'src/scss/*.scss' ], [ 'css' ]
  gulp.watch 'test/*.coffee', [ 'compile.test' ]
  gulp.watch [ 'src/*.html', 'src/*.ico' ], [ 'static' ]
