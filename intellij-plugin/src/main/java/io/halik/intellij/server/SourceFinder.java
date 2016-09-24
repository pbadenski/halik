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
package io.halik.intellij.server;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Iterables.limit;
import static com.google.common.collect.Iterables.size;

public class SourceFinder {
    public static Optional<String> findFirstSource(String className, Project[] projects) throws InterruptedException, ExecutionException {
        Optional<String> optionalJavaSourceContent = Optional.absent();
        for (Project project : projects) {
            optionalJavaSourceContent = findSource(className, project);
            if (optionalJavaSourceContent.isPresent()) {
                break;
            }
        }
        return optionalJavaSourceContent;
    }

    private static Optional<String> findSource(final String className, final Project project) throws InterruptedException, ExecutionException {
        return ApplicationManager.getApplication().executeOnPooledThread(new Callable<Optional<String>>() {
            @Override
            public Optional<String> call() throws Exception {
                return ApplicationManager.getApplication().runReadAction(new FindFile(project, className));
            }
        }).get();
    }

    private static class FindFile implements Computable<Optional<String>> {
        private final Project project;
        private final String className;

        public FindFile(Project project, String className) {
            this.project = project;
            this.className = className;
        }

        @Override
        public Optional<String> compute() {
            PsiClass aClass = JavaPsiFacade.getInstance(project)
                    .findClass(className, GlobalSearchScope.projectScope(project));
            if (aClass == null) {
                aClass = JavaPsiFacade.getInstance(project)
                        .findClass(className, GlobalSearchScope.allScope(project));
            }
            if (aClass == null) {
                return fallbackToFileSearch(className);
            }
            // getNavigationElement to avoid showing decompiled source, where source exists
            return Optional.of(aClass.getNavigationElement().getContainingFile().getText());
        }

        private Optional<String> fallbackToFileSearch(String className) {
            Iterable<String> splitClassName = Splitter.on(".").split(className);
            String packageName = Joiner.on(".").join(limit(splitClassName, size(splitClassName) - 1));
            PsiPackage psiPackage = JavaPsiFacade.getInstance(project).findPackage(packageName);
            if (psiPackage == null) {
                return Optional.absent();
            }
            PsiFile[] files = psiPackage.getFiles(GlobalSearchScope.allScope(project));
            for (PsiFile file : files) {
                if (file.getName().startsWith(getLast(splitClassName))) {
                    return Optional.of(file.getNavigationElement().getContainingFile().getText());
                }
            }
            return Optional.absent();
        }
    }
}
