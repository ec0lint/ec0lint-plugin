package com.ec0lint.settings;

import com.ec0lint.Ec0lintProjectComponent;
import com.ec0lint.utils.Ec0lintFinder;
import com.ec0lint.utils.Ec0lintRunner;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.execution.ExecutionException;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.TextFieldWithHistory;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.util.Function;
import com.intellij.util.NotNullProducer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.UIUtil;
import com.intellij.webcore.packaging.PackagesNotificationPanel;
import com.intellij.webcore.ui.SwingHelper;
import com.wix.utils.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ec0lintSettingsPage implements Configurable {
    public static final String FIX_IT = "Fix it";
    public static final String HOW_TO_USE_EC0LINT = "How to Use Ec0lint";
    public static final String HOW_TO_USE_LINK = "https://github.com/idok/ec0lint-plugin";
    protected Project project;

    private JCheckBox pluginEnabledCheckbox;
    private JTextField customRulesPathField;
    private JPanel panel;
    private JPanel errorPanel;
    private TextFieldWithHistoryWithBrowseButton ec0lintBinField2;
    private TextFieldWithHistoryWithBrowseButton nodeInterpreterField;
    private TextFieldWithHistoryWithBrowseButton ec0lintrcFile;
    private JRadioButton searchForEc0lintrcInRadioButton;
    private JRadioButton useProjectEc0lintrcRadioButton;
    private HyperlinkLabel usageLink;
    private JLabel Ec0lintConfigFilePathLabel;
    private JLabel rulesDirectoryLabel;
    private JLabel pathToEc0lintBinLabel;
    private JLabel nodeInterpreterLabel;
    private JCheckBox treatAllEc0lintIssuesCheckBox;
    private JLabel versionLabel;
    private TextFieldWithHistoryWithBrowseButton rulesPathField;
    private JLabel rulesDirectoryLabel1;
    private final PackagesNotificationPanel packagesNotificationPanel = new PackagesNotificationPanel();

    public Ec0lintSettingsPage(@NotNull final Project project) {
        this.project = project;
        configEc0lintBinField();
        configEc0lintRcField();
        configEc0lintRulesField();
        configNodeField();
//        searchForEc0lintrcInRadioButton.addItemListener(new ItemListener() {
//            public void itemStateChanged(ItemEvent e) {
//                ec0lintrcFile.setEnabled(e.getStateChange() == ItemEvent.DESELECTED);
//                System.out.println("searchForEc0lintrcInRadioButton: " + (e.getStateChange() == ItemEvent.SELECTED ? "checked" : "unchecked"));
//            }
//        });
        useProjectEc0lintrcRadioButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                ec0lintrcFile.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
//                System.out.println("useProjectEc0lintrcRadioButton: " + (e.getStateChange() == ItemEvent.SELECTED ? "checked" : "unchecked"));
            }
        });
        pluginEnabledCheckbox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                boolean enabled = e.getStateChange() == ItemEvent.SELECTED;
                setEnabledState(enabled);
            }
        });

//        this.packagesNotificationPanel = new PackagesNotificationPanel(project);
//        GridConstraints gridConstraints = new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
//                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
//                null, new Dimension(250, 150), null);
//        errorPanel.add(this.packagesNotificationPanel.getComponent(), BorderLayout.CENTER);

        DocumentAdapter docAdp = new DocumentAdapter() {
            protected void textChanged(DocumentEvent e) {
                updateLaterInEDT();
            }
        };
        ec0lintBinField2.getChildComponent().getTextEditor().getDocument().addDocumentListener(docAdp);
        ec0lintrcFile.getChildComponent().getTextEditor().getDocument().addDocumentListener(docAdp);
        nodeInterpreterField.getChildComponent().getTextEditor().getDocument().addDocumentListener(docAdp);
        rulesPathField.getChildComponent().getTextEditor().getDocument().addDocumentListener(docAdp);
        customRulesPathField.getDocument().addDocumentListener(docAdp);
    }

    private File getProjectPath() {
        return new File(project.getBaseDir().getPath());
    }

    private void updateLaterInEDT() {
        UIUtil.invokeLaterIfNeeded(new Runnable() {
            public void run() {
                Ec0lintSettingsPage.this.update();
            }
        });
    }

    private void update() {
        ApplicationManager.getApplication().assertIsDispatchThread();
        validate();
    }

    private void setEnabledState(boolean enabled) {
        ec0lintrcFile.setEnabled(enabled);
        customRulesPathField.setEnabled(enabled);
        rulesPathField.setEnabled(enabled);
        searchForEc0lintrcInRadioButton.setEnabled(enabled);
        useProjectEc0lintrcRadioButton.setEnabled(enabled);
        ec0lintBinField2.setEnabled(enabled);
        nodeInterpreterField.setEnabled(enabled);
        Ec0lintConfigFilePathLabel.setEnabled(enabled);
        rulesDirectoryLabel.setEnabled(enabled);
        rulesDirectoryLabel1.setEnabled(enabled);
        pathToEc0lintBinLabel.setEnabled(enabled);
        nodeInterpreterLabel.setEnabled(enabled);
        treatAllEc0lintIssuesCheckBox.setEnabled(enabled);
    }

    private void validateField(List<Ec0lintValidationInfo> errors, TextFieldWithHistoryWithBrowseButton field, boolean allowEmpty, String message) {
        if (!validatePath(field.getChildComponent().getText(), allowEmpty)) {
            Ec0lintValidationInfo error = new Ec0lintValidationInfo(field.getChildComponent().getTextEditor(), message, FIX_IT);
            errors.add(error);
        }
    }

    private void validate() {
        if (!pluginEnabledCheckbox.isSelected()) {
            return;
        }
        List<Ec0lintValidationInfo> errors = new ArrayList<Ec0lintValidationInfo>();
        validateField(errors, ec0lintBinField2, false, "Path to ec0lint is invalid {{LINK}}");
        validateField(errors, ec0lintrcFile, true, "Path to ec0lintrc is invalid {{LINK}}"); //Please correct path to
        validateField(errors, nodeInterpreterField, false, "Path to node interpreter is invalid {{LINK}}");
        if (!validateDirectory(customRulesPathField.getText(), true)) {
            Ec0lintValidationInfo error = new Ec0lintValidationInfo(customRulesPathField, "Path to custom rules is invalid {{LINK}}", FIX_IT);
            errors.add(error);
        }
        if (!validateDirectory(rulesPathField.getChildComponent().getText(), true)) {
            Ec0lintValidationInfo error = new Ec0lintValidationInfo(rulesPathField.getChildComponent().getTextEditor(), "Path to rules is invalid {{LINK}}", FIX_IT);
            errors.add(error);
        }
        if (errors.isEmpty()) {
            try {
                packagesNotificationPanel.removeAllLinkHandlers();
            } catch (Exception e) {
                e.printStackTrace();
            }
            packagesNotificationPanel.hide();
            getVersion();
        } else {
            showErrors(errors);
        }
    }

    private Ec0lintRunner.Ec0lintSettings settings;

    private void getVersion() {
        if (settings != null &&
                areEqual(nodeInterpreterField, settings.node) &&
                areEqual(ec0lintBinField2, settings.ec0lintExecutablePath) &&
                settings.cwd.equals(project.getBasePath())
        ) {
            return;
        }
        settings = new Ec0lintRunner.Ec0lintSettings();
        settings.node = nodeInterpreterField.getChildComponent().getText();
        settings.ec0lintExecutablePath = ec0lintBinField2.getChildComponent().getText();
        settings.cwd = project.getBasePath();
        try {
            String version = Ec0lintRunner.runVersion(settings);
            versionLabel.setText(version.trim());
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private boolean validatePath(String path, boolean allowEmpty) {
        if (StringUtils.isEmpty(path)) {
            return allowEmpty;
        }
        File filePath = new File(path);
        if (filePath.isAbsolute()) {
            if (!filePath.exists() || !filePath.isFile()) {
                return false;
            }
        } else {
            VirtualFile child = project.getBaseDir().findFileByRelativePath(path);
            if (child == null || !child.exists() || child.isDirectory()) {
                return false;
            }
        }
        return true;
    }

    private boolean validateDirectory(String path, boolean allowEmpty) {
        if (StringUtils.isEmpty(path)) {
            return allowEmpty;
        }
        File filePath = new File(path);
        if (filePath.isAbsolute()) {
            if (!filePath.exists() || !filePath.isDirectory()) {
                return false;
            }
        } else {
            VirtualFile child = project.getBaseDir().findFileByRelativePath(path);
            if (child == null || !child.exists() || !child.isDirectory()) {
                return false;
            }
        }
        return true;
    }

    private static TextFieldWithHistory configWithDefaults(TextFieldWithHistoryWithBrowseButton field) {
        TextFieldWithHistory textFieldWithHistory = field.getChildComponent();
        textFieldWithHistory.setHistorySize(-1);
        textFieldWithHistory.setMinimumAndPreferredWidth(0);
        return textFieldWithHistory;
    }

    private void configEc0lintBinField() {
        configWithDefaults(ec0lintBinField2);
        SwingHelper.addHistoryOnExpansion(ec0lintBinField2.getChildComponent(), new NotNullProducer<List<String>>() {
            @NotNull
            public List<String> produce() {
                List<File> newFiles = Ec0lintFinder.searchForEc0lintBin(getProjectPath());
                return FileUtils.toAbsolutePath(newFiles);
            }
        });
        SwingHelper.installFileCompletionAndBrowseDialog(project, ec0lintBinField2, "Select Ec0lint.js cli", FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());
    }

    private void configEc0lintRulesField() {
        TextFieldWithHistory textFieldWithHistory = rulesPathField.getChildComponent();
        SwingHelper.addHistoryOnExpansion(textFieldWithHistory, new NotNullProducer<List<String>>() {
            @NotNull
            public List<String> produce() {
                return Ec0lintFinder.tryFindRulesAsString(getProjectPath());
            }
        });
        SwingHelper.installFileCompletionAndBrowseDialog(project, rulesPathField, "Select Built in rules", FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());
    }

    private void configEc0lintRcField() {
        TextFieldWithHistory textFieldWithHistory = configWithDefaults(ec0lintrcFile);
        SwingHelper.addHistoryOnExpansion(textFieldWithHistory, new NotNullProducer<List<String>>() {
            @NotNull
            public List<String> produce() {
                return Ec0lintFinder.searchForEc0lintRCFiles(getProjectPath());
            }
        });
        SwingHelper.installFileCompletionAndBrowseDialog(project, ec0lintrcFile, "Select Ec0lint config", FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());
    }

    private void configNodeField() {
        TextFieldWithHistory textFieldWithHistory = configWithDefaults(nodeInterpreterField);
        SwingHelper.addHistoryOnExpansion(textFieldWithHistory, new NotNullProducer<List<String>>() {
            @NotNull
            public List<String> produce() {
                List<File> newFiles = Collections.emptyList();
                return FileUtils.toAbsolutePath(newFiles);
            }
        });
        SwingHelper.installFileCompletionAndBrowseDialog(project, nodeInterpreterField, "Select Node interpreter", FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Ec0lint Plugin";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        loadSettings();
        return panel;
    }

    private static boolean areEqual(TextFieldWithHistoryWithBrowseButton field, String value) {
        return field.getChildComponent().getText().equals(value);
    }

    @Override
    public boolean isModified() {
        Settings s = getSettings();
        return pluginEnabledCheckbox.isSelected() != s.pluginEnabled ||
                !areEqual(ec0lintBinField2, s.ec0lintExecutable) ||
                !areEqual(nodeInterpreterField, s.nodeInterpreter) ||
                treatAllEc0lintIssuesCheckBox.isSelected() != s.treatAllEc0lintIssuesAsWarnings ||
                !customRulesPathField.getText().equals(s.rulesPath) ||
                !areEqual(rulesPathField, s.builtinRulesPath) ||
                !getEc0lintRCFile().equals(s.ec0lintRcFile);
    }

    private String getEc0lintRCFile() {
        return useProjectEc0lintrcRadioButton.isSelected() ? ec0lintrcFile.getChildComponent().getText() : "";
    }

    @Override
    public void apply() throws ConfigurationException {
        saveSettings();
        PsiManager.getInstance(project).dropResolveCaches();
    }

    protected void saveSettings() {
        Settings settings = getSettings();
        settings.pluginEnabled = pluginEnabledCheckbox.isSelected();
        settings.ec0lintExecutable = ec0lintBinField2.getChildComponent().getText();
        settings.nodeInterpreter = nodeInterpreterField.getChildComponent().getText();
        settings.ec0lintRcFile = getEc0lintRCFile();
        settings.rulesPath = customRulesPathField.getText();
        settings.builtinRulesPath = rulesPathField.getChildComponent().getText();
        settings.treatAllEc0lintIssuesAsWarnings = treatAllEc0lintIssuesCheckBox.isSelected();
        project.getComponent(Ec0lintProjectComponent.class).validateSettings();
        DaemonCodeAnalyzer.getInstance(project).restart();
    }

    protected void loadSettings() {
        Settings settings = getSettings();
        pluginEnabledCheckbox.setSelected(settings.pluginEnabled);
        ec0lintBinField2.getChildComponent().setText(settings.ec0lintExecutable);
        ec0lintrcFile.getChildComponent().setText(settings.ec0lintRcFile);
        nodeInterpreterField.getChildComponent().setText(settings.nodeInterpreter);
        customRulesPathField.setText(settings.rulesPath);
        rulesPathField.getChildComponent().setText(settings.builtinRulesPath);
        useProjectEc0lintrcRadioButton.setSelected(StringUtils.isNotEmpty(settings.ec0lintRcFile));
        searchForEc0lintrcInRadioButton.setSelected(StringUtils.isEmpty(settings.ec0lintRcFile));
        ec0lintrcFile.setEnabled(useProjectEc0lintrcRadioButton.isSelected());
        treatAllEc0lintIssuesCheckBox.setSelected(settings.treatAllEc0lintIssuesAsWarnings);
        setEnabledState(settings.pluginEnabled);
    }

    @Override
    public void reset() {
        loadSettings();
    }

    @Override
    public void disposeUIResources() {
    }

    protected Settings getSettings() {
        return Settings.getInstance(project);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        usageLink = SwingHelper.createWebHyperlink(HOW_TO_USE_EC0LINT, HOW_TO_USE_LINK);
    }

    private void showErrors(@NotNull List<Ec0lintValidationInfo> errors) {
        List<String> errorHtmlDescriptions = ContainerUtil.map(errors, new Function<Ec0lintValidationInfo, String>() {
            public String fun(Ec0lintValidationInfo info) {
                return info.getErrorHtmlDescription();
            }
        });
        String styleTag = UIUtil.getCssFontDeclaration(UIUtil.getLabelFont());
        String html = "<html>" + styleTag + "<body><div style='padding-left:4px;'>" + StringUtil.join(errorHtmlDescriptions, "<div style='padding-top:2px;'/>") + "</div></body></html>";

        for (Ec0lintValidationInfo error : errors) {
            String linkText = error.getLinkText();
            final JTextComponent component = error.getTextComponent();
            if (linkText != null && component != null) {
                this.packagesNotificationPanel.addLinkHandler(linkText, new Runnable() {
                    public void run() {
                        component.requestFocus();
                    }
                });
            }
        }
        this.packagesNotificationPanel.showError(html, null, null);
    }
}