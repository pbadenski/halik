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

import com.intellij.execution.Executor;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.ui.UIBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class HalikExecutor extends Executor {

    public static final String EXECUTOR_ID = "Halik Executor";
    public static final Icon ICON = IconLoader.getIcon("/run-icon.png");

    @Override
    public String getToolWindowId() {
        return ToolWindowId.RUN;
    }

    @Override
    public Icon getToolWindowIcon() {
        return ICON;
    }

    @NotNull
    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public Icon getDisabledIcon() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Run selected configuration with Halik";
    }

    @NotNull
    @Override
    public String getActionName() {
        return UIBundle.message("tool.window.name.run", new Object[0]);
    }

    @NotNull
    @Override
    public String getId() {
        return EXECUTOR_ID;
    }

    @Override
    public String getStartActionText(String configurationName) {
        final String name = configurationName != null ? escapeMnemonicsInConfigurationName(StringUtil.first(configurationName, 30, true)) : null;
        return "Run" + (StringUtil.isEmpty(name) ? "" :  " '" + name + "'") + " with Halik";
    }

    private static String escapeMnemonicsInConfigurationName(String configurationName) {
        return configurationName.replace("_", "__");
    }

    @NotNull
    @Override
    public String getStartActionText() {
        return "Run with Halik";
    }

    @Override
    public String getContextActionId() {
        return "RunHalik";
    }

    @Override
    public String getHelpId() {
        return null;
    }
}
