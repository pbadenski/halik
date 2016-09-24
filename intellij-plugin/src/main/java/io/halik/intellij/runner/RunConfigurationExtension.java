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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Location;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.projectView.impl.nodes.PackageUtil;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPackage;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.classFilter.ClassFilter;
import com.intellij.ui.classFilter.ClassFilterEditor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.JBUI;
import io.halik.intellij.server.Config;
import io.halik.intellij.server.Server;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import static com.google.common.base.Functions.compose;
import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Iterables.transform;
import static com.intellij.util.containers.ContainerUtilRt.newArrayList;
import static java.util.Arrays.asList;

public class RunConfigurationExtension extends com.intellij.execution.RunConfigurationExtension {
    public static final String PLUGIN_ID = "io.halik.intellij-plugin";
    private static final String BOOTCLASSPATH_JAR = "bootclasspath-1.0.0-SNAPSHOT.jar";
    private static final String JAVAAGENT_UBER_JAR = "uber-javaagent-1.0.0-SNAPSHOT.jar";

    @NotNull
    private static Function<String, String> bytecodePackageFormat() {
        return new Function<String, String>() {
            @Override
            public String apply(String pattern) {
                return pattern.replaceAll("\\.", "/");
            }
        };
    }

    private static Function<String, String> toJavaAgentPattern() {
        return new Function<String, String>() {
            @Override
            public String apply(String pattern) {
                if (pattern.endsWith(".*")) {
                    return pattern.substring(0, pattern.length() - 2);
                }
                return pattern;
            }
        };
    }

    @Override
    public <T extends RunConfigurationBase> void updateJavaParameters(T applicationConfiguration, JavaParameters javaParameters, RunnerSettings runnerSettings) throws ExecutionException {
        if (runnerSettings instanceof HalikConfigurationData) {
            HalikEnabledConfiguration configuration = HalikEnabledConfiguration.getOrCreate(applicationConfiguration);

            String bootclasspathJar = PluginManager.getPlugin(PluginId.getId(PLUGIN_ID)).getPath() + "/lib/" + BOOTCLASSPATH_JAR;
            javaParameters.getVMParametersList()
                    .addParametersString(
                            String.format("-Xbootclasspath/a:\"%s\"", bootclasspathJar
                            ));

            String javaagentJar = PluginManager.getPlugin(PluginId.getId(PLUGIN_ID)).getPath() + "/lib/" + JAVAAGENT_UBER_JAR;
            javaParameters.getVMParametersList()
                    .addParametersString(
                            String.format("-javaagent:\"%s\"=" + javaagentArguments(configuration),
                                    javaagentJar
                            ));
        }
    }

    @NotNull
    private static String javaagentArguments(HalikEnabledConfiguration configuration) {
        String include = Joiner.on("|").join(
                transform(configuration.getIncludePattern(), compose(bytecodePackageFormat(), toJavaAgentPattern())));
        String exclude = Joiner.on("|").join(
                transform(configuration.getExcludePattern(), compose(bytecodePackageFormat(), toJavaAgentPattern())));

        return String.format("include:%s,exclude:%s,debug:%s,experimentalCollections:%s," +
                "browserUrl:localhost:%s,sessionsDir:\"%s\"",
                include, exclude, configuration.isDebug(), configuration.isExperimentalCollectionsSupport(),
                Config.PORT, Config.SESSIONS_DIR);
    }

    private List<PsiPackage> getPackages(Project project) {
        final List<VirtualFile> sourceRoots = new ArrayList<>();
        final ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);
        ContainerUtil.addAll(sourceRoots, projectRootManager.getContentSourceRoots());

        final PsiManager psiManager = PsiManager.getInstance(project);
        final Set<PsiPackage> topLevelPackages = new HashSet<>();

        for (final VirtualFile root : sourceRoots) {
            final PsiDirectory directory = psiManager.findDirectory(root);
            if (directory == null) {
                continue;
            }
            final PsiPackage directoryPackage = JavaDirectoryService.getInstance().getPackage(directory);

            if (directoryPackage == null || PackageUtil.isPackageDefault(directoryPackage)) {
                // add subpackages
                final PsiDirectory[] subdirectories = directory.getSubdirectories();
                for (PsiDirectory subdirectory : subdirectories) {
                    final PsiPackage aPackage = JavaDirectoryService.getInstance().getPackage(subdirectory);
                    if (aPackage != null && !PackageUtil.isPackageDefault(aPackage)) {
                        topLevelPackages.add(aPackage);
                    }
                }
            } else {
                // this is the case when a source root has package prefix assigned
                topLevelPackages.add(directoryPackage);
            }
        }

        return new ArrayList<>(topLevelPackages);
    }

    @Override
    protected void extendCreatedConfiguration(@NotNull RunConfigurationBase configuration, @NotNull Location location) {
        HalikEnabledConfiguration halikEnabledConfiguration = HalikEnabledConfiguration.getOrCreate(configuration);
        if (Rules.isSupported(configuration)) {
            Iterable<String> packages = toString(extractWriteable(subpackages(getPackages(configuration.getProject()))));
            halikEnabledConfiguration.setIncludePattern(Lists.newArrayList(packages));
        }
    }

    private Iterable<String> toString(Iterable<PsiPackage> packages) {
        return Iterables.transform(packages, new Function<PsiPackage, String>() {
            @Override
            public String apply(PsiPackage psiPackage) {
                return psiPackage.getQualifiedName();
            }
        });
    }

    private Iterable<PsiPackage> extractWriteable(Iterable<PsiPackage> packages) {
        return Iterables.filter(packages, new Predicate<PsiPackage>() {
            @Override
            public boolean apply(PsiPackage psiPackage) {
                return Iterables.any(Lists.newArrayList(psiPackage.getDirectories()), new Predicate<PsiDirectory>() {
                    @Override
                    public boolean apply(PsiDirectory directory) {
                        return directory.isWritable();
                    }
                });
            }
        });
    }

    private Iterable<PsiPackage> subpackages(List<PsiPackage> packages) {
        return Iterables.concat(Iterables.transform(packages, new Function<PsiPackage, List<PsiPackage>>() {
            @Override
            public List<PsiPackage> apply(PsiPackage psiPackage) {
                return Arrays.asList(psiPackage.getSubPackages());
            }
        }));
    }

    @Override
    protected void readExternal(RunConfigurationBase configuration, Element element) throws InvalidDataException {
        HalikEnabledConfiguration.getOrCreate(configuration).readExternal(element);
        }

    @Override
    protected void writeExternal(RunConfigurationBase configuration, Element element) throws WriteExternalException {
        HalikEnabledConfiguration.getOrCreate(configuration).writeExternal(element);
    }

    @Nullable
    @Override
    protected <P extends RunConfigurationBase> SettingsEditor<P> createEditor(@NotNull final P configuration) {
        return new HalikConfigurable<>(configuration);
    }

    @Nullable
    @Override
    protected String getEditorTitle() {
        return "Halik";
    }

    @Override
    protected boolean isApplicableFor(RunConfigurationBase configuration) {
        return Rules.isSupported(configuration);
    }

    private class HalikConfigurable<P extends RunConfigurationBase> extends SettingsEditor<P> {
        private final P configuration;
        private ClassFilterEditor includePackageFilterEditor;
        private JBCheckBox debugCheckbox;
        private JBCheckBox experimentalCollectionsSupportCheckbox;
        private ClassFilterEditor excludePackageFilterEditor;

        public HalikConfigurable(P configuration) {
            this.configuration = configuration;

        }

        @Override
        protected void resetEditorFrom(P settings) {
            HalikEnabledConfiguration configuration = HalikEnabledConfiguration.getOrCreate(settings);
            includePackageFilterEditor.setFilters(toArray(transform(configuration.getIncludePattern(), toClassFilter()), ClassFilter.class));
            excludePackageFilterEditor.setFilters(toArray(transform(configuration.getExcludePattern(), toClassFilter()), ClassFilter.class));
            debugCheckbox.setSelected(configuration.isDebug());
            experimentalCollectionsSupportCheckbox.setSelected(configuration.isExperimentalCollectionsSupport());
        }

        @NotNull
        private Function<String, ClassFilter> toClassFilter() {
            return new Function<String, ClassFilter>() {
                @Override
                public ClassFilter apply(String pattern) {
                    return new ClassFilter(pattern);
                }
            };
        }

        @NotNull
        private Function<ClassFilter, String> toPattern() {
            return new Function<ClassFilter, String>() {
                @Override
                public String apply(ClassFilter filter) {
                    return  filter.getPattern();
                }
            };
        }

        @Override
        protected void applyEditorTo(P settings) throws ConfigurationException {
            HalikEnabledConfiguration configuration = HalikEnabledConfiguration.getOrCreate(settings);
            configuration.setIncludePattern(newArrayList(transform(asList(includePackageFilterEditor.getFilters()), toPattern())));
            configuration.setExcludePattern(newArrayList(transform(asList(excludePackageFilterEditor.getFilters()), toPattern())));
            configuration.setDebug(debugCheckbox.isSelected());
            configuration.setExperimentalCollectionsSupport(experimentalCollectionsSupportCheckbox.isSelected());
        }

        @NotNull
        @Override
        protected JComponent createEditor() {
            JPanel mainPanel = new JPanel(new GridBagLayout());

            includePackageFilterEditor = new PackageOnlyFilterEditor(configuration.getProject());
            includePackageFilterEditor.setBorder(IdeBorderFactory.createTitledBorder("Packages and classes to include in Halik session", false));

            excludePackageFilterEditor = new PackageOnlyFilterEditor(configuration.getProject());
            excludePackageFilterEditor.setBorder(IdeBorderFactory.createTitledBorder("Packages and classes to exclude from Halik session", false));

            final GridBagConstraints bagConstraints =
                    new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                            JBUI.emptyInsets(), 0, 0);
            bagConstraints.weightx = 0.5;
            bagConstraints.weighty = 1;
            mainPanel.add(includePackageFilterEditor, bagConstraints);

            bagConstraints.gridx = 1;
            mainPanel.add(excludePackageFilterEditor, bagConstraints);

            bagConstraints.gridx = 0;
            bagConstraints.gridy = GridBagConstraints.RELATIVE;
            bagConstraints.gridwidth = 2;
            bagConstraints.weightx = 1;
            bagConstraints.weighty = 0;
            experimentalCollectionsSupportCheckbox = new JBCheckBox("[Experimental] Record collections and maps");
            mainPanel.add(experimentalCollectionsSupportCheckbox, bagConstraints);

            debugCheckbox = new JBCheckBox("Generate debug information");
            mainPanel.add(debugCheckbox, bagConstraints);
            return mainPanel;
        }

    }
}
