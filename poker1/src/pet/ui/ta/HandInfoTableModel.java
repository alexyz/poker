package pet.ui.ta;

import java.util.*;

import pet.hp.info.*;

/**
 * table model for hands in a session
 */
public class HandInfoTableModel extends MyTableModel<HandInfo> {
	
	private static final List<MyTableModelColumn<HandInfo,?>> cols = new ArrayList<MyTableModelColumn<HandInfo,?>>();
	
	static {
		cols.add(new MyTableModelColumn<HandInfo,Date>(Date.class, "Date", "Date") {
			@Override
			public Date getValue(HandInfo o) {
				return o.hand.date;
			}
		});
		cols.add(new MyTableModelColumn<HandInfo,HoleInfo>(HoleInfo.class, "MyHole", "Final hole cards") {
			@Override
			public HoleInfo getValue(HandInfo o) {
				return o.hole;
			}
		});
		cols.add(new MyTableModelColumn<HandInfo,String[]>(String[].class, "Board", "Community cards") {
			@Override
			public String[] getValue(HandInfo o) {
				return o.hand.board;
			}
		});
		cols.add(new MyTableModelColumn<HandInfo,Integer>(Integer.class, "Seats", "Number of players") {
			@Override
			public Integer getValue(HandInfo o) {
				return o.hand.seats.length;
			}
		});
		/*
		cols.add(new MyTableModelColumn<HandInfo,Integer>(Integer.class, "MyPos", "My position") {
			@Override
			public Integer getValue(HandInfo o) {
				return o.mypos();
			}
		});
		*/
		cols.add(new MyTableModelColumn<HandInfo,String>(String.class, "MyPosDesc", "My position description (0=button)") {
			@Override
			public String getValue(HandInfo o) {
				return o.myposdesc();
			}
		});
		cols.add(new MyTableModelColumn<HandInfo,String>(String.class, "LastFlop", "Player was last to act on flop") {
			@Override
			public String getValue(HandInfo o) {
				int i = o.lastonflop();
				return i == 1 ? "Y" : i == -1 ? "N" : "";
			}
		});
		cols.add(new MyTableModelColumn<HandInfo,Integer>(Integer.class, "N-Flop", "Number of players to flop") {
			@Override
			public Integer getValue(HandInfo o) {
				return o.numtoflop();
			}
		});
		cols.add(new MyTableModelColumn<HandInfo,String>(String.class, "Show", "Show down") {
			@Override
			public String getValue(HandInfo o) {
				return o.hand.showdown ? "Y" : "";
			}
		});
		cols.add(new MyTableModelColumn<HandInfo,Integer>(Integer.class, "Pot", "Pot") {
			@Override
			public Integer getValue(HandInfo o) {
				return o.hand.pot;
			}
		});
		cols.add(new MyTableModelColumn<HandInfo,Integer>(Integer.class, "MyValue", "Amount won") {
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

