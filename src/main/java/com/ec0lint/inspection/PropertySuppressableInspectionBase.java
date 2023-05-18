package com.ec0lint.inspection;

import com.ec0lint.Ec0lintBundle;
import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInsight.daemon.impl.actions.SuppressByCommentFix;
import com.intellij.codeInspection.CustomSuppressableInspectionTool;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.SuppressIntentionAction;
import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.lang.javascript.inspections.JSInspectionSuppressor;
import com.intellij.lang.javascript.linter.jshint.JSHintInspection;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.impl.JSFileImpl;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PropertySuppressableInspectionBase extends LocalInspectionTool { //implements CustomSuppressableInspectionTool {
    private static final Logger LOG = Logger.getInstance("#com.intellij.lang.properties.PropertySuppressableInspectionBase");

    @NotNull
    public String getGroupDisplayName() {
        return Ec0lintBundle.message("ec0lint.inspection.group.name");
    }

    public SuppressIntentionAction[] getSuppressActions(final PsiElement element) {
        PsiNamedElement pe = getProblemElement(element);
        return new SuppressIntentionAction[]{new SuppressForStatement(getShortName()), new SuppressForFile(getShortName())};
    }

    @NotNull
    public SuppressQuickFix[] getBatchSuppressActions(@Nullable PsiElement element) {
        return new SuppressQuickFix[]{new Ec0lintSuppressByCommentFix(HighlightDisplayKey.find(this.getShortName()), JSInspectionSuppressor.getHolderClass(element))};
    }

    public static class Ec0lintSuppressByCommentFix extends SuppressByCommentFix {
        public Ec0lintSuppressByCommentFix(HighlightDisplayKey key, Class<? extends PsiElement> suppressionHolderClass) {
            super(key, suppressionHolderClass);
        }

        @NotNull
        public String getText() {
            return "Suppress for line";
        }

        protected void createSuppression(@NotNull Project project, @NotNull PsiElement element, @NotNull PsiElement container) throws IncorrectOperationException {
            if (element.isValid()) {
                PsiFile psiFile = element.getContainingFile();
                if (psiFile != null) {
                    psiFile = psiFile.getOriginalFile();
                }

                if (psiFile != null && psiFile.isValid()) {
                    final Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
                    if (document != null) {
                        int lineNo = document.getLineNumber(element.getTextOffset());
                        final int lineEndOffset = document.getLineEndOffset(lineNo);
                        CommandProcessor.getInstance().executeCommand(project, new Runnable() {
                            public void run() {
                                document.insertString(lineEndOffset, " //ec0lint-disable-line");
                            }
                        }, null, null);
                    }
                }
            }
        }
    }


    public boolean isSuppressedFor(@NotNull PsiElement element) {
//        Property property = PsiTreeUtil.getParentOfType(element, Property.class, false);
//        JSFileImpl file;
//        if (property == null) {
//            PsiFile containingFile = element.getContainingFile();
//            if (containingFile instanceof JSFileImpl) {
//                file = (JSFileImpl) containingFile;
//            } else {
//                return false;
//            }
//        } else {
//            PsiElement prev = property.getPrevSibling();
//            while (prev instanceof PsiWhiteSpace || prev instanceof PsiComment) {
//                if (prev instanceof PsiComment) {
//                    @NonNls String text = prev.getText();
//                    if (text.contains("suppress") && text.contains('"' + getShortName() + '"')) return true;
//                }
//                prev = prev.getPrevSibling();
//            }
//            file = property.getPropertiesFile();
//        }
//        PsiElement leaf = file.getContainingFile().findElementAt(0);
//        while (leaf instanceof PsiWhiteSpace) leaf = leaf.getNextSibling();
//
//        while (leaf instanceof PsiComment) {
//            @NonNls String text = leaf.getText();
//            if (text.contains("suppress") && text.contains('"' + getShortName() + '"') && text.contains("file")) {
//                return true;
//            }
//            leaf = leaf.getNextSibling();
//            if (leaf instanceof PsiWhiteSpace) leaf = leaf.getNextSibling();
//            // comment before first property get bound to the file, not property
//            if (leaf instanceof PropertiesList && leaf.getFirstChild() == property && text.contains("suppress") && text.contains("\"" + getShortName() + "\"")) {
//                return true;
//            }
//        }

        return false;
    }

    private static class SuppressForStatement extends SuppressIntentionAction {
        private final String rule;

        public SuppressForStatement(String rule) {
            this.rule = rule;
        }

        @NotNull
        public String getText() {
            return Ec0lintBundle.message("unused.property.suppress.for.statement");
        }

        @NotNull
        public String getFamilyName() {
            return Ec0lintBundle.message("unused.property.suppress.for.statement");
        }

        public boolean isAvailable(@NotNull final Project project, final Editor editor, @NotNull final PsiElement element) {
            final JSElement property = PsiTreeUtil.getParentOfType(element, JSElement.class);
            return property != null && property.isValid();
        }

        public void invoke(@NotNull final Project project, final Editor editor, @NotNull final PsiElement element) throws IncorrectOperationException {
            final PsiFile file = element.getContainingFile();
            if (!FileModificationService.getInstance().prepareFileForWrite(file)) return;

//            InspectionManager inspectionManager = InspectionManager.getInstance(project);
//            ProblemDescriptor descriptor = inspectionManager.createProblemDescriptor(element, element, "", ProblemHighlightType.GENERIC_ERROR_OR_WARNING, false);

            final JSElement property = PsiTreeUtil.getParentOfType(element, JSElement.class);
            LOG.assertTrue(property != null);
            final int start = property.getTextRange().getStartOffset();

            @NonNls final Document doc = PsiDocumentManager.getInstance(project).getDocument(file);
            LOG.assertTrue(doc != null);
            final int line = doc.getLineNumber(start);
            final int lineEnd = doc.getLineEndOffset(line);
            doc.insertString(lineEnd, " //ec0lint-disable-line " + rule);
        }
    }

    private static class SuppressForFile extends SuppressIntentionAction {
        private final String rule;

        public SuppressForFile(String rule) {
            this.rule = rule;
        }

        @NotNull
        public String getText() {
            return Ec0lintBundle.message("unused.property.suppress.for.file");
        }

        @NotNull
        public String getFamilyName() {
            return Ec0lintBundle.message("unused.property.suppress.for.file");
        }

        public boolean isAvailable(@NotNull final Project project, final Editor editor, @NotNull final PsiElement element) {
            return element.isValid() && element.getContainingFile() instanceof JSFile;
        }

        public void invoke(@NotNull final Project project, final Editor editor, @NotNull final PsiElement element) throws IncorrectOperationException {
            final PsiFile file = element.getContainingFile();
            if (!FileModificationService.getInstance().prepareFileForWrite(file)) return;

            @NonNls final Document doc = PsiDocumentManager.getInstance(project).getDocument(file);
            LOG.assertTrue(doc != null, file);

//            doc.insertString(0, "// ec0lint suppress inspection \"" + rule + "\" for whole file\n");
            doc.insertString(0, "/* ec0lint-disable */\n");
        }
    }

    //doc.insertString(lineStart, "/*ec0lint " + rule + ":0*/\n");
}