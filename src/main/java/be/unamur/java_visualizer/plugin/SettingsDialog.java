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

    // TODO sens de la pile

    public SettingsDialog() {
        super(true); // true pour activer les boutons OK et Annuler
        init();
        setTitle("Paramètres d'affichage");

        // Récupérer le mode actuel depuis JavaVisualizerManager (mode par défaut = "concret")
        String currentMode = "concret";
        if (JavaVisualizerManager.getInstance() != null) {
            currentMode = JavaVisualizerManager.getInstance().getAffichageMode();
        }
        // Pré-sélectionner le mode courant dans le combo box
        affichageCombo.setSelectedItem(currentMode);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        // Utilisation d'un GridLayout avec 2 colonnes
        mainPanel = new JPanel(new GridLayout(0, 2, 8, 8));

        // Ligne : Type d'affichage
        JLabel affichageLabel = new JLabel("Type d'affichage:");
        // Options disponibles : "abstrait" et "concret"
        affichageCombo = new ComboBox<>(new String[]{"abstrait", "concret"});

        JLabel pileLabel = new JLabel("Sens de la pile:");
        pileCombo = new ComboBox<>(new String[]{"alphabetique", "LIFO", "FIFO"});

        // Ajout des composants dans le panneau
        mainPanel.add(affichageLabel);
        mainPanel.add(affichageCombo);
        mainPanel.add(pileLabel);
        mainPanel.add(pileCombo);

        return mainPanel;
    }

    // Méthode pour récupérer l'option de type d'affichage sélectionnée
    public String getSelectedAffichage() {
        return (String) affichageCombo.getSelectedItem();
    }
}
