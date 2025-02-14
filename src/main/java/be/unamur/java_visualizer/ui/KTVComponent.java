package be.unamur.java_visualizer.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;

/**
 * A component that displays a table of three columns of components:
 * Type, Name, and Value, with divider lines.
 */
class KTVComponent extends JPanel {
	// Positions of vertical separators
	private int hsplit1; // separation between the Type column and the Name column
	private int hsplit2; // separation between the Name column and the Value column
	private int[] vsplits;  // horizontal splits (between rows)
	private int padding;

	private Color colorLeft;
	private Color colorMiddle;
	private Color colorRight;
	private Color colorBorder;

	// Three lists: types (left), keys (middle), values (right)
	private List<? extends JComponent> types;
	private List<? extends JComponent> keys;
	private List<? extends JComponent> vals;

	KTVComponent() {
		setLayout(null);
	}

	public void setPadding(int padding) {
		this.padding = padding;
	}

	/**
	 * Sets the three columns: types, keys, and vals.
	 * All lists must be the same size.
	 */
	public void setComponents(List<? extends JComponent> types,
							  List<? extends JComponent> keys,
							  List<? extends JComponent> vals) {
		if (types.size() != keys.size() || keys.size() != vals.size()) {
			throw new IllegalArgumentException("All component lists must be the same size.");
		}
		this.types = types;
		this.keys = keys;
		this.vals = vals;
	}

	public void setColors(Color colorLeft, Color colorMiddle, Color colorRight, Color colorBorder) {
		this.colorLeft = colorLeft;
		this.colorMiddle = colorMiddle;
		this.colorRight = colorRight;
		this.colorBorder = colorBorder;
	}

	public void build() {
		int n = types.size();
		int typeWidth = 0, keyWidth = 0, valueWidth = 0;
		// Calcul des largeurs maximales de chaque colonne
		for (int i = 0; i < n; i++) {
			typeWidth = Math.max(typeWidth, types.get(i).getPreferredSize().width);
			keyWidth = Math.max(keyWidth, keys.get(i).getPreferredSize().width);
			valueWidth = Math.max(valueWidth, vals.get(i).getPreferredSize().width);
		}

		int y = 0;
		vsplits = new int[n];
		for (int i = 0; i < n; i++) {
			JComponent typeComp = types.get(i);
			JComponent keyComp = keys.get(i);
			JComponent valComp = vals.get(i);
			Dimension typeSize = typeComp.getPreferredSize();
			Dimension keySize = keyComp.getPreferredSize();
			Dimension valSize = valComp.getPreferredSize();
			int h = Math.max(Math.max(typeSize.height, keySize.height), valSize.height);

			add(typeComp);
			add(keyComp);
			add(valComp);
			y += padding;
			// Positionner la colonne Type
			int xType = padding;
			typeComp.setBounds(xType + (typeWidth - typeSize.width), y, typeSize.width, h);

			// Positionner la colonne Nom (Key)
			int xKey = xType + typeWidth + padding;
			keyComp.setBounds(xKey + (keyWidth - keySize.width), y, keySize.width, h);

			// Positionner la colonne Valeur (Value)
			int xVal = xKey + keyWidth + padding;
			valComp.setBounds(xVal, y, valSize.width, h);

			y += h + padding;
			vsplits[i] = y;
		}

		int totalWidth = padding + typeWidth + padding + keyWidth + padding + valueWidth + padding;
		setPreferredSize(new Dimension(totalWidth, y));

		// Calcul des séparateurs verticaux (on place la première juste après la colonne Type, et la deuxième après la colonne Key)
		hsplit1 = padding + typeWidth + padding / 2;
		hsplit2 = padding + typeWidth + padding + keyWidth + padding / 2;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		// Paint the background for each column if colors are provided
		if (colorLeft != null) {
			g.setColor(colorLeft);
			g.fillRect(0, 0, hsplit1, getHeight());
		}
		if (colorMiddle != null) {
			g.setColor(colorMiddle);
			g.fillRect(hsplit1, 0, hsplit2 - hsplit1, getHeight());
		}
		if (colorRight != null) {
			g.setColor(colorRight);
			g.fillRect(hsplit2, 0, getWidth() - hsplit2, getHeight());
		}
		// Draw divider lines if colorBorder is provided
		if (colorBorder != null) {
			g.setColor(colorBorder);
			g.drawLine(hsplit1, 0, hsplit1, getHeight());
			g.drawLine(hsplit2, 0, hsplit2, getHeight());
			// Optionally, draw horizontal divider lines
			for (int s : vsplits) {
				g.drawLine(0, s - 1, getWidth(), s - 1);
			}
		}
	}
}
