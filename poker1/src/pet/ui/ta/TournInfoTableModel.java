package pet.ui.ta;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pet.hp.info.TournInfo;

public class TournInfoTableModel extends MyTableModel<TournInfo> {
	
	public static final List<MyColumn<TournInfo>> allCols = new ArrayList<MyColumn<TournInfo>>();
	
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
	
	static {
		allCols.add(id);
		allCols.add(date);
	}

	public TournInfoTableModel() {
		super(allCols);
	}

}
