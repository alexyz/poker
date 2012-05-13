package pet.ui.eq;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.LineBorder;

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
	private final RanksPanel highRanks = new RanksPanel();
	private final EquityPanel hiEquityPanel = new EquityPanel();
	private final EquityPanel loEquityPanel = new EquityPanel();

	public HandCardPanel(String name, int mincards, int maxcards) {
		super(name, mincards, maxcards);
		hiEquityPanel.setVisible(false);
		loEquityPanel.setVisible(false);
		
		JPanel p = new JPanel(new GridBagLayout());
		p.setBorder(new LineBorder(Color.green));
		GridBagConstraints g = new GridBagConstraints();
		g.gridx = 0;
		g.fill = GridBagConstraints.HORIZONTAL;
		g.weightx = 1;
		
		totalLabel.setBorder(new LineBorder(Color.red));
		g.gridy = 0;
		p.add(totalLabel, g);
		
		hiOnlyEquityPanel.setBorder(new LineBorder(Color.red));
		g.gridy++;
		p.add(hiOnlyEquityPanel, g);
		
		highRanks.setBorder(new LineBorder(Color.red));
		g.gridy++;
		p.add(highRanks, g);
		
		hiEquityPanel.setBorder(new LineBorder(Color.red));
		g.gridy++;
		p.add(hiEquityPanel, g);
		
		loEquityPanel.setBorder(new LineBorder(Color.red));
		g.gridy++;
		p.add(loEquityPanel, g);
		
		// add to superclass layout
		addDetails(p);
	}
	
	private void clearHandEquity() {
		hiOnlyEquityPanel.clearHandEquity();
		highRanks.clearHandEquity();
		hiEquityPanel.clearHandEquity();
		hiEquityPanel.setVisible(false);
		loEquityPanel.clearHandEquity();
		loEquityPanel.setVisible(false);
		totalLabel.setText("");
	}

	public void setHandEquity(MEquity me) {
		clearHandEquity();
		if (me != null) {
			if (me.hionly() != null) {
				hiOnlyEquityPanel.setHandEquity(me, me.hionly());
				highRanks.setHandEquity(me.hionly());
			}
			if (me.hihalf() != null) {
				hiEquityPanel.setHandEquity(me, me.hihalf());
				hiEquityPanel.setVisible(true);
			}
			if (me.lohalf() != null) {
				loEquityPanel.setHandEquity(me, me.lohalf());
				loEquityPanel.setVisible(true);
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



