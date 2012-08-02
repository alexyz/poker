package pet.ui.ta;

import java.util.*;

import pet.eq.*;
import pet.hp.*;
import pet.hp.state.*;

/**
 * shows hand states in a table. each row is either board/pot if there is no
 * action seat, or the player/stack/holecards/equity if there is an action seat.
 */
public class HandStateTableModel extends MyTableModel<HandState> {
	
	private static final List<MyColumn<HandState>> cols = new ArrayList<MyColumn<HandState>>();
	
	static {
		cols.add(new HandStateColumn(String.class, "Player", "Player name") {
			@Override
			public String getValue(HandState hs) {
				SeatState ss = hs.actionSeat();
				if (ss != null) {
					return ss.seat.name;
				} else {
					return hs.note;
				}
			}
		});
		cols.add(new HandStateColumn(String.class, "Pot/Stack", "Player stack and SPR or pot") {
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
		});
		cols.add(new HandStateColumn(String.class, "Board/Hole", "Players hole cards or board") {
			@Override
			public String getValue(HandState hs) {
				SeatState ss = hs.actionSeat();
				if (ss == null) {
					return PokerUtil.cardsString(hs.board);
				}
				
				if (ss.actionNum > 1) {
					return "";
				}
				
				if (ss.holeObj != null) {
					if (ss.holeObj.guess) {
						return "(" + PokerUtil.cardsString(ss.holeObj.hole) + ")";
						
					} else {
						String s = PokerUtil.cardsString(ss.holeObj.hole);
						if (ss.holeObj.discarded != null) {
							s += " (" + PokerUtil.cardsString(ss.holeObj.discarded) + ")";
						}
						return s;
					}
				}
				
				return "";
			}
			@Override
			public String getToolTip(HandState hs) {
				SeatState ss = hs.actionSeat();
				if (ss != null) {
					if (ss.meq != null) {
						return MEquityUtil.currentString(ss.meq);
					}
				}
				return null;
			}
		});
		cols.add(new HandStateColumn(String.class, "Action", "Player action") {
			@Override
			public String getValue(HandState hs) {
				SeatState ss = hs.actionSeat();
				if (ss != null) {
					String v = HandUtil.actionString(hs.hand, hs.action);
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
		});
		cols.add(new HandStateColumn(String.class, "Equity", "Hand equity") {
			@Override
			public String getValue(HandState hs) {
				SeatState ss = hs.actionSeat();
				if (ss != null) {
					if (ss.meq != null && ss.actionNum <= 1) {
						return MEquityUtil.equityString(ss.meq);
					}
				}
				return "";
			}
		});
		cols.add(new HandStateColumn(String.class, "EV", "Expected Value") {
			@Override
			public String getValue(HandState hs) {
				SeatState ss = hs.actionSeat();
				if (ss != null) {
					if (hs.action.type == Action.COLLECT_TYPE && ss.tev != 0) {
						return GameUtil.formatMoney(hs.hand.game.currency, (int) ss.tev);
					} else if (ss.ev != 0) {
						return GameUtil.formatMoney(hs.hand.game.currency, (int) ss.ev);
					}
				}
				return "";
			}
		});
	}
	
	public HandStateTableModel() {
		super(cols);
	}
	
}
