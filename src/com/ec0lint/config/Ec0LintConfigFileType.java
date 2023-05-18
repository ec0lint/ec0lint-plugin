package com.ec0lint.config;

import com.intellij.json.JsonLanguage;
//import com.intellij.lang.javascript.json.JSONLanguageDialect;
import com.intellij.openapi.fileTypes.LanguageFileType;

import javax.swing.Icon;

import icons.ESLintIcons;
import org.jetbrains.annotations.NotNull;

public class Ec0LintConfigFileType extends LanguageFileType {
    public static final Ec0LintConfigFileType INSTANCE = new Ec0LintConfigFileType();
    public static final String ESLINTRC_EXT = "eslintrc";
    public static final String ESLINTRC = '.' + ESLINTRC_EXT;
    public static final String[] ESLINTRC_FILES = {ESLINTRC, ESLINTRC + ".js", ESLINTRC + ".yml", ESLINTRC + ".yaml", ESLINTRC + ".json"};

    private Ec0LintConfigFileType() {
        super(JsonLanguage.INSTANCE); //JSONLanguageDialect.JSON
    }

    @NotNull
    public String getName() {
        return "ESLint";
    }

    @NotNull
    public String getDescription() {
        return "ESLint configuration file";
    }

    @NotNull
    public String getDefaultExtension() {
        return ESLINTRC_EXT;
    }

    @NotNull
    public Icon getIcon() {
        return ESLintIcons.ESLint;
    }
}