package pet.ui.ta;

import java.util.*;

import pet.hp.info.PlayerInfo;

public class PlayerInfoTableModel extends MyTableModel<PlayerInfo> {
	
	private static final List<MyColumn<PlayerInfo>> cols = new ArrayList<MyColumn<PlayerInfo>>();
	
	static {
		cols.add(new MyColumn<PlayerInfo>(String.class, "Player", "Player name") {
			@Override
			public String getValue(PlayerInfo o) {
				return o.name;
			}
		});
		cols.add(new MyColumn<PlayerInfo>(Integer.class, "Games", "Number of game types") {
			@Override
			public Integer getValue(PlayerInfo o) {
				return o.getGameCount();
			}
		});
		cols.add(new MyColumn<PlayerInfo>(Integer.class, "Hands", "Number of hands") {
			@Override
			public Integer getValue(PlayerInfo o) {
				return o.hands;
			}
		});
		cols.add(new MyColumn<PlayerInfo>(Date.class, "Last", "Date of last hand") {
			@Override
			public Date getValue(PlayerInfo o) {
				return o.date;
			}
		});
	}
	
	public PlayerInfoTableModel() {
		super(cols);
	}
	
}
