package be.unamur.java_visualizer.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import com.intellij.icons.AllIcons;

public class ShowSettingsAction extends AnAction {

    public ShowSettingsAction() {
        super("Paramètres", "Ouvrir la fenêtre de configuration des options d'affichage",
                AllIcons.General.Settings);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        SettingsDialog dialog = new SettingsDialog();
        if (dialog.showAndGet()) { // Si l'utilisateur valide le dialogue
            String affichage = dialog.getSelectedAffichage();
            String sensPile = dialog.getSelectedPile();
            String typeMode = dialog.getSelectedTypeMode();

            // Récupération de l'instance du manager
            JavaVisualizerManager manager = JavaVisualizerManager.getInstance();
            if (manager != null) {
                // Met à jour le mode d'affichage (abstrait ou concret)
                manager.setAffichageMode(affichage);

                // Mise à jour du sens de la pile en fonction de la sélection
                SortMode selectedMode;
                if (sensPile.equals(SortMode.BOTTOMUP.toDisplayString())) {
                    selectedMode = SortMode.BOTTOMUP;
                } else {
                    selectedMode = SortMode.TOPDOWN;
                }
                PluginSettings.setSortMode(selectedMode);

                // Mise à jour du mode de type
                PluginSettings.setTypeMode(typeMode);

                // Force le rafraîchissement de la visualisation pour appliquer le changement
                manager.forceRefreshVisualizer();
            }
        }
    }
}
