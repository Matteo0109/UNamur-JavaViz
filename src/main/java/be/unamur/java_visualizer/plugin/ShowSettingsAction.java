package be.unamur.java_visualizer.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ShowSettingsAction extends AnAction {

    public ShowSettingsAction() {
        super("Paramètres", "Ouvrir la fenêtre de configuration des options d'affichage", null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        SettingsDialog dialog = new SettingsDialog();
        if (dialog.showAndGet()) { // Si l'utilisateur valide le dialogue
            String affichage = dialog.getSelectedAffichage();
            // String sensPile = dialog.getSelectedPile(); TODO

            // Récupération de l'instance du manager
            JavaVisualizerManager manager = JavaVisualizerManager.getInstance();
            if (manager != null) {
                // Met à jour le mode d'affichage (abstrait ou concret)
                manager.setAffichageMode(affichage);
                // Force le rafraîchissement de la visualisation pour appliquer le changement
                manager.forceRefreshVisualizer();
            }
        }
    }
}
