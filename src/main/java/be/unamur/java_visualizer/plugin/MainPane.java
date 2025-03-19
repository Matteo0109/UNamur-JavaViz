package be.unamur.java_visualizer.plugin;

import be.unamur.java_visualizer.model.ExecutionTrace;
import be.unamur.java_visualizer.ui.VisualizationPanel;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;

public class MainPane extends JPanel {
    private JLabel placeholderLabel;
    private JLabel sortModeLabel;
    private VisualizationPanel viz;
    private ExecutionTrace currentTrace;

    private final float[] ZOOM_LEVELS = {
            0.25f, 0.333f, 0.5f, 0.666f, 0.75f, 0.8f, 0.9f,
            1.0f, 1.1f, 1.25f, 1.5f, 1.75f, 2.0f, 2.5f, 3.0f, 4.0f
    };

    MainPane() {
        setLayout(new BorderLayout());

        // Label pour informer l'utilisateur du mode de tri
        sortModeLabel = new JLabel("Tri actuel : " + PluginSettings.getSortMode());
        add(sortModeLabel, BorderLayout.NORTH);

        String text = "No execution trace loaded: make sure you've stopped on a breakpoint.";
        placeholderLabel = new JLabel(text, SwingConstants.CENTER);
        add(placeholderLabel, BorderLayout.CENTER);
    }

    void setTrace(ExecutionTrace trace) {
        //Mémorisation de la trace actuelle
        this.currentTrace = trace;

        // Met à jour le label du mode de tri (ex. ALPHABETICAL / FIFO / LIFO)
        sortModeLabel.setText("Tri actuel : " + PluginSettings.getSortMode());

        if (viz == null) {
            remove(placeholderLabel);

            viz = new VisualizationPanel();
            viz.setScale(getZoom());

            // On place le VisualizationPanel dans un JBScrollPane
            JBScrollPane scrollPane = new JBScrollPane(viz);
            scrollPane.setBorder(null);

            // Important : on l'ajoute au CENTER pour qu'il prenne tout l'espace
            add(scrollPane, BorderLayout.CENTER);

            revalidate();
        }
        // On met à jour la trace affichée
        viz.setTrace(trace);
    }

    // AJOUT : méthode pour réappliquer la dernière trace (rafraîchir l'affichage)
    void refreshTrace() {
        if (viz != null && currentTrace != null) {
            viz.setTrace(currentTrace);
            revalidate();
            repaint();
        }
    }

    void zoom(int direction) {
        if (viz != null) {
            float currentZoom = getZoom();
            int closestLevel = -1;
            float closestLevelDistance = Float.MAX_VALUE;
            for (int i = 0; i < ZOOM_LEVELS.length; i += 1) {
                float dist = Math.abs(ZOOM_LEVELS[i] - currentZoom);
                if (dist < closestLevelDistance) {
                    closestLevelDistance = dist;
                    closestLevel = i;
                }
            }

            int level = Math.max(0, Math.min(ZOOM_LEVELS.length - 1, closestLevel + direction));
            float newZoom = ZOOM_LEVELS[level];
            PropertiesComponent.getInstance().setValue(JavaVisualizerManager.KEY_ZOOM, newZoom, 1.0f);
            viz.setScale(newZoom);
        }
    }

    private float getZoom() {
        return PropertiesComponent.getInstance().getFloat(JavaVisualizerManager.KEY_ZOOM, 1.0f);
    }

    public VisualizationPanel getVisualizationPanel() {
        return this.viz;
    }

}
