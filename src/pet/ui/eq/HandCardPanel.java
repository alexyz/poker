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
	private final EquityPanel firstEquityPanel = new EquityPanel();
	private final RanksPanel firstRankPanel = new RanksPanel();
	private final EquityPanel secondEquityPanel = new EquityPanel();
	private final RanksPanel secondRanksPanel = new RanksPanel();
	private final EquityPanel thirdEquityPanel = new EquityPanel();
	private final RanksPanel thirdRanksPanel = new RanksPanel();
	
	public HandCardPanel(String name, int mincards, int maxcards, boolean below) {
		super(name, mincards, maxcards);
		secondEquityPanel.setVisible(false);
		thirdEquityPanel.setVisible(false);
		
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints g = new GridBagConstraints();
		g.gridx = 0;
		g.fill = GridBagConstraints.HORIZONTAL;
		g.weightx = 1;
		
		g.gridy = 0;
		p.add(totalLabel, g);
		
		g.gridy++;
		p.add(firstEquityPanel, g);
		
		g.gridy++;
		p.add(firstRankPanel, g);
		
		g.gridy++;
		p.add(secondEquityPanel, g);
		
		g.gridy++;
		p.add(secondRanksPanel, g);
		
		g.gridy++;
		p.add(thirdEquityPanel, g);
		
		g.gridy++;
		p.add(thirdRanksPanel, g);
		
		// add to superclass layout
		addDetails(p, below);
	}
	
	private void clearHandEquity() {
		totalLabel.setText("");
		firstEquityPanel.clearHandEquity();
		firstRankPanel.clearHandEquity();
		secondEquityPanel.clearHandEquity();
		secondEquityPanel.setVisible(false);
		secondRanksPanel.clearHandEquity();
		secondRanksPanel.setVisible(false);
		thirdEquityPanel.clearHandEquity();
		thirdEquityPanel.setVisible(false);
		thirdRanksPanel.clearHandEquity();
		thirdRanksPanel.setVisible(false);
	}

	public void setHandEquity(MEquity me) {
		clearHandEquity();
		
		if (me != null) {
			firstEquityPanel.setHandEquity(me, me.eqs[0]);
			firstRankPanel.setHandEquity(me.eqs[0]);
			
			if (me.eqs.length >= 2) {
				secondEquityPanel.setHandEquity(me, me.eqs[1]);
				secondEquityPanel.setVisible(true);
				secondRanksPanel.setHandEquity(me.eqs[1]);
				secondRanksPanel.setVisible(true);
			}
			
			if (me.eqs.length >= 3) {
				thirdEquityPanel.setHandEquity(me, me.eqs[2]);
				thirdEquityPanel.setVisible(true);
				thirdRanksPanel.setHandEquity(me.eqs[2]);
				thirdRanksPanel.setVisible(true);
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



