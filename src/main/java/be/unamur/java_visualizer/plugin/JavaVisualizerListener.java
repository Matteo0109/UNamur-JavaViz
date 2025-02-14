package be.unamur.java_visualizer.plugin;

import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebuggerManagerListener;
import org.jetbrains.annotations.NotNull;

public class JavaVisualizerListener implements XDebuggerManagerListener {
    private final Project project;

    public JavaVisualizerListener(Project project) {
        this.project = project;
    }

    @Override
    public void processStarted(@NotNull XDebugProcess debugProcess) {
        System.out.println("JavaVisualizerListener : Debug process started.");
        new JavaVisualizerManager(project, debugProcess);
    }
}
