package pet.ui.table;

import java.awt.Font;
import java.util.*;

import pet.eq.*;
import pet.hp.*;
import pet.hp.state.*;

/**
 * shows hand states in a table. each row is either board/pot if there is no
 * action seat, or the player/stack/holecards/equity if there is an action seat.
 */
public class HandStateTableModel extends MyTableModel<HandState> {
	
	private static final List<MyColumn<HandState>> cols = new ArrayList<>();
	
	private static final MyColumn<HandState> playerNameCol = new HandStateColumn(String.class, "Player", "Player name") {
		@Override
		public String getValue(HandState hs) {
			SeatState ss = hs.actionSeat();
			if (ss != null) {
				return ss.seat.name;
			} else {
				return hs.note;
			}
		}
	};
	
	private static final MyColumn<HandState> potStackCol = new HandStateColumn(String.class, "Pot/Stack", "Player stack and SPR or pot") {
		@Override
		public String getValue(HandState h) {
			SeatState ss = h.actionSeat();
			if (ss != null) {
				String v = GameUtil.formatMoney(h.hand.game.currency, ss.stack);
				if (ss.spr > 0) {
					v += String.format(" (spr %2.1f)", ss.spr);
				}
				return v;
				
			} else {
				return "Pot: " + GameUtil.formatMoney(h.hand.game.currency, h.pot);
			}
		}
	};
	
	private static final MyColumn<HandState> holeBoardCol = new HandStateColumn(String.class, "Board/Cards", "Board, player hole cards or up cards") {
		@Override
		public String getValue(HandState hs) {
			SeatState ss = hs.actionSeat();
			if (ss == null) {
				// show board (could be null)
				return PokerUtil.cardsString(hs.board);
			}
			
			// show hole cards if first action
			// this does make the draw actions a bit confusing as it
			// displays the hand after, not before the draw
			// but there might not be any other actions to display the
			// equity against
			
			if (ss.actionNum == 1) {
				if (ss.cardsState != null) {
					if (ss.cardsState.guess) {
						return "(" + PokerUtil.cardsString(ss.cardsState.cards) + ")";
						
					} else {
						String s = PokerUtil.cardsString(ss.cardsState.cards);
						if (ss.cardsState.discarded != null && ss.cardsState.discarded.length > 0) {
							s += " (" + PokerUtil.cardsString(ss.cardsState.discarded) + ")";
						}
						return s;
					}
				}
			}
			
			return "";
		}
		@Override
		public String getToolTip(HandState hs) {
			SeatState ss = hs.actionSeat();
			if (ss == null) {
				return null;
			}
			
			StringBuilder sb = new StringBuilder();
			sb.append("<html>");
			if (ss.meq != null) {
				sb.append(MEquityUtil.currentString(ss.meq));
			}
			if (ss.cardsState != null && ss.cardsState.suggestedDraws != null) {
				// suggested draws
				sb.append("<table><tr><th>Hand</th><th>Score</th></tr>");
				for (Draw d : ss.cardsState.suggestedDraws) {
					boolean e = Arrays.equals(ss.cardsState.cards, d.cards);
					sb.append("<tr><td>");
					if (e) {
						sb.append("<u>");
					}
					sb.append(PokerUtil.cardsString(d.cards));
					if (e) {
						sb.append("</u>");
					}
					sb.append("</td>");
					sb.append("<td>").append(d.score).append("</td></tr>");
				}
				sb.append("</table>");
			}
			return sb.toString();
		}
		@Override
		public Font getFont (HandState hs) {
			return MyJTable.monoTableFont;
		}
	};
	
	private static final MyColumn<HandState> actCol = new HandStateColumn(String.class, "Action", "Player action") {
		@Override
		public String getValue(HandState hs) {
			SeatState ss = hs.actionSeat();
			if (ss != null) {
				String v = hs.actionString();
				if (ss.amount > 0) {
					if (ss.bpr > 0) {
						v += String.format(" (%2.1f%%)", ss.bpr);
					}
				}
				return v;
			} else {
				return "";
			}
		}
	};
	
	private static final MyColumn<HandState> eqCol = new HandStateColumn(String.class, "Equity", "Hand equity (total, hi, lo)") {
		@Override
		public String getValue(HandState hs) {
			if (hs.hand.showdown) {
				SeatState ss = hs.actionSeat();
				if (ss != null) {
					if (ss.meq != null && ss.actionNum == 1) {
						return MEquityUtil.equityString(ss.meq);
					}
				}
			}
			return "";
		}
		@Override
		public String getToolTip(HandState hs) {
			StringBuilder sb = new StringBuilder();
			SeatState ss = hs.actionSeat();
			if (ss != null) {
				if (hs.hand.showdown && ss.meq != null) {
					sb.append("<html>");
					
					sb.append("<table><tr><th>Total</th><td>").append(pc(ss.meq.totaleq)).append("</td></tr>");
					sb.append("<tr><th>Scoop</th><td>").append(pc(ss.meq.scoop)).append("</td></tr>");
					if (ss.meq.hilo) {
						sb.append("<tr><th>Low possible</th><td>").append(pc(ss.meq.lowPossible)).append("</td></tr>");
					}
					sb.append("<tr><th>Exact</th><td>").append(ss.meq.exact).append("</td></tr>");
					//sb.append("<tr><th>Remaining cards</th><td>").append(ss.meq.remCards).append("</td></tr>");
					sb.append("</table>");
					
					//sb.append("<hr>");
					
					sb.append("<table><tr><th></th>");
					sb.append("<th>Win</th>");
					sb.append("<th>Tie</th></tr>");
					for (Equity e : ss.meq.eqs) {
						sb.append("<tr><th>").append(e.type.desc).append("</th>");
						sb.append("<td>").append(pc(e.won)).append("</td>");
						sb.append("<td>").append(pc(e.tied)).append("</td></tr>");
					}
					sb.append("</table>");
					
					sb.append("</html>");
					
				} else {
					// either 100 or 0
					sb.append("Default equity:  ").append(ss.deq).append("%");
				}
			}
			//System.out.println(sb);
			return sb.toString();
		}
		private String pc(float f) {
			if (f != 0) {
				return "<b>" + String.format("%.1f", f) + "%</b>";
			} else {
				return "0.0%";
			}
		}
	};
	
	private static final MyColumn<HandState> evCol = new HandStateColumn(String.class, "EV", "Expected Value") {
		@Override
		public String getValue(HandState hs) {
			SeatState ss = hs.actionSeat();
			if (ss != null) {
				if (hs.action.type == Action.Type.COLLECT && ss.tev != 0) {
					return GameUtil.formatMoney(hs.hand.game.currency, (int) ss.tev);
				} else if (ss.ev != 0) {
					return GameUtil.formatMoney(hs.hand.game.currency, (int) ss.ev);
				}
			}
			return "";
		}
	};
	
	static {
		cols.add(playerNameCol);
		cols.add(potStackCol);
		cols.add(holeBoardCol);
		cols.add(actCol);
		cols.add(eqCol);
		cols.add(evCol);
	}
	
	public HandStateTableModel() {
		super(cols);
	}
	
}
