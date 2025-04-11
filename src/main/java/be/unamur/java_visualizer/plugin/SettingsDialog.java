package be.unamur.java_visualizer.plugin;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class SettingsDialog extends DialogWrapper {

    private JPanel mainPanel;
    private JComboBox<String> affichageCombo;
    private JComboBox<String> pileCombo;
    private JComboBox<String> typeModeCombo;

    public SettingsDialog() {
        super(true); // true pour activer les boutons OK et Annuler
        init();
        setTitle("Paramètres d'affichage");

        // Récupérer le mode actuel depuis JavaVisualizerManager
        String currentDisplayMode = "Concret";

        if (JavaVisualizerManager.getInstance() != null) {
            currentDisplayMode = JavaVisualizerManager.getInstance().getAffichageMode();
        }

        // Pré-sélectionner le mode courant dans le combo box
        affichageCombo.setSelectedItem(currentDisplayMode);

        // Pré-sélection du tri de la pile via PluginSettings
        SortMode currentSortMode = PluginSettings.getSortMode();
        if(currentSortMode != null) {
            pileCombo.setSelectedItem(currentSortMode.toDisplayString());
        }

        String currentTypeMode = PluginSettings.getTypeMode();
        typeModeCombo.setSelectedItem(currentTypeMode);

    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        // Utilisation d'un GridLayout avec 2 colonnes
        mainPanel = new JPanel(new GridLayout(0, 2, 8, 8));

        // Ligne : Type d'affichage
        JLabel affichageLabel = new JLabel("Type d'affichage des objets:");
        // Options disponibles : "abstrait" et "concret"
        affichageCombo = new ComboBox<>(new String[]{"Abstrait", "Concret"});

        JLabel pileLabel = new JLabel("Sens de la pile:");
        pileCombo = new ComboBox<>(new String[]{
                SortMode.TOPDOWN.toDisplayString(),
                SortMode.BOTTOMUP.toDisplayString()
        });

        JLabel typeModeLabel = new JLabel("Extension:");
        typeModeCombo =  new ComboBox<>(new String[]{"Précis", "Simplifié"});

        // Ajout des composants dans le panneau
        mainPanel.add(affichageLabel);
        mainPanel.add(affichageCombo);
        mainPanel.add(pileLabel);
        mainPanel.add(pileCombo);
        mainPanel.add(typeModeLabel);
        mainPanel.add(typeModeCombo);

        return mainPanel;
    }

    // Méthode pour récupérer l'option de type d'affichage sélectionnée
    public String getSelectedAffichage() {
        return (String) affichageCombo.getSelectedItem();
    }

    // Méthode pour récupérer l'option de tri de la pile sélectionnée
    public String getSelectedPile() {
        return (String) pileCombo.getSelectedItem();
    }

    // Méthode pour récupérer l'option de mode de type sélectionnée
    public String getSelectedTypeMode() {
        return (String) typeModeCombo.getSelectedItem();
    }

}
