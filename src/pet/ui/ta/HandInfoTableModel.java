package pet.ui.ta;

import java.util.*;

import pet.hp.info.*;

/**
 * table model for hands in a session
 */
public class HandInfoTableModel extends MyTableModel<HandInfo> {
	
	private static final List<MyColumn<HandInfo>> cols = new ArrayList<MyColumn<HandInfo>>();
	
	static {
		cols.add(new MyColumn<HandInfo>(Date.class, "Date", "Date") {
			@Override
			public Date getValue(HandInfo o) {
				return o.hand.date;
			}
		});
		cols.add(new MyColumn<HandInfo>(HoleInfo.class, "MyHole", "Final hole cards") {
			@Override
			public HoleInfo getValue(HandInfo o) {
				return o.hole;
			}
		});
		cols.add(new MyColumn<HandInfo>(String[].class, "Board", "Community cards") {
			@Override
			public String[] getValue(HandInfo o) {
				return o.hand.board;
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
				return o.numtoflop();
			}
		});
		cols.add(new MyColumn<HandInfo>(String.class, "Show", "Show down") {
			@Override
			public String getValue(HandInfo o) {
				return o.hand.showdown ? "Y" : "";
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
		//, "pip", "af", "av"
		// handtype (ss, ds, P, 2P, (A)--1), floptype (P, F, S)
	}
	
	public HandInfoTableModel() {
		super(cols);
	}
}

