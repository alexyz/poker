package pet.ui.eq;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

import pet.eq.*;

/**
 * extends card panel to show hand equity statistics
 */
class HandCardPanel extends CardPanel {
	
	private final TotalPanel totalPanel = new TotalPanel();
	private final AllPanels[] panels = new AllPanels[3];
	
	/**
	 * create hand card panel with name and number of cards, with the equity
	 * information optionally positioned either to the right or below the cards
	 */
	public HandCardPanel(String name, int mincards, int maxcards, boolean detailsBelow) {
		super(name, mincards, maxcards);
		
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints g = new GridBagConstraints();
		g.gridx = 0;
		g.fill = GridBagConstraints.HORIZONTAL;
		g.weightx = 1;
		
		g.gridy = 0;
		p.add(totalPanel, g);
		
		for (int n = 0; n < 3; n++) {
			panels[n] = new AllPanels();
			g.gridy++;
			p.add(panels[n], g);
		}
		
		// add to superclass layout
		addDetails(p, detailsBelow);
		
		clearEquity();
	}
	
	private void clearEquity() {
		totalPanel.clearEquity();
		for (int n = 0; n < 3; n++) {
			AllPanels p = panels[n];
			p.setBorder(null);
			p.equityPanel.clearEquity();
			p.rankPanel.clearEquity();
			p.outsPanel.clearEquity();
			p.equityPanel.setVisible(false);
			p.rankPanel.setVisible(false);
			p.outsPanel.setVisible(false);
		}
	}
	
	public void setEquity(MEquity me) {
		clearEquity();
		
		if (me != null) {
			totalPanel.setEquity(me);
			for (int n = 0; n < me.eqs.length; n++) {
				Equity e = me.eqs[n];
				AllPanels p = panels[n];
				p.setBorder(e.curwin ? new LineBorder(Color.green) : e.curtie ? new LineBorder(Color.yellow) : null);
				p.equityPanel.setEquity(e);
				p.equityPanel.setVisible(true);
				p.rankPanel.setEquity(e);
				p.rankPanel.setVisible(true);
				p.outsPanel.setEquity(e, me.remCards);
			}
		}
		
	}
	
	@Override
	public void clearCards() {
		super.clearCards();
		clearEquity();
	}
	
}

class AllPanels extends JPanel {
	final EquityPanel equityPanel = new EquityPanel();
	final RanksPanel rankPanel = new RanksPanel();
	final OutsPanel outsPanel = new OutsPanel();
	public AllPanels() {
		super(new GridLayout(3,1));
		add(equityPanel);
		add(rankPanel);
		add(outsPanel);
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
		totalLabel.setText(String.format("Equity: %.1f%%", me.totaleq) + (me.exact ? "" : " ~"));
		scoopLabel.setText(String.format("Scoop: %.1f%%", me.scoop));
		// XXX only display for hi/lo
		lowPossibleLabel.setText(String.format("Low poss: %.1f%%", me.lowPossible));
	}
}


