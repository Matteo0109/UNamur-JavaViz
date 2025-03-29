package be.unamur.java_visualizer.ui;

import be.unamur.java_visualizer.model.Frame;
import be.unamur.java_visualizer.model.Value;
import be.unamur.java_visualizer.plugin.PluginSettings;    // Pour récupérer le mode de tri
import be.unamur.java_visualizer.plugin.SortMode;         // Pour l'enum des modes de tri
import be.unamur.java_visualizer.ui.RoundedBorder;

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
	private String frameName;

	public StackFrameComponent(VisualizationPanel viz, Frame frame, boolean first) {
		this.frame = frame;
		this.viz = viz;
		this.frameName = frame.name;

		//setBackground(first ? Constants.colorFrameBGFirst : Constants.colorFrameBG);
		setOpaque(false);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

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
				// FIFO : on conserve l'ordre d'insertion
				break;
		}
		// ---------------------

		List<JComponent> type = new ArrayList<>();
		List<JComponent> key = new ArrayList<>();
		List<JComponent> val = new ArrayList<>();

		// -- Ajout de la première ligne (en-tête) --
		JLabel headerType = new JLabel("Type", SwingConstants.CENTER);
		headerType.setForeground(Color.BLACK);
		headerType.setFont(Constants.fontUI.deriveFont(Font.BOLD));

		JLabel headerKey = new JLabel("Variable", SwingConstants.CENTER);
		headerKey.setForeground(Color.BLACK);
		headerKey.setFont(Constants.fontUI.deriveFont(Font.BOLD));

		JLabel headerVal = new JLabel(" Valeur  ", SwingConstants.CENTER);
		headerVal.setForeground(Color.BLACK);
		headerVal.setFont(Constants.fontUI.deriveFont(Font.BOLD));


		type.add(headerType);
		key.add(headerKey);
		val.add(headerVal);

		// Parcours de la liste potentiellement réordonnée
		for (Map.Entry<String, Value> local : localEntries) {
			// TYPE
			String typeMode = PluginSettings.getTypeMode(); // "précis" ou "simplifié"
			String typeName = (local.getValue().typeName != null) ? local.getValue().typeName : "<?>";

			if ("Simplifié".equals(typeMode)) {
				// Simplification du nom du type
				typeName = PluginSettings.simplifyTypeName(typeName);
			}

			JLabel typeLabel = new CustomJLabel(typeName, JLabel.CENTER);
			typeLabel.setOpaque(true);
			typeLabel.setBackground(isPrimitive(local.getValue())
					? Constants.colorPrimitiveLabel
					: Constants.colorNonPrimitiveLabel);
			typeLabel.setFont(Constants.fontUI);
			typeLabel.setForeground(Constants.colorText);
			typeLabel.setBorder(new RoundedBorder(8));

			// NAME
			JLabel localLabel = new CustomJLabel(local.getKey(), JLabel.CENTER);
			localLabel.setFont(Constants.fontUI);
			localLabel.setForeground(Constants.colorText);

			// VALUE
			ValueComponent value = new ValueComponent(viz, local.getValue(), first);

			type.add(typeLabel);
			key.add(localLabel);
			val.add(value);
		}

		KTVComponent locals = new KTVComponent();
		locals.setPadding(7);
		locals.setBackground(first ? Constants.colorFrameBGFirst : Constants.colorFrameBG);
		locals.setOpaque(true);
		locals.setColors(Constants.colorHeapKey, Constants.colorHeapVal, Constants.colorHeapVal, Constants.colorHeapBorder);
		locals.setComponents(type, key, val);
		locals.build();
		locals.setBorder(new RoundedBorder(8));


		add(locals);
	}

	public String getFrameName() {
		return frameName;
	}

	@Override
	public Dimension getMaximumSize() {
		return Constants.maxDimension;
	}

	public static boolean isPrimitive(Value v) {
        return switch (v.type) {
            case LONG, DOUBLE, BOOLEAN, CHAR -> true;
            default -> false;
        };
	}


}
