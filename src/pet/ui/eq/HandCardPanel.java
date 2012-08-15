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
	public static List<String[]> getCards(HandCardPanel[] cps) {
		List<String[]> hands = new ArrayList<String[]>();
		for (HandCardPanel cp : cps) {
			List<String> cards = cp.getCards();
			if (cards.size() > 0) {
				if (cards.size() < cp.getMinCards()) {
					System.out.println("not enough cards for " + cp);
					return null;
				}
				hands.add(cards.toArray(new String[cards.size()]));
			}
		}
		System.out.println("hands: " + hands.size());
		return hands.size() == 0 ? null : hands;
	}
	
	private final TotalPanel totalPanel = new TotalPanel();
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
		p.add(totalPanel, g);
		
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
		totalPanel.clearEquity();
		for (int n = 0; n < 3; n++) {
			equityPanels[n].clearEquity();
			rankPanels[n].clearEquity();
			outsPanels[n].clearEquity();
			equityPanels[n].setVisible(false);
			rankPanels[n].setVisible(false);
			outsPanels[n].setVisible(false);
		}
	}
	
	public void setEquity(MEquity me) {
		clearEquity();
		
		if (me != null) {
			totalPanel.setEquity(me);
			for (int n = 0; n < me.eqs.length; n++) {
				equityPanels[n].setEquity(me, me.eqs[n]);
				equityPanels[n].setVisible(true);
				rankPanels[n].setEquity(me.eqs[n]);
				rankPanels[n].setVisible(true);
				outsPanels[n].setEquity(me.eqs[n], me.remCards);
			}
		}
		
	}
	
	@Override
	public void clearCards() {
		super.clearCards();
		clearEquity();
	}
	
}

class TotalPanel extends JPanel {
	private final JLabel totalLabel = new JLabel();
	private final JLabel scoopLabel = new JLabel();
	private final JLabel lowPossibleLabel = new JLabel();
	public TotalPanel() {
		super(new GridLayout(1,3));
		add(totalLabel);
		add(scoopLabel);
		add(lowPossibleLabel);
	}
	void clearEquity() {
		totalLabel.setText("");
		scoopLabel.setText("");
		lowPossibleLabel.setText("");
	}
	void setEquity(MEquity me) {
		totalLabel.setText(String.format("Equity: %.1f%%", me.totaleq));
		scoopLabel.setText(String.format("Scoop: %.1f%%", me.scoop));
		lowPossibleLabel.setText(String.format("Low possible: %.1f%%", me.lowPossible));
	}
}


