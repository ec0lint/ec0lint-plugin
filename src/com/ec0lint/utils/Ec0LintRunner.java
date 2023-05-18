package com.ec0lint.utils;

import com.ec0lint.Ec0LintProjectComponent;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.wix.nodejs.NodeRunner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.concurrent.TimeUnit;

public final class Ec0LintRunner {
    private Ec0LintRunner() {
    }

    private static final Logger LOG = Logger.getInstance(Ec0LintRunner.class);

    private static final int TIME_OUT = (int) TimeUnit.SECONDS.toMillis(120L);

    public static class ESLintSettings {
        public String node;
        public String eslintExecutablePath;
        public String rules;
        public String config;
        public String cwd;
        public String targetFile;
        public String ext;
        public boolean fix;
        public boolean reportUnused;
    }

    public static ESLintSettings buildSettings(@NotNull String cwd, @NotNull String path, @NotNull Ec0LintProjectComponent component) {
        return buildSettings(cwd, path, component.nodeInterpreter, component.eslintExecutable, component.eslintRcFile, component.customRulesPath, component.ext, component.autoFix, component.reportUnused);
    }

    private static ESLintSettings buildSettings(@NotNull String cwd, @NotNull String path, @NotNull String nodeInterpreter, @NotNull String eslintBin, @Nullable String eslintrc,
                                                @Nullable String rulesdir, @Nullable String ext, boolean autoFix, boolean reportUnused) {
        Ec0LintRunner.ESLintSettings settings = new Ec0LintRunner.ESLintSettings();
        settings.cwd = cwd;
        settings.eslintExecutablePath = eslintBin;
        settings.node = nodeInterpreter;
        settings.rules = rulesdir;
        settings.config = eslintrc;
        settings.targetFile = path;
        settings.ext = ext;
        settings.fix = autoFix;
        settings.reportUnused = reportUnused;
        return settings;
    }

    @NotNull
    public static ProcessOutput lint(@NotNull ESLintSettings settings) throws ExecutionException {
        GeneralCommandLine commandLine = CliBuilder.createLint(settings);
        return NodeRunner.execute(commandLine, TIME_OUT);
    }

    @NotNull
    public static Result lint(@NotNull String cwd, @NotNull String path, @NotNull Ec0LintProjectComponent component) {
        Ec0LintRunner.ESLintSettings settings = Ec0LintRunner.buildSettings(cwd, path, component);
        try {
            ProcessOutput output = Ec0LintRunner.lint(settings);
            return Result.processResults(output);
        } catch (ExecutionException e) {
            LOG.warn("Could not lint file", e);
            Ec0LintProjectComponent.showNotification("Error running ESLint inspection: " + e.getMessage() + "\ncwd: " + cwd + "\ncommand: " + component.eslintExecutable, NotificationType.WARNING);
            e.printStackTrace();
            return Result.createError(e.getMessage());
        }
    }

//    @NotNull
//    public static Result lint(@NotNull String cwd, @NotNull String path, @NotNull String nodeInterpreter, @NotNull String eslintBin, @Nullable String eslintrc, @Nullable String rulesdir, @Nullable String ext, boolean autoFix) {
//        ESLintRunner.ESLintSettings settings = ESLintRunner.buildSettings(cwd, path, nodeInterpreter, eslintBin, eslintrc, rulesdir, ext, autoFix);
//        try {
//            ProcessOutput output = ESLintRunner.lint(settings);
//            return Result.processResults(output);
//        } catch (ExecutionException e) {
//            LOG.warn("Could not lint file", e);
//            ESLintProjectComponent.showNotification("Error running ESLint inspection: " + e.getMessage() + "\ncwd: " + cwd + "\ncommand: " + eslintBin, NotificationType.WARNING);
//            e.printStackTrace();
//            return Result.createError(e.getMessage());
//        }
//    }

    @NotNull
    public static ProcessOutput fix(@NotNull ESLintSettings settings) throws ExecutionException {
        GeneralCommandLine commandLine = CliBuilder.createFix(settings);
        return NodeRunner.execute(commandLine, TIME_OUT);
    }

    @NotNull
    private static ProcessOutput version(@NotNull ESLintSettings settings) throws ExecutionException {
        GeneralCommandLine commandLine = CliBuilder.createVersion(settings);
        return NodeRunner.execute(commandLine, TIME_OUT);
    }

    @NotNull
    public static String runVersion(@NotNull ESLintSettings settings) throws ExecutionException {
        if (!new File(settings.eslintExecutablePath).exists()) {
            LOG.warn("Calling version with invalid eslint exe " + settings.eslintExecutablePath);
            return "";
        }
        ProcessOutput out = version(settings);
        if (out.getExitCode() == 0) {
            return out.getStdout().trim();
        }
        return "";
    }
}