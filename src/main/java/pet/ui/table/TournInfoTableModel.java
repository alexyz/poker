package pet.ui.table;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pet.hp.info.TournInfo;

public class TournInfoTableModel extends MyTableModel<TournInfo> {
	
	public static final List<MyColumn<TournInfo>> allCols = new ArrayList<>();
	
	private static final MyColumn<TournInfo> id = new MyColumn<TournInfo>(Long.class, "ID", "Tournament identifier") {
		@Override
		public Object getValue(TournInfo row) {
			return row.tourn.id;
		}
	};
	
	private static final MyColumn<TournInfo> date = new MyColumn<TournInfo>(Date.class, "Date", "Tournament date") {
		@Override
		public Object getValue(TournInfo row) {
			return row.tourn.date;
		}
	};
	
	private static final MyColumn<TournInfo> hands = new MyColumn<TournInfo>(Integer.class, "Hands", "Number of hands played") {
		@Override
		public Object getValue(TournInfo row) {
			return row.hands;
		}
	};
	
	private static final MyColumn<TournInfo> cost = new MyColumn<TournInfo>(Integer.class, "Cost", "Buy-in plus cost") {
		@Override
		public Object getValue(TournInfo row) {
			return row.tourn.buyin + row.tourn.cost;
		}
	};
	
	private static final MyColumn<TournInfo> won = new MyColumn<TournInfo>(Integer.class, "Won", "Amount won") {
		@Override
		public Object getValue(TournInfo row) {
			return row.tourn.won;
		}
	};
	
	private static final MyColumn<TournInfo> pos = new MyColumn<TournInfo>(Integer.class, "Position", "Final position") {
		@Override
		public Object getValue(TournInfo row) {
			return row.tourn.pos;
		}
	};
	
	private static final MyColumn<TournInfo> pl = new MyColumn<TournInfo>(Integer.class, "Players", "Number of players") {
		@Override
		public Object getValue(TournInfo row) {
			return row.tourn.players;
		}
	};
	
	static {
		//allCols.add(id);
		allCols.add(cost);
		allCols.add(date);
		allCols.add(hands);
		allCols.add(pl);
		allCols.add(pos);
		allCols.add(won);
	}

	public TournInfoTableModel() {
		super(allCols);
	}

}
