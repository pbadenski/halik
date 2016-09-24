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
package io.halik.intellij.runner;

import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.PackageChooser;
import com.intellij.psi.PsiPackage;
import com.intellij.ui.classFilter.ClassFilterEditor;
import com.intellij.util.IconUtil;

import javax.swing.*;
import java.util.List;

public class PackageOnlyFilterEditor extends ClassFilterEditor {
    public PackageOnlyFilterEditor(Project project) {
        super(project);
    }

    @Override
    protected boolean addPatternButtonVisible() {
        return true;
    }

    protected String getAddButtonText() {
        return "Add package";
    }

    @Override
    protected Icon getAddButtonIcon() {
        return IconUtil.getAddPackageIcon();
    }

    @Override
    protected void addClassFilter() {
        itsReallyPackageFilter();
    }

    private void itsReallyPackageFilter() {
        PackageChooser chooser =
                new PackageChooserDialog("Choose package", myProject);
        if (chooser.showAndGet()) {
            List<PsiPackage> packages = chooser.getSelectedPackages();
            if (!packages.isEmpty()) {
                for (final PsiPackage aPackage : packages) {
                    final String fqName = aPackage.getQualifiedName();
                    final String pattern = fqName.length() > 0 ? fqName + ".*" : "*";
                    myTableModel.addRow(createFilter(pattern));
                }
                int row = myTableModel.getRowCount() - 1;
                myTable.getSelectionModel().setSelectionInterval(row, row);
                myTable.scrollRectToVisible(myTable.getCellRect(row, 0, true));
                myTable.requestFocus();
            }
        }
    }
}
