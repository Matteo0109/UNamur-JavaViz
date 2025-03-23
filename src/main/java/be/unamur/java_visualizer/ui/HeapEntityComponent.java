package be.unamur.java_visualizer.ui;

import be.unamur.java_visualizer.model.*;
import be.unamur.java_visualizer.plugin.PluginSettings;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class HeapEntityComponent extends JPanel {
	private VisualizationPanel viz;
	private HeapEntity entity;
	private List<ValueComponent> valueComponents = new ArrayList<>();

	HeapEntityComponent(VisualizationPanel viz, HeapEntity entity) {
		this.viz = viz;
		this.entity = entity;

		setOpaque(false);
		setLayout(new BorderLayout());
		// setBorder(JBUI.Borders.empty(8));

		JLabel topLabel = new CustomJLabel(entity.label);
		topLabel.setFont(Constants.fontUISmall);
		topLabel.setForeground(Constants.colorHeapLabel);
		add(topLabel, BorderLayout.NORTH);

		JPanel mainPanel = null;
		if (entity instanceof HeapObject) {
			mainPanel = new PanelObject((HeapObject) entity);
		} else if (entity instanceof HeapList) {
			mainPanel = new PanelList((HeapList) entity);
		} else if (entity instanceof HeapMap) {
			mainPanel = new PanelMap((HeapMap) entity);
		} else if (entity instanceof HeapPrimitive) {
			mainPanel = new PanelPrimitive((HeapPrimitive) entity);
		}
		if (mainPanel !=  null) {
			add(mainPanel, BorderLayout.WEST);
		}
	}

	HeapEntity getEntity() {
		return entity;
	}

	List<ValueComponent> getValueComponents() {
		return valueComponents;
	}

	private class PanelObject extends KTVComponent {
		PanelObject(HeapObject e) {
			super();

			if (viz.isAbstractView()) {
				// Mode abstrait : afficher uniquement le résumé de l'objet (toString)
				setLayout(new BorderLayout());

				CustomJLabel summaryLabel = new CustomJLabel(e.toString());
				summaryLabel.setFont(Constants.fontUI);
				summaryLabel.setForeground(Constants.colorText);
				summaryLabel.setOpaque(true);
				summaryLabel.setBackground(Constants.colorHeapVal);

				add(summaryLabel, BorderLayout.CENTER);
			} else {
				// Mode concret : afficher les champs de l'objet

				// Trois listes pour les 3 colonnes
				List<JComponent> typeComps = new ArrayList<>();
				List<JComponent> keyComps  = new ArrayList<>();
				List<JComponent> valComps  = new ArrayList<>();

				for (Map.Entry<String, Value> local : e.fields.entrySet()) {

					// -- 1) TYPE --
					String typeName = (local.getValue().typeName != null)
							? local.getValue().typeName
							: "<?>";
					if ("Simplifié".equals(PluginSettings.getTypeMode())) {
						typeName = PluginSettings.simplifyTypeName(typeName);
					}
					JLabel typeLabel = new CustomJLabel(typeName, JLabel.CENTER);
					typeLabel.setOpaque(true);
					typeLabel.setBackground(StackFrameComponent.isPrimitive(local.getValue())
							? new Color(0xFF, 0xEE, 0xCC)
							: new Color(0xEE, 0xFF, 0xEE));
					typeLabel.setFont(Constants.fontUI);
					typeLabel.setForeground(Constants.colorText);


					// -- 2) KEY --
					JLabel keyLabel = new CustomJLabel(local.getKey(), JLabel.CENTER);
					keyLabel.setFont(Constants.fontUI);
					keyLabel.setForeground(Constants.colorText);

					// -- 3) VALUE --
					ValueComponent val = new ValueComponent(viz, local.getValue());
					valueComponents.add(val); // On l'ajoute à la liste globale pour pointerConnections

					typeComps.add(typeLabel);
					keyComps.add(keyLabel);
					valComps.add(val);
				}
				setComponents(typeComps, keyComps, valComps);

				setColors(Constants.colorHeapVal, Constants.colorHeapVal, Constants.colorHeapVal, Constants.colorHeapBorder);
				setPadding(Constants.padHeapMap);

				build();
			}

		}
	}

	private class PanelMap extends KTVComponent {
		PanelMap(HeapMap e) {
			// Trois listes pour les 3 colonnes
			List<JComponent> typeComps = new ArrayList<>();
			List<JComponent> keyComps  = new ArrayList<>();
			List<JComponent> valComps  = new ArrayList<>();

			for (HeapMap.Pair entry : e.pairs) {
				// -- 1) TYPE --
				String typeName = (entry.val.typeName != null)
						? entry.val.typeName
						: "<?>";
				JLabel typeLabel = new CustomJLabel(typeName, JLabel.CENTER);
				typeLabel.setFont(Constants.fontUI);
				typeLabel.setForeground(Constants.colorText);

				// -- 2) KEY --
				ValueComponent keyComp = new ValueComponent(viz, entry.key);
				valueComponents.add(keyComp);

				// -- 3) VALUE --
				ValueComponent valComp = new ValueComponent(viz, entry.val);
				valueComponents.add(valComp);

				typeComps.add(typeLabel);
				keyComps.add(keyComp);
				valComps.add(valComp);
			}

			setColors(Constants.colorHeapVal, Constants.colorHeapVal, Constants.colorHeapVal, Constants.colorHeapBorder);
			setPadding(Constants.padHeapMap);

			setComponents(typeComps, keyComps, valComps);

			build();
		}
	}

	private class PanelList extends JPanel {
		private int[] splits;

		PanelList(HeapList e) {
			setBackground(Constants.colorHeapVal);
			setLayout(null);
			splits = new int[e.items.size()];

			int height = 0;
			int x = 0;
			for (int i = 0; i < e.items.size(); i++) {
				splits[i] = x;
				ValueComponent value = new ValueComponent(viz, e.items.get(i));
				valueComponents.add(value);
				Dimension size = value.getPreferredSize();
				JLabel indexLabel = new CustomJLabel(Integer.toString(i));
				indexLabel.setFont(Constants.fontUISmall);
				indexLabel.setForeground(Constants.colorHeapLabel);
				Dimension indexSize = indexLabel.getPreferredSize();
				indexLabel.setBounds(x + 4, 4, indexSize.width, indexSize.height);
				add(indexLabel);
				x += 8;
				value.setBounds(x, 4 + indexSize.height + 4, size.width, size.height);
				x += size.width + 8;
				add(value);
				height = Math.max(height, indexSize.height + size.height);
			}
			height += 8 + 8;
			setPreferredSize(new Dimension(x, height));
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			g.setColor(Constants.colorHeapBorder);
			g.drawLine(1, getHeight() - 1, getWidth(), getHeight() - 1);
			for (int s : splits) {
				g.drawLine(s + 1, 0, s + 1, getHeight() - 1);
			}
		}


	}
}
