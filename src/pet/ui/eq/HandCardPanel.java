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
	private final EquityPanel[] equityPanels = new EquityPanel[3];
	private final RanksPanel[] rankPanels = new RanksPanel[3];
	private final OutsPanel[] outsPanels = new OutsPanel[3];
	
	public HandCardPanel(String name, int mincards, int maxcards, boolean below) {
		super(name, mincards, maxcards);
		
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints g = new GridBagConstraints();
		g.gridx = 0;
		g.fill = GridBagConstraints.HORIZONTAL;
		g.weightx = 1;
		
		g.gridy = 0;
		p.add(totalLabel, g);
		
		for (int n = 0; n < 3; n++) {
			equityPanels[n] = new EquityPanel();
			rankPanels[n] = new RanksPanel();
			outsPanels[n] = new OutsPanel();
			
			g.gridy++;
			p.add(equityPanels[n], g);
			g.gridy++;
			p.add(rankPanels[n], g);
			g.gridy++;
			p.add(outsPanels[n], g);
		}
		
		// add to superclass layout
		addDetails(p, below);
		
		clearEquity();
	}
	
	private void clearEquity() {
		totalLabel.setText("");
		for (int n = 0; n < 3; n++) {
			equityPanels[n].clearEquity();
			rankPanels[n].clearEquity();
			outsPanels[n].clearEquity();
		}
		for (int n = 1; n < 3; n++) {
			equityPanels[n].setVisible(false);
			rankPanels[n].setVisible(false);
			outsPanels[n].setVisible(false);
		}
	}
	
	public void setEquity(MEquity me) {
		clearEquity();
		
		if (me != null) {
			totalLabel.setText("Total Equity: " + me.totaleq + " Low possible: " + me.lowPossible + " Scoop: " + me.scoop);
			for (int n = 0; n < me.eqs.length; n++) {
				equityPanels[n].setHandEquity(me, me.eqs[n]);
				equityPanels[n].setVisible(true);
				rankPanels[n].setEquity(me.eqs[n]);
				rankPanels[n].setVisible(true);
				outsPanels[n].setEquity(me.eqs[n], me.remCards);
				//outsPanels[n].setVisible(true);
			}
		}
		
	}
	
	@Override
	public void clearCards() {
		super.clearCards();
		clearEquity();
	}
	
}
