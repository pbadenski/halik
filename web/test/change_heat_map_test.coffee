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
chai = require 'chai'
chai.use(require 'chai-jquery')

JsonHuman = require 'json-human'
DeepDiff = require 'deep-diff'

ChangeHeatMap = require '../src/coffee/change_heat_map'
expect = chai.expect
should = chai.should()

describe "Change heat map", ->
  it 'applies for arrays', ->
    jsonInHtml = JsonHuman.format([0, 1])
    root = $("<div></div>").append($("<div class='watch' data-id='test'></div>").append(jsonInHtml))
    changeHeatMap = new ChangeHeatMap(root)
    
    changeHeatMap.onChange("test", DeepDiff.diff([0, 1], [0,2]))
    
    root.find("td:nth(1)").css("background-color").should.equal "rgb(255, 191, 0)"

  it 'applies for objects', ->
    jsonInHtml = JsonHuman.format(a: 1)
    root = $("<div></div>").append($("<div class='watch' data-id='test'></div>").append(jsonInHtml))
    changeHeatMap = new ChangeHeatMap(root)
    
    changeHeatMap.onChange("test", DeepDiff.diff({a: 1}, {a: 2}))
    
    root.find("td:nth(0)").css("background-color").should.equal "rgb(255, 191, 0)"

  it 'applies for embedded objects', ->
    jsonInHtml = JsonHuman.format(a: {b: 1})
    root = $("<div></div>").append($("<div class='watch' data-id='test'></div>").append(jsonInHtml))
    changeHeatMap = new ChangeHeatMap(root)
    
    changeHeatMap.onChange("test", DeepDiff.diff({a: {b: 1}}, {a: {b: 2}}))
    
    root.find("td td:nth(0)").css("background-color").should.equal "rgb(255, 191, 0)"

  it 'higlight gets lighter each "iteration" after applied', ->
    jsonInHtml = JsonHuman.format([0, 1])
    root = $("<div></div>").append($("<div class='watch' data-id='test'></div>").append(jsonInHtml))
    changeHeatMap = new ChangeHeatMap(root)
    
    changeHeatMap.onChange("test", DeepDiff.diff([0, 1], [0,2]))
    
    root.find("td:nth(1)").css("background-color").should.equal "rgb(255, 191, 0)"

    changeHeatMap.onChange("test")

    root.find("td:nth(1)").css("background-color").should.match /rgba\(255, 191, 0, 0.49....\)/
    
