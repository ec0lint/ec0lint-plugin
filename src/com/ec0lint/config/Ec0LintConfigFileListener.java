package com.ec0lint.config;

import com.ec0lint.Ec0LintProjectComponent;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.EditorEventMulticaster;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.*;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

public class Ec0LintConfigFileListener {
    private final Project project;
    private final AtomicBoolean LISTENING = new AtomicBoolean(false);

    public Ec0LintConfigFileListener(@NotNull Project project) {
        this.project = project;
    }

    private void startListener() {
        if (LISTENING.compareAndSet(false, true))
            ApplicationManager.getApplication().invokeLater(new Runnable() {
                public void run() {
                    ApplicationManager.getApplication().runWriteAction(new Runnable() {
                        public void run() {
                            VirtualFileManager.getInstance().addVirtualFileListener(new ESLintConfigFileVfsListener(), Ec0LintConfigFileListener.this.project);
                            EditorEventMulticaster multicaster = EditorFactory.getInstance().getEventMulticaster();
                            multicaster.addDocumentListener(new ESLintConfigFileDocumentListener(), Ec0LintConfigFileListener.this.project);
                        }
                    });
                }
            });
    }

    public static void start(@NotNull Project project) {
        Ec0LintConfigFileListener listener = ServiceManager.getService(project, Ec0LintConfigFileListener.class);
        listener.startListener();
    }

    private void fileChanged(@NotNull VirtualFile file) {
        if (Ec0LintConfigFileUtil.isESLintConfigFile(file) && !project.isDisposed()) {
            restartAnalyzer();
        }
    }

    private void restartAnalyzer() {
        Ec0LintProjectComponent component = project.getComponent(Ec0LintProjectComponent.class);
        if (component.isEnabled()) {
            DaemonCodeAnalyzer.getInstance(project).restart();
        }
    }

    /**
     * VFS Listener
     */
    private class ESLintConfigFileVfsListener extends VirtualFileAdapter {
        private ESLintConfigFileVfsListener() {
        }

        public void fileCreated(@NotNull VirtualFileEvent event) {
            Ec0LintConfigFileListener.this.fileChanged(event.getFile());
        }

        public void fileDeleted(@NotNull VirtualFileEvent event) {
            Ec0LintConfigFileListener.this.fileChanged(event.getFile());
        }

        public void fileMoved(@NotNull VirtualFileMoveEvent event) {
            Ec0LintConfigFileListener.this.fileChanged(event.getFile());
        }

        public void fileCopied(@NotNull VirtualFileCopyEvent event) {
            Ec0LintConfigFileListener.this.fileChanged(event.getFile());
            Ec0LintConfigFileListener.this.fileChanged(event.getOriginalFile());
        }
    }

    /**
     * Document Listener
     */
    private class ESLintConfigFileDocumentListener extends DocumentAdapter {
        private ESLintConfigFileDocumentListener() {
        }

        public void documentChanged(DocumentEvent event) {
            VirtualFile file = FileDocumentManager.getInstance().getFile(event.getDocument());
            if (file != null) {
                Ec0LintConfigFileListener.this.fileChanged(file);
            }
        }
    }
}

