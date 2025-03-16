package be.unamur.java_visualizer.ui;

import be.unamur.java_visualizer.model.Frame;
import be.unamur.java_visualizer.model.Value;
import be.unamur.java_visualizer.plugin.PluginSettings;    // Pour récupérer le mode de tri
import be.unamur.java_visualizer.plugin.SortMode;         // Pour l'enum des modes de tri

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class StackFrameComponent extends JPanel {
	private Frame frame;
	private VisualizationPanel viz;

	public StackFrameComponent(VisualizationPanel viz, Frame frame, boolean first) {
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

		// ---------------------
		// AJOUT DU TRI DES VARIABLES
		// ---------------------
		// Récupération du mode de tri défini via SortVariablesAction (PluginSettings)
		SortMode mode = PluginSettings.getSortMode();

		// Conversion de la Map en liste pour pouvoir appliquer le tri
		List<Map.Entry<String, Value>> localEntries = new ArrayList<>(frame.locals.entrySet());

		// Appliquer le tri en fonction du mode choisi
		switch (mode) {
			case ALPHABETICAL:
				localEntries.sort(Comparator.comparing(Map.Entry::getKey));
				break;
			case LIFO:
				Collections.reverse(localEntries);
				break;
			case FIFO:
				// FIFO : on conserve l'ordre d'insertion (frame.locals doit être un LinkedHashMap)
				break;
		}
		// ---------------------

		List<JComponent> type = new ArrayList<>();
		List<JComponent> key = new ArrayList<>();
		List<JComponent> val = new ArrayList<>();

		// Parcours de la liste potentiellement réordonnée
		for (Map.Entry<String, Value> local : localEntries) {
			// TYPE
			String typeName = (local.getValue().typeName != null)
					? local.getValue().typeName
					: "<?>";
			JLabel typeLabel = new CustomJLabel(typeName, JLabel.LEFT);
			typeLabel.setOpaque(true);
			// Ne modifie rien ici (la logique de new Color reste inchangée)
			typeLabel.setBackground(isPrimitive(local.getValue())
					? new Color(0xFF, 0xEE, 0xCC)
					: new Color(0xEE, 0xFF, 0xEE));
			typeLabel.setFont(Constants.fontUI);
			typeLabel.setForeground(Constants.colorText);

			// NAME
			JLabel localLabel = new CustomJLabel(local.getKey(), JLabel.RIGHT);
			localLabel.setFont(Constants.fontUI);
			localLabel.setForeground(Constants.colorText);

			// VALUE
			ValueComponent value = new ValueComponent(viz, local.getValue(), first);

			type.add(typeLabel);
			key.add(localLabel);
			val.add(value);
		}

		KTVComponent locals = new KTVComponent();
		locals.setPadding(4);
		locals.setColors(
				Constants.colorHeapKey,
				Constants.colorHeapVal,
				Constants.colorHeapVal,
				Constants.colorHeapBorder
		);
		locals.setComponents(type, key, val);
		locals.build();

		add(locals);
	}

	@Override
	public Dimension getMaximumSize() {
		return Constants.maxDimension;
	}

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
