package pet.ui.ta;

import java.util.*;

import pet.eq.*;
import pet.hp.*;
import pet.hp.state.*;

public class HandStateTableModel extends MyTableModel<HandState> {

	private static final List<MyColumn<HandState>> cols = new ArrayList<MyColumn<HandState>>();
	
	static {
		cols.add(new HandStateColumn(String.class, "Player", "Player name") {
			@Override
			public String getValue(HandState hs) {
				if (hs.actionSeat >= 0) {
					return hs.seats[hs.actionSeat].seat.name;
				} else {
					return hs.note;
				}
			}
		});
		cols.add(new HandStateColumn(String.class, "Pot/Stack", "Player stack and SPR or pot") {
			@Override
			public String getValue(HandState h) {
				if (h.actionSeat >= 0) {
					SeatState ss = h.seats[h.actionSeat];
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
			public String getValue(HandState h) {
				if (h.actionSeat >= 0) {
					SeatState ss = h.seats[h.actionSeat];
					if (ss.acts <= 1) {
						return PokerUtil.cardsString(ss.hole);
					} else {
						return "";
					}
				} else {
					return PokerUtil.cardsString(h.board);
				}
			}
			@Override
			public String getToolTip(HandState hs) {
				if (hs.actionSeat >= 0) {
					SeatState ss = hs.seats[hs.actionSeat];
					if (ss.meq != null) {
						return MEquityUtil.currentString(ss.meq);
					}
				}
				return null;
			}
		});
		cols.add(new HandStateColumn(String.class, "Action", "Player action") {
			@Override
			public String getValue(HandState h) {
				if (h.actionSeat >= 0) {
					SeatState ss = h.seats[h.actionSeat];
					String v = HandUtil.actionString(h.hand, h.action);
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
			public String getValue(HandState h) {
				if (h.actionSeat >= 0) {
					SeatState ss = h.seats[h.actionSeat];
					if (ss.meq != null && ss.acts <= 1) {
						return MEquityUtil.equityString(ss.meq);
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
