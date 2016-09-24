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
module.exports = class ThreadList
  constructor: (threadViewClass, stepContext, threadContext) ->
    threads = _.map threadContext.all, "thread"
    threadContext.addListener
      onThreadChange: ({thread: {number}}) ->
        $(".threads > select").val(number)

    _.each threads, (thread) ->
      $(".threads > select").append(
        $("<option value='#{thread.number}'>#{thread.name}</option>")
      )
    if threads.length == 1
      $(".threads > select").attr('disabled', 'true')
    else
      $(".threads > select").change () ->
        ga 'send', 'event', 'Thread', 'select'
        new threadViewClass parseInt($(this).val()), stepContext, threadContext

