package com.ec0lint.config;

import com.intellij.json.JsonLanguage;
//import com.intellij.lang.javascript.json.JSONLanguageDialect;
import com.intellij.openapi.fileTypes.LanguageFileType;

import javax.swing.Icon;

import icons.Ec0lintIcons;
import org.jetbrains.annotations.NotNull;

public class Ec0lintConfigFileType extends LanguageFileType {
    public static final Ec0lintConfigFileType INSTANCE = new Ec0lintConfigFileType();
    public static final String EC0LINTRC_EXT = "ec0lintrc";
    public static final String EC0LINTRC = '.' + EC0LINTRC_EXT;
    public static final String[] EC0LINTRC_FILES = {EC0LINTRC, EC0LINTRC + ".js", EC0LINTRC + ".yml", EC0LINTRC + ".yaml", EC0LINTRC + ".json"};

    private Ec0lintConfigFileType() {
        super(JsonLanguage.INSTANCE); //JSONLanguageDialect.JSON
    }

    @NotNull
    public String getName() {
        return "Ec0lint";
    }

    @NotNull
    public String getDescription() {
        return "Ec0lint configuration file";
    }

    @NotNull
    public String getDefaultExtension() {
        return EC0LINTRC_EXT;
    }

    @NotNull
    public Icon getIcon() {
        return Ec0lintIcons.Ec0lint;
    }
}