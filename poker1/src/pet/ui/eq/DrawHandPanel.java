package pet.ui.eq;

import java.util.List;

class DrawHandPanel extends HandCardPanel {
	public DrawHandPanel(List<CardLabel> cls, int n) {
		super("Draw Hand " + n, cls, 1, 5);
	}
}