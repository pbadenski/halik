/*
 *   Copyright (C) 2016 Pawel Badenski
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.halik.intellij.server.routes;

import com.google.common.base.Optional;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import io.halik.intellij.server.SourceFinder;
import spark.Request;
import spark.Response;
import spark.Route;

import static java.lang.String.format;

public class Source implements Route {

    private final ProjectManager projectManager;

    public Source(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String className = request.params("class");
        Optional<String> optionalJavaSourceContent = SourceFinder.findFirstSource(className, projectManager.getOpenProjects());
        if (optionalJavaSourceContent.isPresent()) {
            return optionalJavaSourceContent.get();
        } else {
            response.status(404);
            return format("File not found: %s", className);
        }
    }
}
