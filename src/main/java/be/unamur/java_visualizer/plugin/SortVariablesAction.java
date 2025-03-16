package be.unamur.java_visualizer.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

public class SortVariablesAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        String[] options = {"Alphabétique", "FIFO", "LIFO"};

        // On détermine l'index par défaut en fonction du mode actuel
        SortMode currentMode = PluginSettings.getSortMode();
        int defaultIndex;
        switch (currentMode) {
            case FIFO: defaultIndex = 1; break;
            case LIFO: defaultIndex = 2; break;
            default:   defaultIndex = 0; break; // ALPHABETICAL
        }

        int choice = Messages.showChooseDialog(
                "Choisissez le type de tri pour les variables de la stack :",
                "Tri des variables",
                options,
                options[defaultIndex],
                null
        );

        if (choice >= 0) {
            SortMode selectedMode;
            switch (choice) {
                case 1:
                    selectedMode = SortMode.FIFO;
                    break;
                case 2:
                    selectedMode = SortMode.LIFO;
                    break;
                default:
                    selectedMode = SortMode.ALPHABETICAL;
                    break;
            }
            PluginSettings.setSortMode(selectedMode);

            // Rafraîchir l'affichage en relançant traceAndVisualize()
            JavaVisualizerManager manager = JavaVisualizerManager.getInstance();
            if (manager != null) {
                manager.forceRefreshVisualizer();
            }
        }
    }
}
