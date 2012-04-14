package pet.ui.eq;

/**
 * create a hand card panel that displays either holdem or omaha hands
 */
class HoldemHandPanel extends HandCardPanel {
	public HoldemHandPanel(int n, boolean omaha) {
		super(String.format("%s hand %d", omaha ? "Omaha" : "Hold'em", n), 2, omaha ? 4 : 2);
	}
}
