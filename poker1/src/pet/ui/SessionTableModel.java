package pet.ui;

import java.util.*;

import javax.swing.table.AbstractTableModel;

import pet.hp.util.*;

/**
 * table model for hands in a session
 */
class SessionTableModel extends AbstractTableModel {
	private static final List<TableCol<HandInfo,?>> cols = new ArrayList<TableCol<HandInfo,?>>();
	static {
		cols.add(new TableCol<HandInfo,Date>("Date", Date.class) {
			@Override
			public Date getValue(HandInfo o) {
				return o.hand.date;
			}
		});
		cols.add(new TableCol<HandInfo,Hole>("MyHand", Hole.class) {
			@Override
			public Hole getValue(HandInfo o) {
				return o.hole;
			}
		});
		cols.add(new TableCol<HandInfo,String[]>("Board", String[].class) {
			@Override
			public String[] getValue(HandInfo o) {
				return o.hand.board;
			}
		});
		cols.add(new TableCol<HandInfo,Integer>("Seats", Integer.class) {
			@Override
			public Integer getValue(HandInfo o) {
				return o.hand.seats.length;
			}
		});
		cols.add(new TableCol<HandInfo,Integer>("MyPos", Integer.class) {
			@Override
			public Integer getValue(HandInfo o) {
				return o.mypos();
			}
		});
		cols.add(new TableCol<HandInfo,String>("Show", String.class) {
			@Override
			public String getValue(HandInfo o) {
				return o.hand.showdown ? "Y" : "";
			}
		});
		cols.add(new TableCol<HandInfo,Integer>("Pot", Integer.class) {
			@Override
			public Integer getValue(HandInfo o) {
				return o.hand.pot;
			}
		});
		cols.add(new TableCol<HandInfo,Integer>("MyValue", Integer.class) {
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

abstract class TableCol<T,S> {
	public final String name;
	public final Class<?> cl;

	public TableCol(String name, Class<S> cl) {
		this.name = name;
		this.cl = cl;
	}
	
	public abstract S getValue(T o);
}

