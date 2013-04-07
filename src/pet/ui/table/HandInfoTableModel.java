package pet.ui.table;

import java.awt.Font;
import java.util.*;

import pet.hp.GameUtil;
import pet.hp.info.*;

/**
 * table model for hands in a session
 */
public class HandInfoTableModel extends MyTableModel<HandInfo> {
	
	private static final List<MyColumn<HandInfo>> cols = new ArrayList<>();
	
	static {
		cols.add(new MyColumn<HandInfo>(Date.class, "Date", "Date") {
			@Override
			public Date getValue(HandInfo o) {
				return new Date(o.hand.date);
			}
		});
		cols.add(new MyColumn<HandInfo>(HoleCards.class, "Down", "Final down cards") {
			@Override
			public HoleCards getValue(HandInfo o) {
				return o.mydowncards();
			}
			@Override
			public Font getFont (HandInfo row) {
				return MyJTable.monoTableFont;
			}
		});
		cols.add(new MyColumn<HandInfo>(String[].class, "Board", "Community cards") {
			@Override
			public String[] getValue(HandInfo o) {
				return o.hand.board;
			}
			@Override
			public Font getFont (HandInfo row) {
				return MyJTable.monoTableFont;
			}
		});
		cols.add(new MyColumn<HandInfo>(Integer.class, "Seats", "Number of players") {
			@Override
			public Integer getValue(HandInfo o) {
				return o.hand.seats.length;
			}
		});
		cols.add(new MyColumn<HandInfo>(String.class, "MyPos", "My position description (0=button)") {
			@Override
			public String getValue(HandInfo o) {
				return o.myposdesc();
			}
		});
		cols.add(new MyColumn<HandInfo>(String.class, "LastFlop", "Player was last to act on flop") {
			@Override
			public String getValue(HandInfo o) {
				int i = o.lastonflop();
				return i == 1 ? "Y" : i == -1 ? "N" : "";
			}
		});
		cols.add(new MyColumn<HandInfo>(Integer.class, "N-Flop", "Number of players to flop") {
			@Override
			public Integer getValue(HandInfo o) {
				int n = o.numtoflop();
				return n != 0 ? n : null;
			}
		});
		cols.add(new MyColumn<HandInfo>(String.class, "Show", "Show down") {
			@Override
			public String getValue(HandInfo o) {
				if (o.hand.showdown) {
					if (GameUtil.isHilo(o.hand.game.type) && !o.hand.showdownNoLow) {
						return "Y+L";
					} else {
						return "Y";
					}
				}
				return "";
			}
		});
		cols.add(new MyColumn<HandInfo>(Integer.class, "Pot", "Pot") {
			@Override
			public Integer getValue(HandInfo o) {
				return o.hand.pot;
			}
		});
		cols.add(new MyColumn<HandInfo>(Integer.class, "MyValue", "Amount won") {
			@Override
			public Integer getValue(HandInfo o) {
				return o.myvalue();
			}
		});
		cols.add(new MyColumn<HandInfo>(HandValue.class, "Hand", "Players final hand") {
			@Override
			public HandValue getValue(HandInfo o) {
				return o.rank();
			}
		});
	}
	
	public HandInfoTableModel() {
		super(cols);
	}
}

