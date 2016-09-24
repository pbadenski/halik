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
# @ifdef PLUGIN
# @ifdef DEVELOPMENT
  WEBSITE_URL: "http://development-browse.halik.io"
# @endif
# @ifdef PRODUCTION
  WEBSITE_URL: "https://preview.halik.io"
# @endif
# @endif
# @ifdef WEBSITE
  WEBSITE_URL: ""
# @endif
# @ifdef PLUGIN
  SANDBOX_URL: "http://127.0.0.1:33284"
# @endif
# @ifdef WEBSITE
# @ifdef DEVELOPMENT
  SANDBOX_URL: "http://127.0.0.1"
# @endif
# @ifdef PRODUCTION
  SANDBOX_URL: "https://halik.xyz"
# @endif
# @endif
