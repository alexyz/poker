package pet.ui.eq;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

import pet.eq.*;

/**
 * extends card panel to show hand equity statistics
 *        [total] [scoop] [low poss]
 *        [hi eq] [hi cur] [hi outs]
 * [hand] [hi ranks]
 *        [low eq] [low cur] [low outs]
 *        [low ranks]
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

	private final JLabel totalLabel = new JLabel();
	private final EquityPanel hiOnlyEquityPanel = new EquityPanel();
	private final RanksPanel hiOnlyRanks = new RanksPanel();
	private final EquityPanel hiHalfEquityPanel = new EquityPanel();
	private final RanksPanel hiHalfRanks = new RanksPanel();
	private final EquityPanel loHalfEquityPanel = new EquityPanel();

	public HandCardPanel(String name, int mincards, int maxcards) {
		super(name, mincards, maxcards);
		hiHalfEquityPanel.setVisible(false);
		loHalfEquityPanel.setVisible(false);
		
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints g = new GridBagConstraints();
		g.gridx = 0;
		g.fill = GridBagConstraints.HORIZONTAL;
		g.weightx = 1;
		
		g.gridy = 0;
		p.add(totalLabel, g);
		
		g.gridy++;
		p.add(hiOnlyEquityPanel, g);
		
		g.gridy++;
		p.add(hiOnlyRanks, g);
		
		g.gridy++;
		p.add(hiHalfEquityPanel, g);
		
		g.gridy++;
		p.add(hiHalfRanks, g);
		
		g.gridy++;
		p.add(loHalfEquityPanel, g);
		
		// add to superclass layout
		addDetails(p);
	}
	
	private void clearHandEquity() {
		hiOnlyEquityPanel.clearHandEquity();
		hiOnlyRanks.clearHandEquity();
		hiHalfEquityPanel.clearHandEquity();
		hiHalfEquityPanel.setVisible(false);
		hiHalfRanks.clearHandEquity();
		hiHalfRanks.setVisible(false);
		loHalfEquityPanel.clearHandEquity();
		loHalfEquityPanel.setVisible(false);
		totalLabel.setText("");
	}

	public void setHandEquity(MEquity me) {
		clearHandEquity();
		if (me != null) {
			if (me.hionly() != null) {
				hiOnlyEquityPanel.setHandEquity(me, me.hionly());
				hiOnlyRanks.setHandEquity(me.hionly());
			}
			if (me.hihalf() != null) {
				hiHalfEquityPanel.setHandEquity(me, me.hihalf());
				hiHalfEquityPanel.setVisible(true);
				hiHalfRanks.setHandEquity(me.hihalf());
				hiHalfRanks.setVisible(true);
			}
			if (me.lohalf() != null) {
				loHalfEquityPanel.setHandEquity(me, me.lohalf());
				loHalfEquityPanel.setVisible(true);
			}
			totalLabel.setText("Total Equity: " + me.totaleq + " Low possible: " + me.lowPossible + " Scoop: " + me.scoop);
		}
	}

	@Override
	public void clearCards() {
		super.clearCards();
		clearHandEquity();
	}

}



