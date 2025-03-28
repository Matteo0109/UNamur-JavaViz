package be.unamur.java_visualizer.ui;

import com.intellij.util.ui.JBUI;

import javax.swing.border.Border;
import java.awt.*;

public class RoundedBorder implements Border {
    private int radius; // Le rayon des coins arrondis

    public RoundedBorder(int radius) {
        this.radius = radius;
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return JBUI.insets(3);
    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
    }
}
