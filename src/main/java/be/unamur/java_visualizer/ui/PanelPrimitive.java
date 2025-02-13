package be.unamur.java_visualizer.ui;

import be.unamur.java_visualizer.model.HeapPrimitive;

import javax.swing.*;
import java.awt.*;

class PanelPrimitive extends JPanel {
    PanelPrimitive(HeapPrimitive e) {
        setLayout(null);
        setBackground(Constants.colorHeapVal);
        // Optionnel: setBorder(BorderFactory.createLineBorder(Constants.colorHeapBorder));

        // Récupérer la valeur string et l'entourer de guillemets
        String quotedVal = "\"" + e.value.stringValue + "\"";

        JLabel label = new CustomJLabel(quotedVal);
        label.setFont(Constants.fontUI);
        label.setForeground(Constants.colorText);

        // On place ce label dans le panneau
        Dimension size = label.getPreferredSize();
        label.setBounds(8, 8, size.width, size.height);
        add(label);

        // Prévoir la taille globale
        setPreferredSize(new Dimension(size.width + 16, size.height + 16));
    }
}


