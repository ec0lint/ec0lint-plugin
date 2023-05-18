package com.wix.settings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.text.JTextComponent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by idok on 3/8/15.
 */
public class Validator {
    public final List<ValidationInfo> errors = new ArrayList<ValidationInfo>();

    public void add(@Nullable JTextComponent textComponent, @NotNull String errorHtmlDescriptionTemplate, @NotNull String linkText) {
        ValidationInfo error = new ValidationInfo(textComponent, errorHtmlDescriptionTemplate, linkText);
        errors.add(error);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean isEmpty() {
        return errors.isEmpty();
    }
}