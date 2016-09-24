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

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Strings.nullToEmpty;
import static java.util.Arrays.asList;

public class HalikEnabledConfiguration implements JDOMExternalizable {

    private static final Key<HalikEnabledConfiguration> HALIK_KEY = Key.create("io.halik");
    public static final String CURRENT_VERSION = "2";

    private String version = CURRENT_VERSION;
    private boolean debug;
    private boolean experimentalCollectionsSupport;
    private List<String> includePattern = new ArrayList<>();
    private List<String> excludePattern = new ArrayList<>();

    public static HalikEnabledConfiguration getOrCreate(final RunConfigurationBase runConfiguration) {
        HalikEnabledConfiguration configuration = runConfiguration.getCopyableUserData(HALIK_KEY);
        if (configuration == null) {
            configuration = new HalikEnabledConfiguration();

            runConfiguration.putCopyableUserData(HALIK_KEY, configuration);
        }
        return configuration;
    }


    @Override
    public void readExternal(Element element) throws InvalidDataException {
        version = element.getAttributeValue("version");
        debug = Boolean.parseBoolean(element.getAttributeValue("debug"));
        experimentalCollectionsSupport = Boolean.parseBoolean(element.getAttributeValue("experimentalCollectionsSupport"));
        if (version == null) {
            includePattern = asList(nullToEmpty(element.getAttributeValue("filters")).split(","));
            upgradeOnSave();
        } else if (version.equals(CURRENT_VERSION)) {
            includePattern = asList(nullToEmpty(element.getAttributeValue("includePattern")).split(","));
            excludePattern = asList(nullToEmpty(element.getAttributeValue("excludePattern")).split(","));
        } else {
            throw new RuntimeException("Version is unsupported: `" + version + "`");
        }
    }

    private void upgradeOnSave() {
        version = CURRENT_VERSION;
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        element.setAttribute("debug", Boolean.toString(debug));
        element.setAttribute("experimentalCollectionsSupport", Boolean.toString(experimentalCollectionsSupport));
        element.setAttribute("includePattern", Joiner.on(",").join(includePattern));
        element.setAttribute("excludePattern", Joiner.on(",").join(excludePattern));
        element.setAttribute("version", version);
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isExperimentalCollectionsSupport() {
        return experimentalCollectionsSupport;
    }

    public void setExperimentalCollectionsSupport(boolean experimentalCollectionsSupport) {
        this.experimentalCollectionsSupport = experimentalCollectionsSupport;
    }

    public List<String> getIncludePattern() {
        return includePattern;
    }

    public void setIncludePattern(List<String> includePattern) {
        this.includePattern = includePattern;
    }

    public List<String> getExcludePattern() {
        return excludePattern;
    }

    public void setExcludePattern(List<String> excludePattern) {
        this.excludePattern = excludePattern;
    }
}
