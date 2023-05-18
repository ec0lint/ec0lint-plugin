package com.ec0lint;

import com.ec0lint.config.schema.RuleCache;
import com.ec0lint.settings.Settings;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.wix.utils.FileUtils;
import com.wix.utils.FileUtils.ValidationStatus;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;

public class Ec0lintProjectComponent implements ProjectComponent {
    public static final String FIX_CONFIG_HREF = "\n<a href=\"#\">Fix Configuration</a>";
    protected Project project;
    protected Settings settings;
    protected boolean settingValidStatus;
    protected String settingValidVersion;
    protected String settingVersionLastShowNotification;

    private static final Logger LOG = Logger.getInstance(Ec0lintBundle.LOG_ID);

    public String ec0lintRcFile;
    public String customRulesPath;
    public String ext;
    public String rulesPath;
    public String ec0lintExecutable;
    public String nodeInterpreter;
    public boolean treatAsWarnings;
    public boolean pluginEnabled;
    public boolean autoFix;
    public boolean reportUnused;

    public static final String PLUGIN_NAME = "Ec0lint plugin";

    public Ec0lintProjectComponent(Project project) {
        this.project = project;
        settings = Settings.getInstance(project);
    }

    @Override
    public void projectOpened() {
        if (isEnabled()) {
            isSettingsValid();
        }
    }

    @Override
    public void projectClosed() {
    }

    @Override
    public void initComponent() {
        if (isEnabled()) {
            isSettingsValid();
        }
    }

    @Override
    public void disposeComponent() {
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "Ec0lintProjectComponent";
    }

    public boolean isEnabled() {
        return Settings.getInstance(project).pluginEnabled;
    }

    public boolean isSettingsValid() {
        if (!settings.getVersion().equals(settingValidVersion)) {
            validateSettings();
            settingValidVersion = settings.getVersion();
        }
        return settingValidStatus;
    }

    public boolean validateSettings() {
        // do not validate if disabled
        if (!settings.pluginEnabled) {
            return true;
        }
        boolean status = validateField("Node Interpreter", settings.nodeInterpreter, true, false, true);
        if (!status) {
            return false;
        }
        status = validateField("Rules", settings.rulesPath, false, true, false);
        if (!status) {
            return false;
        }
        status = validateField("Ec0lint bin", settings.ec0lintExecutable, false, false, true);
        if (!status) {
            return false;
        }
        status = validateField("Builtin rules", settings.builtinRulesPath, false, true, false);
        if (!status) {
            return false;
        }

//        if (StringUtil.isNotEmpty(settings.ec0lintExecutable)) {
//            File file = new File(project.getBasePath(), settings.ec0lintExecutable);
//            if (!file.exists()) {
//                showErrorConfigNotification(Ec0lintBundle.message("ec0lint.rules.dir.does.not.exist", file.toString()));
//                LOG.debug("Rules directory not found");
//                settingValidStatus = false;
//                return false;
//            }
//        }
        ec0lintExecutable = settings.ec0lintExecutable;
        ec0lintRcFile = settings.ec0lintRcFile;
        customRulesPath = settings.rulesPath;
        rulesPath = settings.builtinRulesPath;
        nodeInterpreter = settings.nodeInterpreter;
        treatAsWarnings = settings.treatAllEc0lintIssuesAsWarnings;
        pluginEnabled = settings.pluginEnabled;
        ext = settings.ext;
        autoFix = settings.autoFix;
        reportUnused = settings.reportUnused;

        RuleCache.initializeFromPath(project, this);

        settingValidStatus = true;
        return true;
    }

    private boolean validateField(String fieldName, String value, boolean shouldBeAbsolute, boolean allowEmpty, boolean isFile) {
        ValidationStatus r = FileUtils.validateProjectPath(shouldBeAbsolute ? null : project, value, allowEmpty, isFile);
        if (isFile) {
            if (r == ValidationStatus.NOT_A_FILE) {
                String msg = Ec0lintBundle.message("ec0lint.file.is.not.a.file", fieldName, value);
                validationFailed(msg);
                return false;
            }
        } else {
            if (r == ValidationStatus.NOT_A_DIRECTORY) {
                String msg = Ec0lintBundle.message("ec0lint.directory.is.not.a.dir", fieldName, value);
                validationFailed(msg);
                return false;
            }
        }
        if (r == ValidationStatus.DOES_NOT_EXIST) {
            String msg = Ec0lintBundle.message("ec0lint.file.does.not.exist", fieldName, value);
            validationFailed(msg);
            return false;
        }
        return true;
    }

    private void validationFailed(String msg) {
        NotificationListener notificationListener = new NotificationListener() {
            @Override
            public void hyperlinkUpdate(@NotNull Notification notification, @NotNull HyperlinkEvent event) {
                Ec0lintInspection.showSettings(project);
            }
        };
        String errorMessage = msg + FIX_CONFIG_HREF;
        showInfoNotification(errorMessage, NotificationType.WARNING, notificationListener);
        LOG.debug(msg);
        settingValidStatus = false;
    }

    protected void showErrorConfigNotification(String content) {
        if (!settings.getVersion().equals(settingVersionLastShowNotification)) {
            settingVersionLastShowNotification = settings.getVersion();
            showInfoNotification(content, NotificationType.WARNING);
        }
    }

    public void showInfoNotification(String content, NotificationType type) {
        Notification errorNotification = new Notification(PLUGIN_NAME, PLUGIN_NAME, content, type);
        Notifications.Bus.notify(errorNotification, this.project);
    }

    public void showInfoNotification(String content, NotificationType type, NotificationListener notificationListener) {
        Notification errorNotification = new Notification(PLUGIN_NAME, PLUGIN_NAME, content, type, notificationListener);
        Notifications.Bus.notify(errorNotification, this.project);
    }

    public static void showNotification(String content, NotificationType type) {
        Notification errorNotification = new Notification(PLUGIN_NAME, PLUGIN_NAME, content, type);
        Notifications.Bus.notify(errorNotification);
    }
}
