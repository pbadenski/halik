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
module.exports =
[
   {
      "id":"fa5da5f9-073c-4a29-9f1b-9dd95953cc66",
      "title":"[builtin] array of ints as bar chart",
      "builtin": true,
      "code":"return {\n  setup: function (target, value) {\n    $(target)\n\t  .width(\"300px\")\n      .height(\"300px\");\n    return {};\n  },\n  render: function (target, value, extra) {\n    if (value == undefined) {\n      $(target).html(\"undefined\");\n      return;\n    }\n    c3.generate({\n      bindto: $(\"#contents\")[0],\n      transition: { duration: 0 },\n      data: {\n        columns: [[\"array\"].concat(value)],\n        type: 'bar'\n      },\n      bar: {\n        width: {\n          ratio: 1.01\n        }\n      }\n    });\n  }\n};"
   },
   {
      "id":"b42c1491-9c18-4de1-9c1b-6cd6316d1685",
      "title":"[builtin] neural network",
      "builtin": true,
      "code":"return {\n  setup: function (target) {\n    var options = {\n      height: '300px',\n      width: '300px',\n      layout: {\n        randomSeed: undefined,\n        improvedLayout: true,\n        hierarchical: {\n          enabled: true,\n          levelSeparation: 180,\n          sortMethod: 'directed'\n        }\n      }\n    };\n    var network = \n      new vis.Network(target, {nodes: [], edges: []}, options);\n    return {network: network };\n  },\n  render: function (target, value, extra) {\n    if (value === undefined) {\n       extra.network.setData({nodes: [], edges: []});\n\t   return;\n  \t}\n    var visitChildren = function(node, idx, nodes, edges) {\n      var previousIdx = idx;\n      for (var i = 0; i < node.inputs.length; i++) {\n        idx++;\n        nodes.push({\n          id: idx,\n          fired: node.inputs[i].fired,\n          label: 'weight :' + node.inputs[i].weight + \n          (node.inputs[i].threshold !== 0 ? ('\\n threshold: ' + node.inputs[i].threshold) : \"\")\n        });\n        edges.push({\n          from: previousIdx,\n          to: idx\n        });\n        if (node.inputs[i].inputs !== undefined) {\n          idx = visitChildren(node.inputs[i], idx, nodes, edges);\n        }\n      }\n      return idx;\n    };\n    var nodes = [{\n      id: 1,\n      fired: value.fired,\n      label: 'threshold: ' + value.threshold\n    }];\n    var edges = [];\n    visitChildren(value, 1, nodes, edges);\n    for (var i = 0; i < nodes.length; i++) {\n      if (nodes[i].fired) {\n        nodes[i].color = 'lightgreen';\n      } else {\n        nodes[i].color = 'pink';\n      }\n    }\n    extra.network.setData({nodes: nodes, edges: edges});\n  }\n};\n"
   }
]
