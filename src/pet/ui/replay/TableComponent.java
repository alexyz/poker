package pet.ui.replay;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.text.DateFormat;
import java.util.*;
import java.util.List;

import javax.swing.JComponent;

import pet.eq.*;
import pet.hp.*;
import pet.hp.state.*;
import pet.ui.PokerFrame;

/**
 * Draws a table in the given state
 */
class TableComponent extends JComponent {
	
	private HandState hs;
	private Hand hand;

	public TableComponent() {
		//
	}

	public void setState(HandState state) {
		System.out.println("state now " + state);
		this.hs = state;
		repaint();
	}
	
	public void setHand(Hand hand) {
		this.hand = hand;
	}

	/**
	 * get angle of seat from centre
	 */
	private double seatAngle(int seat, int max) {
		double angle = Math.PI;
		if (seat > 0) {
			angle = angle + ((seat * 2 * Math.PI) / max);
		}
		return angle;
	}

	/**
	 * get x position of seat centre with given radius from centre
	 */
	private double seatX(double angle, double radius) {
		// centre of seat
		double width = getWidth();
		double seatx = Math.sin(angle) * (width * radius * 0.5) + (width * 0.5);
		return seatx;
	}

	private double seatY(double angle, double radius) {
		// centre of seat
		double height = getHeight();
		double seaty = Math.cos(angle + Math.PI) * (height * radius * 0.5) + (height * 0.5);
		return seaty;
	}

	@Override
	protected void paintComponent(Graphics g) {
		final int w = getWidth();
		final int h = getHeight();
		final Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// table
		{
			Color lg = new Color(0x18a020);
			Color dg = new Color(0x085010);
			GradientPaint p = new GradientPaint(w * 0.1f, h * 0.1f, dg, w * 0.5f, h * 0.5f, lg, true);
			g2.setPaint(p);
			//g2.setColor(Color.green);
			Shape table = new Ellipse2D.Double(w * 0.1, h * 0.1, w * 0.8, h * 0.8);
			g2.fill(table);
			g2.setPaint(null);
			g2.setColor(Color.black);
			g2.setStroke(new BasicStroke(5));
			g2.draw(table);
		}
		
		if (hand != null) {
			g2.setColor(Color.black);
			g2.setFont(PokerFrame.boldfont);
			g2.drawString(hand.game.id, 18, 18);
			g2.drawString(String.valueOf(hand.tablename), 18, 36);
			g2.drawString(DateFormat.getDateTimeInstance().format(new Date(hand.date)), 18, 52);
		}

		if (hs == null)
			return;

		// pot information
		{
			int btx = w / 2;
			int bty = h / 2 - 48;
			g2.setFont(PokerFrame.bigfont);
			FontMetrics fm = g2.getFontMetrics();
			String noteStr = String.valueOf(hs.note);
			g2.drawString(noteStr, btx - fm.stringWidth(noteStr) / 2, bty);
			String boardStr = hs.board != null ? PokerUtil.cardsString(hs.board) : "";
			g2.drawString(boardStr, btx - fm.stringWidth(boardStr) / 2, bty + 36);
			String potStr = GameUtil.formatMoney(hand.game.currency, hs.pot);
			g2.drawString(potStr, btx - fm.stringWidth(potStr) / 2, bty + 72);
		}

		int max = hs.seats.length;
		for (int s = 0; s < max; s++) {
			// can be null
			SeatState ss = hs.seats[s];

			// seat angle
			double angle = seatAngle(s, max);

			// seat colour
			Color seatCol;
			if (ss == null) {
				seatCol = Color.darkGray;
			} else if (ss.folded) {
				seatCol = Color.gray;
			} else if (hs.actionSeatIndex == s) {
				seatCol = Color.yellow;
			} else if (ss.won) {
				seatCol = Color.orange;
			} else {
				seatCol = Color.white;
			}

			// distance from centre of table (0-0.5)
			//double seatdis = 0.35;
			// centre of seat
			//double seatx = Math.sin(angle) * (w * seatdis) + (w * 0.5);
			//double seaty = Math.cos(angle + Math.PI) * (h * seatdis) + (h * 0.5);
			double seatx = seatX(angle, 0.7);
			double seaty = seatY(angle, 0.7);
			g2.setColor(seatCol);
			// seat radius (0-0.5)
			double seatrad = 0.1;
			Shape seat = new Ellipse2D.Double(seatx - w * seatrad, seaty - h * seatrad, w * seatrad * 2, h * seatrad * 2);
			g2.fill(seat);
			g2.setColor(Color.black);
			g2.setStroke(new BasicStroke(2));
			g2.draw(seat);

			// seat information
			
			if (ss != null) {
				
				List<String> lines = new ArrayList<>();
				lines.add(ss.seat.name);
				lines.add(GameUtil.formatMoney(hand.game.currency, ss.stack));
				if (!ss.folded || ss.cardsState != null) {
					lines.add(ss.cardsState != null ? PokerUtil.cardsString(ss.cardsState.cards) : GameUtil.unknownCardsString(hs.hand.game.type));
				}
				if (ss.meq != null) {
					if (ss.meq.eqs[0].current != 0) {
						lines.add(MEquityUtil.currentString(ss.meq));
					}
					lines.add(MEquityUtil.equityString(ss.meq));
				}
				if (ss.spr > 0) {
					lines.add(String.format("SPR: %2.1f", ss.spr));
				}
				if (hs.actionSeatIndex == s || ss.won) {
					lines.add(ss.won ? "wins" : hs.actionString());
				}
				
				{
					int texty = (int) (seaty - (lines.size() * 8) - 4);
					g2.setFont(PokerFrame.boldfont);
					FontMetrics fm = g2.getFontMetrics();
					for (String str : lines) {
						g2.drawString(str, (int) seatx - fm.stringWidth(str) / 2, texty += 16);
					}
				}
				
				if (ss.amount > 0) {
					double betx = seatX(angle, 0.36);
					double bety = seatY(angle, 0.36);
					g2.setColor(Color.black);
					g2.setFont(PokerFrame.boldfont);
					String amountStr = GameUtil.formatMoney(hand.game.currency, ss.amount);
					if (ss.bpr != 0) {
						amountStr = String.format("%s (%2.1f%% pot)", amountStr, ss.bpr);
					}
					FontMetrics fm = g2.getFontMetrics();
					g2.drawString(amountStr, (int) betx - fm.stringWidth(amountStr) / 2, (int) bety);
				}
			}

			// seat button
			if (hs.buttonIndex == s) {
				g2.setColor(Color.gray);
				double butdis = 0.25;
				double ao = Math.PI / 9;
				double butx = Math.sin(angle - ao) * (w * butdis) + (w * 0.5);
				double buty = Math.cos(angle + Math.PI - ao) * (h * butdis) + (h * 0.5);
				double butrad = 0.02;
				Shape but = new Ellipse2D.Double(butx - w * butrad, buty - h * butrad, w * butrad * 2, h * butrad * 2);
				g2.fill(but);
				g2.setColor(Color.black);
				g2.setFont(PokerFrame.boldfont);
				g2.drawString("D", (int) butx - 5, (int) buty + 5);
			}
		}
	}

}