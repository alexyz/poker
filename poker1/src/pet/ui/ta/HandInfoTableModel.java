package pet.ui.ta;

import java.util.*;

import pet.hp.util.*;

/**
 * table model for hands in a session
 */
public class HandInfoTableModel extends MyTableModel<HandInfo> {
	
	private static final List<MyTableModelColumn<HandInfo,?>> cols = new ArrayList<MyTableModelColumn<HandInfo,?>>();
	
	static {
		cols.add(new MyTableModelColumn<HandInfo,Date>("Date", "Date", Date.class) {
			@Override
			public Date getValue(HandInfo o) {
				return o.hand.date;
			}
		});
		cols.add(new MyTableModelColumn<HandInfo,Hole>("MyHole", "Final hole cards", Hole.class) {
			@Override
			public Hole getValue(HandInfo o) {
				return o.hole;
			}
		});
		cols.add(new MyTableModelColumn<HandInfo,String[]>("Board", "Community cards", String[].class) {
			@Override
			public String[] getValue(HandInfo o) {
				return o.hand.board;
			}
		});
		cols.add(new MyTableModelColumn<HandInfo,Integer>("Seats", "Number of players", Integer.class) {
			@Override
			public Integer getValue(HandInfo o) {
				return o.hand.seats.length;
			}
		});
		cols.add(new MyTableModelColumn<HandInfo,Integer>("MyPos", "My position", Integer.class) {
			@Override
			public Integer getValue(HandInfo o) {
				return o.mypos();
			}
		});
		cols.add(new MyTableModelColumn<HandInfo,String>("Show", "Show down", String.class) {
			@Override
			public String getValue(HandInfo o) {
				return o.hand.showdown ? "Y" : "";
			}
		});
		cols.add(new MyTableModelColumn<HandInfo,Integer>("Pot", "Pot", Integer.class) {
			@Override
			public Integer getValue(HandInfo o) {
				return o.hand.pot;
			}
		});
		cols.add(new MyTableModelColumn<HandInfo,Integer>("MyValue", "Amount won", Integer.class) {
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

