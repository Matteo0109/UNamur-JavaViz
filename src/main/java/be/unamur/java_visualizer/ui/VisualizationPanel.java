package be.unamur.java_visualizer.ui;

import be.unamur.java_visualizer.model.ExecutionTrace;
import be.unamur.java_visualizer.model.Value;
import be.unamur.java_visualizer.plugin.PluginSettings;
import com.intellij.ui.JBColor; // Pour la couleur du frame courant

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static be.unamur.java_visualizer.ui.Constants.*;

public class VisualizationPanel extends JPanel {
	private ExecutionTrace trace = null;
	private double scale = 1.0;

	private List<ValueComponent> referenceComponents;
	private List<PointerConnection> pointerConnections;
	private StackPanel stackPanel;
	private HeapPanel heapPanel;
	private PointerConnection selectedPointer;
	private boolean abstractView = false;


	public VisualizationPanel() {
		setBackground(colorBackground);
		setLayout(null);
		referenceComponents = new ArrayList<>();
		pointerConnections = new ArrayList<>();

		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int px = (int) (e.getX() / scale);
				int py = (int) (e.getY() / scale);
				PointerConnection sel = getSelectedPointer(px, py);
				if (sel != selectedPointer) {
					if (selectedPointer != null) {
						selectedPointer.setSelected(false);
					}
					selectedPointer = sel;
					if (selectedPointer != null) {
						selectedPointer.setSelected(true);
					}
					repaint();
				}
			}
		});
	}

	public void setTrace(ExecutionTrace t) {
		this.trace = t;
		refreshUI();
	}

	public void setScale(double scale) {
		this.scale = scale;
		if (this.trace != null) {
			refreshUI();
		}
	}

	public void setAbstractView(boolean mode) {
		this.abstractView = mode;
		if (this.trace != null) {
			refreshUI();
		}
	}

	public boolean isAbstractView() {
		return abstractView;
	}

	private void refreshUI() {
		if (trace == null) {
			removeAll();
			buildUI(); // Afficher le message "Aucun trace..."
			revalidate();
			repaint();
			return;
		}
		referenceComponents.clear();
		pointerConnections.clear();
		removeAll();
		buildUI();
		revalidate();
		repaint();
	}

	private void buildUI() {
		if (trace == null) {
			JLabel noTraceLabel = new CustomJLabel("Aucun trace à afficher pour l'instant. Lancez le débogueur et arrêtez-vous sur un point d'arrêt.", JLabel.CENTER);
			noTraceLabel.setFont(Constants.fontUI);
			Dimension prefSize = noTraceLabel.getPreferredSize();
			noTraceLabel.setBounds(padOuter, padOuter, 600, prefSize.height);
			add(noTraceLabel);
			setPreferredSize(new Dimension(600 + 2 * padOuter, prefSize.height + 2 * padOuter));
			return;
		}

		// Récupérer le nom du frame courant (le premier dans la liste = le plus haut dans la pile)
		String currentFrameName = "N/A";
		if (trace.frames != null && !trace.frames.isEmpty()) {
			currentFrameName = trace.frames.get(0).name;
		}

		// Panel d'information en haut
		JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
		infoPanel.setOpaque(false);

		JLabel currentFrameLabel = new CustomJLabel("Current: " + currentFrameName + " ");
		currentFrameLabel.setFont(Constants.fontUIMono.deriveFont(Font.BOLD));
		currentFrameLabel.setForeground(JBColor.BLUE);
		infoPanel.add(currentFrameLabel);

		infoPanel.add(new CustomJLabel("|") {{ setFont(Constants.fontUISmall); }});

		JLabel displayModeLabel = new CustomJLabel("Affichage : " + (abstractView ? "Abstrait" : "Concret"));
		displayModeLabel.setFont(Constants.fontUISmall);
		infoPanel.add(displayModeLabel);

		infoPanel.add(new CustomJLabel("|") {{ setFont(Constants.fontUISmall); }});

		JLabel sortModeLabel = new CustomJLabel("Tri Stack : " + PluginSettings.getSortMode().name());
		sortModeLabel.setFont(Constants.fontUISmall);
		infoPanel.add(sortModeLabel);

		infoPanel.add(new CustomJLabel("|") {{ setFont(Constants.fontUISmall); }});

		JLabel typeModeLabel = new CustomJLabel("Types : " + PluginSettings.getTypeMode());
		typeModeLabel.setFont(Constants.fontUISmall);
		infoPanel.add(typeModeLabel);

		add(infoPanel);

		// Création des composants principaux
		JLabel labelStack = new CustomJLabel("Stack", JLabel.CENTER);
		JLabel labelHeap = new CustomJLabel("Heap", JLabel.CENTER);
		labelStack.setForeground(Constants.colorText);
		labelHeap.setForeground(Constants.colorText);
		labelStack.setFont(fontTitle);
		labelHeap.setFont(fontTitle);
		stackPanel = new StackPanel(this, trace.frames);
		heapPanel = new HeapPanel(this, trace.heap, this.abstractView);

		add(labelStack);
		add(labelHeap);
		add(stackPanel);
		add(heapPanel);

		// Calculs de dimensions et positionnement
		int labelHeight = Math.max(labelStack.getPreferredSize().height, labelHeap.getPreferredSize().height);
		Dimension sizeStack = stackPanel.getPreferredSize();
		Dimension sizeHeap = heapPanel.getPreferredSize();
		int minPanelWidth = 200;
		int stackWidth = Math.max(Math.max(labelStack.getPreferredSize().width, sizeStack.width), minPanelWidth);
		int heapWidth = Math.max(Math.max(labelHeap.getPreferredSize().width, sizeHeap.width), minPanelWidth);

		int infoPanelHeight = infoPanel.getPreferredSize().height;
		int verticalGapAfterInfo = Constants.padOuter / 2;
		int contentWidth = stackWidth + Constants.padCenter + heapWidth;
		int infoPanelWidth = infoPanel.getPreferredSize().width;
		infoPanel.setBounds(padOuter, padOuter, Math.min(infoPanelWidth, contentWidth), infoPanelHeight);

		int titlesStartY = padOuter + infoPanelHeight + verticalGapAfterInfo;
		labelStack.setBounds(padOuter, titlesStartY, stackWidth, labelHeight);
		labelHeap.setBounds(padOuter + stackWidth + padCenter, titlesStartY, heapWidth, labelHeight);

		int panelsStartY = titlesStartY + labelHeight + padTitle;
		stackPanel.setBounds(padOuter, panelsStartY, stackWidth, sizeStack.height);
		heapPanel.setBounds(padOuter + stackWidth + padCenter, panelsStartY, heapWidth, sizeHeap.height);

		int outerWidth = (padOuter * 2) + stackWidth + padCenter + heapWidth;
		int outerHeight = panelsStartY + Math.max(sizeStack.height, sizeHeap.height) + padOuter;
		setPreferredSize(new Dimension((int) (outerWidth * scale), (int) (outerHeight * scale)));
	}

	private void computePointerPaths() {
		if (heapPanel == null || stackPanel == null || referenceComponents == null || trace == null) {
			return;
		}
		pointerConnections.clear();

		for (ValueComponent ref : referenceComponents) {
			if (!ref.isShowing() || ref.getParent() == null) continue;

			Rectangle refBounds = getRelativeBounds(this, ref);
			if (refBounds == null) continue;

			long refId = ref.getValue().reference;
			if (trace.heap == null || !trace.heap.containsKey(refId)) continue;

			HeapEntityComponent obj = heapPanel.getHeapComponents().get(refId);
			if (obj == null || !obj.isShowing() || obj.getParent() == null) {
				continue;
			}

			Rectangle objBounds = getRelativeBounds(this, obj);
			if (objBounds == null) {
				continue;
			}

			double startX = refBounds.x + refBounds.width - (pointerWidth / 2.0);
			double startY = refBounds.y + (refBounds.height / 2.0);
			double endX = objBounds.x;
			double endY = objBounds.y + (objBounds.height / 2.0);

			PointerConnection p = new PointerConnection( ref.isActive(), startX, startY, endX, endY );
			pointerConnections.add(p);
		}
	}

	@Override
	protected void paintChildren(Graphics _g) {
		Graphics2D g = (Graphics2D) _g.create();
		try {
			g.scale(scale, scale);
			super.paintChildren(g);

			if (pointerConnections != null && !pointerConnections.isEmpty()) {
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				List<PointerConnection> connectionsToDraw = new ArrayList<>(pointerConnections);
				for (PointerConnection p : connectionsToDraw) {
					if (p != null) {
						p.paint(g);
					}
				}
			}
		} finally {
			g.dispose();
		}
	}

	@Override
	protected void validateTree() {
		super.validateTree();
		computePointerPaths();
	}

	List<ValueComponent> getReferenceComponents() {
		return referenceComponents;
	}

	void registerValueComponent(ValueComponent component) {
		if (component != null && component.getValue().type == Value.Type.REFERENCE) {
			if (!referenceComponents.contains(component)) {
				referenceComponents.add(component);
			}
		}
	}

	private PointerConnection getSelectedPointer(int x, int y) {
		if (pointerConnections == null || pointerConnections.isEmpty()) {
			return null;
		}
		PointerConnection selected = null;
		Iterator<PointerConnection> it = pointerConnections.iterator();
		PointerConnection potentialSelection = null;
		while (it.hasNext()) {
			PointerConnection p = it.next();
			if (p.isNear(x, y)) {
				potentialSelection = p;
				it.remove();
				break;
			}
		}
		if (potentialSelection != null) {
			pointerConnections.add(potentialSelection);
			selected = potentialSelection;
		}
		return selected;
	}

	private static Rectangle getRelativeBounds(Component parent, Component c) {
		if (parent == null || c == null || !SwingUtilities.isDescendingFrom(c, parent)) {
			return null;
		}
		Point compCoords = new Point(0, 0);
		Point parentCoords = SwingUtilities.convertPoint(c, compCoords, parent);
		return new Rectangle(parentCoords, c.getSize());
	}
}