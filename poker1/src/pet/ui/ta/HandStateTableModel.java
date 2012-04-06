package pet.ui.ta;

import java.util.*;

import pet.eq.PokerUtil;
import pet.hp.state.*;

public class HandStateTableModel extends MyTableModel<HandState> {

	private static final List<MyTableModelColumn<HandState,?>> cols = new ArrayList<MyTableModelColumn<HandState,?>>();
	
	static {
		cols.add(new MyTableModelColumn<HandState,String>(String.class, "Player", "Player name") {
			@Override
			public String getValue(HandState h) {
				if (h.actionSeat >= 0) {
					return h.seats[h.actionSeat].seat.name;
				} else {
					return h.note;
				}
			}
		});
		cols.add(new MyTableModelColumn<HandState,String>(String.class, "Hole", "Players hole cards or board") {
			@Override
			public String getValue(HandState h) {
				if (h.actionSeat >= 0) {
					return PokerUtil.cardsString(h.seats[h.actionSeat].seat.hole);
				} else {
					return PokerUtil.cardsString(h.board);
				}
			}
		});
		cols.add(new MyTableModelColumn<HandState,String>(String.class, "Stack (SPR)", "Player stack and SPR or pot") {
			@Override
			public String getValue(HandState h) {
				if (h.actionSeat >= 0) {
					SeatState ss = h.seats[h.actionSeat];
					String v = ss.seat.name;
					if (ss.spr > 0) {
						v += " (" + ss.spr + ")";
					}
					return v;
					
				} else {
					return "Pot: " + h.pot;
				}
			}
		});
		cols.add(new MyTableModelColumn<HandState,String>(String.class, "Action", "Player action") {
			@Override
			public String getValue(HandState h) {
				if (h.actionSeat >= 0) {
					SeatState ss = h.seats[h.actionSeat];
					String v = h.action;
					if (ss.amount > 0) {
						v += " " + ss.amount;
						if (ss.bpr > 0) {
							v += " (" + ss.bpr + " pot)";
						}
					}
					return v;
				} else {
					return "";
				}
			}
		});
		cols.add(new MyTableModelColumn<HandState,String>(String.class, "Equity", "Hand equity") {
			@Override
			public String getValue(HandState h) {
				if (h.actionSeat >= 0) {
					SeatState ss = h.seats[h.actionSeat];
					String v = "" + ss.eq.won;
					if (ss.eq.tied > 0) {
						v += " (" + ss.eq.tied + " tie)";
					}
					return v;
					
				} else {
					return "";
				}
			}
		});
	}
	
	public HandStateTableModel() {
		super(cols);
	}

}
