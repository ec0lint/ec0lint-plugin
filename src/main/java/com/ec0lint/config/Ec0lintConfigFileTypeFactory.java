package com.ec0lint.config;

import com.intellij.openapi.fileTypes.ExactFileNameMatcher;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

public class Ec0lintConfigFileTypeFactory extends FileTypeFactory {
    public void createFileTypes(@NotNull FileTypeConsumer consumer) {
        consumer.consume(Ec0lintConfigFileType.INSTANCE, new ExactFileNameMatcher(Ec0lintConfigFileType.EC0LINTRC));
//                new ExtensionFileNameMatcher(Ec0lintConfigFileType.EC0LINTRC), new ExactFileNameMatcher("ec0lint.json"));
    }
}