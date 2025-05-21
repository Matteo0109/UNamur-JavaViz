package be.unamur.java_visualizer.plugin;

import be.unamur.java_visualizer.ui.VisualizationPanel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import com.intellij.icons.AllIcons;

/**
 * Prend un screenshot de la Visualisation et demande où le sauvegarder.
 */
public class ScreenshotAction extends AnAction {

    public ScreenshotAction() {
        super("Screenshot", "Enregistrer la visualisation en image PNG", 
		AllIcons.Actions.MenuSaveall);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        // Récupère le MainPane => VisualizationPanel
        MainPane main = (MainPane) e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
        if (main == null) return;

        VisualizationPanel viz = main.getVisualizationPanel();
        if (viz == null) return;

        // On capture la taille « réelle » du panel
        Dimension size = viz.getPreferredSize();
        BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Force la validation et le layout avant peinture
        SwingUtilities.invokeLater(() -> {
            viz.doLayout();
            viz.paintAll(g);
            g.dispose();

            // Affiche le chooser dans l'Event-Dispatch-Thread
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Enregistrer le screenshot");
            chooser.setSelectedFile(new File("visualisation.png"));
			chooser.setFileFilter(new FileNameExtensionFilter("Images PNG", "png"));

            int ret = chooser.showSaveDialog(viz);
            if (ret == JFileChooser.APPROVE_OPTION) {
                File chosen = chooser.getSelectedFile();
				String path = chosen.getAbsolutePath();

				// Si l'utilisateur n'a pas saisi .png, on l'ajoute
				if (!path.toLowerCase().endsWith(".png")) {
					chosen = new File(path + ".png");
				}
                try {
                    ImageIO.write(img, "PNG", chosen );
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
