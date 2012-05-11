package pet.ui.eq;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.LineBorder;

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
	private final JLabel totalLabel = new JLabel();

	public HandCardPanel(String name, int mincards, int maxcards) {
		super(name, mincards, maxcards);
		lowEquityPanel.setVisible(false);
		
		JPanel p = new JPanel(new GridBagLayout());
		p.setBorder(new LineBorder(Color.green));
		GridBagConstraints g = new GridBagConstraints();
		
		totalLabel.setBorder(new LineBorder(Color.red));
		g.gridx = 0;
		g.gridy = 0;
		p.add(totalLabel, g);
		
		highEquityPanel.setBorder(new LineBorder(Color.red));
		g.gridy++;
		p.add(highEquityPanel, g);
		
		lowEquityPanel.setBorder(new LineBorder(Color.red));
		g.gridy++;
		p.add(lowEquityPanel, g);
		
		// add to superclass layout
		addDetails(p);
	}

	public void setHandEquity(MEquity me) {
		highEquityPanel.clearHandEquity();
		lowEquityPanel.clearHandEquity();
		lowEquityPanel.setVisible(false);
		totalLabel.setText("");
		if (me != null) {
			if (me.hi != null) {
				highEquityPanel.setHandEquity(me, true);
			}
			if (me.lo != null) {
				lowEquityPanel.setHandEquity(me, false);
				lowEquityPanel.setVisible(true);
			}
			totalLabel.setText("Total Equity: " + me.totaleq + " Low possible: " + me.lowPossible);
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



