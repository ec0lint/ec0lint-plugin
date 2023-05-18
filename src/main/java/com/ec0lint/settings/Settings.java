package com.ec0lint.settings;

import com.ec0lint.utils.Ec0lintFinder;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "Ec0lintProjectComponent",
        storages = {@Storage("ec0lintPlugin.xml")}
)
public class Settings implements PersistentStateComponent<Settings> {
    public String ec0lintRcFile = Ec0lintFinder.EC0LINTRC;
    public String rulesPath = "";
    public String builtinRulesPath = "";
    public String ec0lintExecutable = "";
    public String nodeInterpreter;
    public boolean treatAllEc0lintIssuesAsWarnings;
    public boolean pluginEnabled;
    public boolean autoFix;
    public boolean reportUnused;
    public String ext = "";

    protected Project project;

    public static Settings getInstance(Project project) {
        Settings settings = ServiceManager.getService(project, Settings.class);
        settings.project = project;
        return settings;
    }

    @Nullable
    @Override
    public Settings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull Settings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public String getVersion() {
        return nodeInterpreter + ec0lintExecutable + ec0lintRcFile + rulesPath + builtinRulesPath + ext + autoFix + reportUnused;
    }
}
