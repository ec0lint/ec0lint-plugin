package com.ec0lint.utils;

import com.ec0lint.Ec0lintProjectComponent;
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

public final class Ec0lintRunner {
    private Ec0lintRunner() {
    }

    private static final Logger LOG = Logger.getInstance(Ec0lintRunner.class);

    private static final int TIME_OUT = (int) TimeUnit.SECONDS.toMillis(120L);

    public static class Ec0lintSettings {
        public String node;
        public String ec0lintExecutablePath;
        public String rules;
        public String config;
        public String cwd;
        public String targetFile;
        public String ext;
        public boolean fix;
        public boolean reportUnused;
    }

    public static Ec0lintSettings buildSettings(@NotNull String cwd, @NotNull String path, @NotNull Ec0lintProjectComponent component) {
        return buildSettings(cwd, path, component.nodeInterpreter, component.ec0lintExecutable, component.ec0lintRcFile, component.customRulesPath, component.ext, component.autoFix, component.reportUnused);
    }

    private static Ec0lintSettings buildSettings(@NotNull String cwd, @NotNull String path, @NotNull String nodeInterpreter, @NotNull String ec0lintBin, @Nullable String ec0lintrc,
                                                @Nullable String rulesdir, @Nullable String ext, boolean autoFix, boolean reportUnused) {
        Ec0lintSettings settings = new Ec0lintSettings();
        settings.cwd = cwd;
        settings.ec0lintExecutablePath = ec0lintBin;
        settings.node = nodeInterpreter;
        settings.rules = rulesdir;
        settings.config = ec0lintrc;
        settings.targetFile = path;
        settings.ext = ext;
        settings.fix = autoFix;
        settings.reportUnused = reportUnused;
        return settings;
    }

    @NotNull
    public static ProcessOutput lint(@NotNull Ec0lintSettings settings) throws ExecutionException {
        GeneralCommandLine commandLine = CliBuilder.createLint(settings);
        return NodeRunner.execute(commandLine, TIME_OUT);
    }

    @NotNull
    public static Result lint(@NotNull String cwd, @NotNull String path, @NotNull Ec0lintProjectComponent component) {
        Ec0lintSettings settings = Ec0lintRunner.buildSettings(cwd, path, component);
        try {
            ProcessOutput output = Ec0lintRunner.lint(settings);
            return Result.processResults(output);
        } catch (ExecutionException e) {
            LOG.warn("Could not lint file", e);
            Ec0lintProjectComponent.showNotification("Error running Ec0lint inspection: " + e.getMessage() + "\ncwd: " + cwd + "\ncommand: " + component.ec0lintExecutable, NotificationType.WARNING);
            e.printStackTrace();
            return Result.createError(e.getMessage());
        }
    }

//    @NotNull
//    public static Result lint(@NotNull String cwd, @NotNull String path, @NotNull String nodeInterpreter, @NotNull String ec0lintBin, @Nullable String ec0lintrc, @Nullable String rulesdir, @Nullable String ext, boolean autoFix) {
//        Ec0lintRunner.Ec0lintSettings settings = Ec0lintRunner.buildSettings(cwd, path, nodeInterpreter, ec0lintBin, ec0lintrc, rulesdir, ext, autoFix);
//        try {
//            ProcessOutput output = Ec0lintRunner.lint(settings);
//            return Result.processResults(output);
//        } catch (ExecutionException e) {
//            LOG.warn("Could not lint file", e);
//            Ec0lintProjectComponent.showNotification("Error running Ec0lint inspection: " + e.getMessage() + "\ncwd: " + cwd + "\ncommand: " + ec0lintBin, NotificationType.WARNING);
//            e.printStackTrace();
//            return Result.createError(e.getMessage());
//        }
//    }

    @NotNull
    public static ProcessOutput fix(@NotNull Ec0lintSettings settings) throws ExecutionException {
        GeneralCommandLine commandLine = CliBuilder.createFix(settings);
        return NodeRunner.execute(commandLine, TIME_OUT);
    }

    @NotNull
    private static ProcessOutput version(@NotNull Ec0lintSettings settings) throws ExecutionException {
        GeneralCommandLine commandLine = CliBuilder.createVersion(settings);
        return NodeRunner.execute(commandLine, TIME_OUT);
    }

    @NotNull
    public static String runVersion(@NotNull Ec0lintSettings settings) throws ExecutionException {
        if (!new File(settings.ec0lintExecutablePath).exists()) {
            LOG.warn("Calling version with invalid ec0lint exe " + settings.ec0lintExecutablePath);
            return "";
        }
        ProcessOutput out = version(settings);
        if (out.getExitCode() == 0) {
            return out.getStdout().trim();
        }
        return "";
    }
}