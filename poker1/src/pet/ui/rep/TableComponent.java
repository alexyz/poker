package pet.ui.rep;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.text.DateFormat;

import javax.swing.JComponent;

import pet.eq.Poker;
import pet.eq.PokerUtil;
import pet.hp.Hand;

/**
 * Draws a table in the given state
 * TODO currency and number formatting
 * TODO show table information in top left
 */
class TableComponent extends JComponent {

	private static final Font normalfont = new Font("SansSerif", Font.PLAIN, 12);
	private static final Font boldfont = new Font("SansSerif", Font.BOLD, 12);
	private static final Font centrefont = new Font("SansSerif", Font.BOLD, 24);
	
	private HandState state;
	private Hand hand;

	public TableComponent() {
		//
	}

	public void setState(HandState state) {
		System.out.println("state now " + state);
		this.state = state;
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
			g2.setColor(Color.green);
			Shape table = new Ellipse2D.Double(w * 0.1, h * 0.1, w * 0.8, h * 0.8);
			//g2.fillOval(w / 10, h / 10, w * 8 / 10, h * 8 / 10);
			g2.fill(table);
			g2.setColor(Color.black);
			g2.setStroke(new BasicStroke(5));
			//g2.drawOval(w / 10, h / 10, w * 8 / 10, h * 8 / 10);
			g2.draw(table);
		}
		
		if (hand != null) {
			g2.setColor(Color.black);
			g2.setFont(normalfont);
			g2.drawString(String.valueOf(hand.gamename), 18, 18);
			g2.drawString(String.valueOf(hand.tablename), 18, 36);
			g2.drawString(DateFormat.getDateTimeInstance().format(hand.date), 18, 52);
		}

		if (state == null)
			return;

		// pot information
		{
			int btx = w / 2;
			int bty = h / 2 - 48;
			g2.setFont(centrefont);
			FontMetrics fm = g2.getFontMetrics();
			String noteStr = String.valueOf(state.note);
			g2.drawString(noteStr, btx - fm.stringWidth(noteStr) / 2, bty);
			String boardStr = state.board != null ? PokerUtil.cardsString(state.board) : "";
			g2.drawString(boardStr, btx - fm.stringWidth(boardStr) / 2, bty + 36);
			String potStr = String.valueOf(state.pot);
			g2.drawString(potStr, btx - fm.stringWidth(potStr) / 2, bty + 72);
		}

		int max = state.seats.length;
		for (int s = 0; s < max; s++) {
			// can be null
			SeatState ss = state.seats[s];

			// seat angle
			double angle = seatAngle(s, max);

			// seat colour
			Color seatCol;
			if (ss == null) {
				seatCol = Color.darkGray;
			} else if (ss.folded) {
				seatCol = Color.gray;
			} else if (state.actionSeat == s) {
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
				int textx = (int) seatx;
				int texty = (int) (seaty - 45); // TODO centre
				g2.setFont(normalfont);
				FontMetrics fm = g2.getFontMetrics();
				
				String nameStr = String.valueOf(ss.seat.name);
				g2.drawString(nameStr, textx - fm.stringWidth(nameStr) / 2, texty += 15);
				
				String stackStr = String.valueOf(ss.stack);
				g2.drawString(stackStr, textx - fm.stringWidth(stackStr) / 2, texty += 15);
				
				String holeStr = ss.hole != null ? PokerUtil.cardsString(ss.hole) : "[unknown]";
				g2.drawString(holeStr, textx - fm.stringWidth(holeStr) / 2, texty += 15);
				
				String valueStr = ss.eq != null ? Poker.valueString(ss.eq.current) : "";
				g2.drawString(valueStr, textx - fm.stringWidth(valueStr) / 2, texty += 15);
				
				String eqStr = "Equity: " + (ss.eq != null ? ss.eq.won + ss.eq.tied : 0);
				g2.drawString(eqStr, textx - fm.stringWidth(eqStr) / 2, texty += 15);
				
				if (state.actionSeat == s || ss.won) {
					g2.setFont(boldfont);
					fm = g2.getFontMetrics();
					String actStr = String.valueOf(ss.won ? "wins" : state.action);
					g2.drawString(actStr, textx - fm.stringWidth(actStr) / 2, texty += 15);
				}

				if (ss.bet > 0) {
					double betx = seatX(angle, 0.4);
					double bety = seatY(angle, 0.4);
					g2.setColor(Color.black);
					g2.setFont(normalfont);
					g2.drawString(String.valueOf(ss.bet), (int) betx, (int) bety);
				}
			}

			// seat button
			if (state.button == s) {
				g2.setColor(Color.gray);
				double butdis = 0.25;
				double ao = Math.PI / 9;
				double butx = Math.sin(angle - ao) * (w * butdis) + (w * 0.5);
				double buty = Math.cos(angle + Math.PI - ao) * (h * butdis) + (h * 0.5);
				double butrad = 0.02;
				Shape but = new Ellipse2D.Double(butx - w * butrad, buty - h * butrad, w * butrad * 2, h * butrad * 2);
				g2.fill(but);
				g2.setColor(Color.black);
				g2.setFont(normalfont);
				g2.drawString("D", (int) butx - 5, (int) buty + 5);
			}
		}
	}

}