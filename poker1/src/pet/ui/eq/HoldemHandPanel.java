package pet.ui.eq;

import java.util.List;

class HoldemHandPanel extends HandCardPanel {
	public HoldemHandPanel(List<CardLabel> cls, int n, boolean tx) {
		super(String.format("%s hand %d", tx ? "Hold'em" : "Omaha", n), cls, 2, tx ? 2 : 4);
	}
}