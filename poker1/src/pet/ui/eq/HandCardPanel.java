package pet.ui.eq;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

import pet.eq.*;

/**
 * extends card panel to show hand equity statistics
 */
class HandCardPanel extends CardPanel {

	/**
	 * get cards from array of hand card panels.
	 * return null if no cards set or some hands incomplete
	 */
	public static String[][] getCards(HandCardPanel[] cps) {
		List<String[]> hands = new ArrayList<String[]>();
		for (HandCardPanel cp : cps) {
			String[] cards = cp.getCards();
			if (cards.length > 0) {
				if (cards.length < cp.getMinCards()) {
					System.out.println("not enough cards for " + cp);
					return null;
				}
				hands.add(cards);
			}
		}
		System.out.println("hands: " + hands.size());
		return hands.size() == 0 ? null : hands.toArray(new String[hands.size()][]);
	}

	private final EquityPanel highEquityPanel = new EquityPanel(true);
	private final EquityPanel lowEquityPanel = new EquityPanel(false);

	public HandCardPanel(String name, int mincards, int maxcards) {
		super(name, mincards, maxcards);
		lowEquityPanel.setVisible(false);
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(highEquityPanel);
		p.add(lowEquityPanel);
		// add to superclass layout
		add(p, BorderLayout.CENTER);
	}

	public void setHandEquity(MEquity me) {
		if (me != null && me.hi != null) {
			highEquityPanel.setHandEquity(me, true);
		} else {
			highEquityPanel.clearHandEquity();
		}
		if (me != null && me.lo != null) {
			lowEquityPanel.setVisible(true);
			lowEquityPanel.setHandEquity(me, false);
		} else {
			lowEquityPanel.setVisible(false);
			lowEquityPanel.clearHandEquity();
		}
	}

	@Override
	public void clearCards() {
		super.clearCards();
		highEquityPanel.clearHandEquity();
		lowEquityPanel.clearHandEquity();
		lowEquityPanel.setVisible(false);
	}

}



