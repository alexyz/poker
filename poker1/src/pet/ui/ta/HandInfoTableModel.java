package pet.ui;

import java.util.*;

import javax.swing.table.AbstractTableModel;

import pet.hp.util.*;

/**
 * table model for hands in a session
 */
class SessionTableModel extends AbstractTableModel {
	private static final List<TableModelColumn<HandInfo,?>> cols = new ArrayList<TableModelColumn<HandInfo,?>>();
	static {
		cols.add(new TableModelColumn<HandInfo,Date>("Date", Date.class) {
			@Override
			public Date getValue(HandInfo o) {
				return o.hand.date;
			}
		});
		cols.add(new TableModelColumn<HandInfo,Hole>("MyHand", Hole.class) {
			@Override
			public Hole getValue(HandInfo o) {
				return o.hole;
			}
		});
		cols.add(new TableModelColumn<HandInfo,String[]>("Board", String[].class) {
			@Override
			public String[] getValue(HandInfo o) {
				return o.hand.board;
			}
		});
		cols.add(new TableModelColumn<HandInfo,Integer>("Seats", Integer.class) {
			@Override
			public Integer getValue(HandInfo o) {
				return o.hand.seats.length;
			}
		});
		cols.add(new TableModelColumn<HandInfo,Integer>("MyPos", Integer.class) {
			@Override
			public Integer getValue(HandInfo o) {
				return o.mypos();
			}
		});
		cols.add(new TableModelColumn<HandInfo,String>("Show", String.class) {
			@Override
			public String getValue(HandInfo o) {
				return o.hand.showdown ? "Y" : "";
			}
		});
		cols.add(new TableModelColumn<HandInfo,Integer>("Pot", Integer.class) {
			@Override
			public Integer getValue(HandInfo o) {
				return o.hand.pot;
			}
		});
		cols.add(new TableModelColumn<HandInfo,Integer>("MyValue", Integer.class) {
			@Override
			public Integer getValue(HandInfo o) {
				return o.myvalue();
			}
		});
		//, "pip", "af", "av"
		// handtype (ss, ds, P, 2P, (A)--1), floptype (P, F, S)
	}
	private final List<HandInfo> hands;
	public SessionTableModel(List<HandInfo> hands) {
		this.hands = hands;
	}
	public HandInfo getRow(int r) {
		return hands.get(r);
	}
	@Override
	public int getColumnCount() {
		return cols.size();
	}
	@Override
	public Class<?> getColumnClass(int c) {
		return cols.get(c).cl;
	}
	@Override
	public String getColumnName(int c) {
		return cols.get(c).name;
	}
	@Override
	public int getRowCount() {
		return hands.size();
	}
	@Override
	public Object getValueAt(int r, int c) {
		if (r < hands.size()) {
			return cols.get(c).getValue(hands.get(r));
		}
		return null;
	}

}

