package com.ec0lint.config;

import com.ec0lint.Ec0lintProjectComponent;
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

public class Ec0lintConfigFileListener {
    private final Project project;
    private final AtomicBoolean LISTENING = new AtomicBoolean(false);

    public Ec0lintConfigFileListener(@NotNull Project project) {
        this.project = project;
    }

    private void startListener() {
        if (LISTENING.compareAndSet(false, true))
            ApplicationManager.getApplication().invokeLater(new Runnable() {
                public void run() {
                    ApplicationManager.getApplication().runWriteAction(new Runnable() {
                        public void run() {
                            VirtualFileManager.getInstance().addVirtualFileListener(new Ec0lintConfigFileVfsListener(), Ec0lintConfigFileListener.this.project);
                            EditorEventMulticaster multicaster = EditorFactory.getInstance().getEventMulticaster();
                            multicaster.addDocumentListener(new Ec0lintConfigFileDocumentListener(), Ec0lintConfigFileListener.this.project);
                        }
                    });
                }
            });
    }

    public static void start(@NotNull Project project) {
        Ec0lintConfigFileListener listener = ServiceManager.getService(project, Ec0lintConfigFileListener.class);
        listener.startListener();
    }

    private void fileChanged(@NotNull VirtualFile file) {
        if (Ec0lintConfigFileUtil.isEc0lintConfigFile(file) && !project.isDisposed()) {
            restartAnalyzer();
        }
    }

    private void restartAnalyzer() {
        Ec0lintProjectComponent component = project.getComponent(Ec0lintProjectComponent.class);
        if (component.isEnabled()) {
            DaemonCodeAnalyzer.getInstance(project).restart();
        }
    }

    /**
     * VFS Listener
     */
    private class Ec0lintConfigFileVfsListener extends VirtualFileAdapter {
        private Ec0lintConfigFileVfsListener() {
        }

        public void fileCreated(@NotNull VirtualFileEvent event) {
            Ec0lintConfigFileListener.this.fileChanged(event.getFile());
        }

        public void fileDeleted(@NotNull VirtualFileEvent event) {
            Ec0lintConfigFileListener.this.fileChanged(event.getFile());
        }

        public void fileMoved(@NotNull VirtualFileMoveEvent event) {
            Ec0lintConfigFileListener.this.fileChanged(event.getFile());
        }

        public void fileCopied(@NotNull VirtualFileCopyEvent event) {
            Ec0lintConfigFileListener.this.fileChanged(event.getFile());
            Ec0lintConfigFileListener.this.fileChanged(event.getOriginalFile());
        }
    }

    /**
     * Document Listener
     */
    private class Ec0lintConfigFileDocumentListener extends DocumentAdapter {
        private Ec0lintConfigFileDocumentListener() {
        }

        public void documentChanged(DocumentEvent event) {
            VirtualFile file = FileDocumentManager.getInstance().getFile(event.getDocument());
            if (file != null) {
                Ec0lintConfigFileListener.this.fileChanged(file);
            }
        }
    }
}

