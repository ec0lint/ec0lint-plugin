package com.ec0lint.config;

import com.intellij.openapi.fileTypes.ExactFileNameMatcher;
import com.intellij.openapi.fileTypes.ExtensionFileNameMatcher;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

public class Ec0LintConfigFileTypeFactory extends FileTypeFactory {
    public void createFileTypes(@NotNull FileTypeConsumer consumer) {
        consumer.consume(Ec0LintConfigFileType.INSTANCE, new ExactFileNameMatcher(Ec0LintConfigFileType.ESLINTRC));
//                new ExtensionFileNameMatcher(ESLintConfigFileType.ESLINTRC), new ExactFileNameMatcher("eslint.json"));
    }
}