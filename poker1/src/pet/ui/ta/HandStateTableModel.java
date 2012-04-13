package pet.ui.ta;

import java.awt.Color;
import java.awt.Font;
import java.util.*;

import pet.eq.Poker;
import pet.eq.PokerUtil;
import pet.hp.Action;
import pet.hp.GameUtil;
import pet.hp.HandUtil;
import pet.hp.state.*;

public class HandStateTableModel extends MyTableModel<HandState> {

	private static final List<MyTableModelColumn<HandState,?>> cols = new ArrayList<MyTableModelColumn<HandState,?>>();
	
	static {
		cols.add(new HandStateTableModelColumn<HandState,String>(String.class, "Player", "Player name") {
			@Override
			public String getValue(HandState hs) {
				if (hs.actionSeat >= 0) {
					return hs.seats[hs.actionSeat].seat.name;
				} else {
					return hs.note;
				}
			}
		});
		cols.add(new HandStateTableModelColumn<HandState,String>(String.class, "Pot/Stack", "Player stack and SPR or pot") {
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
		cols.add(new HandStateTableModelColumn<HandState,String>(String.class, "Board/Hole", "Players hole cards or board") {
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
					if (ss.eq != null) {
						return Poker.valueString(ss.eq.current);
					}
				}
				return null;
			}
		});
		cols.add(new HandStateTableModelColumn<HandState,String>(String.class, "Action", "Player action") {
			@Override
			public String getValue(HandState h) {
				if (h.actionSeat >= 0) {
					SeatState ss = h.seats[h.actionSeat];
					String v = HandUtil.actionString(h.hand, h.action);
					if (ss.amount > 0) {
						if (ss.bpr > 0) {
							v += String.format(" (%2.1f%% p)", ss.bpr);
						}
					}
					return v;
				} else {
					return "";
				}
			}
		});
		cols.add(new HandStateTableModelColumn<HandState,String>(String.class, "Equity", "Hand equity") {
			@Override
			public String getValue(HandState h) {
				if (h.actionSeat >= 0) {
					SeatState ss = h.seats[h.actionSeat];
					if (ss.eq != null && ss.acts <= 1) {
						String v = String.format("%2.1f%%", ss.eq.won);
						if (ss.eq.tied > 0) {
							v += String.format(" (%2.1f%% T)", ss.eq.tied);
						}
						return v;
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


abstract class HandStateTableModelColumn<T,S> extends MyTableModelColumn<HandState,S> {

	private static final Color playerColour = new Color(192, 255, 192);
	private static final Color winColour = new Color(255, 255, 128);
	private static final Font boldfont = MyJTable.deffont.deriveFont(Font.BOLD);
	
	public HandStateTableModelColumn(Class<S> cl, String name, String desc) {
		super(cl, name, desc);
	}
	
	@Override
	public Color getColour(HandState hs) {
		if (hs.actionSeat == -1) {
			return Color.lightGray;
			
		} else if (hs.action.type == Action.COLLECT_TYPE) {
			return winColour;
			
		} else if (hs.seats[hs.actionSeat].seat == hs.hand.myseat) {
			return playerColour;
		} 
		
		return null;
	}
	
	@Override
	public Font getFont(HandState hs) {
		if (hs.actionSeat == -1) {
			return boldfont;
		} 
		return null;
	}
	
}
