package pet.ui.table;

import java.util.*;

import pet.hp.info.PlayerInfo;

public class PlayerInfoTableModel extends MyTableModel<PlayerInfo> {
	
	private static final List<MyColumn<PlayerInfo>> cols = new ArrayList<>();
	
	private static final MyColumn<PlayerInfo> name = new MyColumn<PlayerInfo>(String.class, "Player", "Player name") {
		@Override
		public String getValue(PlayerInfo o) {
			return o.name;
		}
	};
	
	private static final MyColumn<PlayerInfo> games = new MyColumn<PlayerInfo>(Integer.class, "Games", "Number of game types") {
		@Override
		public Integer getValue(PlayerInfo o) {
			return o.getGameCount();
		}
	};
	
	private static final MyColumn<PlayerInfo> hands = new MyColumn<PlayerInfo>(Integer.class, "Hands", "Number of hands") {
		@Override
		public Integer getValue(PlayerInfo o) {
			return o.hands;
		}
	};
	
	private static final MyColumn<PlayerInfo> first = new MyColumn<PlayerInfo>(Date.class, "First", "First seen") {
		@Override
		public Date getValue(PlayerInfo o) {
			return o.firstDate;
		}
	};
	
	private static final MyColumn<PlayerInfo> last = new MyColumn<PlayerInfo>(Date.class, "Last", "Last seen") {
		@Override
		public Date getValue(PlayerInfo o) {
			return o.lastDate;
		}
	};
	
	static {
		cols.add(name);
		cols.add(games);
		cols.add(hands);
		cols.add(first);
		cols.add(last);
	}
	
	public PlayerInfoTableModel() {
		super(cols);
	}
	
}
