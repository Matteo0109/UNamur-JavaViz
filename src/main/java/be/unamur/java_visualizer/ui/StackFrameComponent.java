package be.unamur.java_visualizer.ui;

import be.unamur.java_visualizer.model.Frame;
import be.unamur.java_visualizer.model.Value;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class StackFrameComponent extends JPanel {
	private Frame frame;
	private VisualizationPanel viz;

	StackFrameComponent(VisualizationPanel viz, Frame frame, boolean first) {
		this.frame = frame;
		this.viz = viz;

		setBackground(first ? Constants.colorFrameBGFirst : Constants.colorFrameBG);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new MatteBorder(0, 1, 0, 0, Constants.colorFrameOutline));

		JLabel labelName = new CustomJLabel(frame.name, JLabel.LEFT);
		labelName.setFont(Constants.fontUIMono);
		labelName.setForeground(Constants.colorText);
		labelName.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		labelName.setAlignmentX(RIGHT_ALIGNMENT);
		labelName.setMaximumSize(Constants.maxDimension);
		add(labelName);

		List<JComponent> type = new ArrayList<>();
		List<JComponent> key = new ArrayList<>();
		List<JComponent> val  = new ArrayList<>();

		for (Map.Entry<String, Value> local : frame.locals.entrySet()) {
			Value v = local.getValue();

			// TYPE de la variable
			String typeName = (v.typeName != null) ? v.typeName : "<?>";

			// On crée un label pour le type
			JLabel typeLabel = new CustomJLabel(typeName, JLabel.LEFT);
			typeLabel.setOpaque(true);

			// Code couleur : on colorie le fond selon que c'est un primitif ou non
			if (isPrimitive(v)) {
				typeLabel.setBackground(new Color(0xFF, 0xEE, 0xCC)); // Couleur pour primitifs
			} else {
				typeLabel.setBackground(new Color(0xEE, 0xFF, 0xEE)); // Couleur pour objets
			}
			typeLabel.setFont(Constants.fontUI);
			typeLabel.setForeground(Constants.colorText);

			// NOM de la variable
			JLabel localLabel = new CustomJLabel(local.getKey(), JLabel.RIGHT);
			localLabel.setForeground(Constants.colorText);
			localLabel.setFont(Constants.fontUI);

			// VALEUR
			ValueComponent value = new ValueComponent(viz, v, first);
			Border b1 = new MatteBorder(0, 1, 1, 0, Constants.colorFrameOutline);
			Border b2 = BorderFactory.createEmptyBorder(2, 2, 2, 2);
			value.setBorder(new CompoundBorder(b1, b2));

			// On ajoute chaque composant dans la liste qui correspond
			type.add(typeLabel);
			key.add(localLabel);
			val.add(value);
		}

		KTVComponent locals = new KTVComponent();
		locals.setPadding(4);
		locals.setDividerColor(Constants.colorHeapBorder);

		// On passe les trois listes : type, nom, valeur
		locals.setComponents(type, key, val);
		locals.build();

		add(locals);
	}

	@Override
	public Dimension getMaximumSize() {
		return Constants.maxDimension;
	}

	// Méthode pour savoir si une valeur est un primitif
	private boolean isPrimitive(Value v) {
		switch (v.type) {
			case LONG:
			case DOUBLE:
			case BOOLEAN:
			case CHAR:
				return true;
			default:
				return false;
		}
	}
}
