package pet.ui.ta;

import java.util.*;

import pet.hp.util.PlayerInfo;

public class PlayerInfoTableModel extends MyTableModel<PlayerInfo> {
	
	private static final List<MyTableModelColumn<PlayerInfo,?>> cols = new ArrayList<MyTableModelColumn<PlayerInfo,?>>();
	
	static {
		cols.add(new MyTableModelColumn<PlayerInfo,String>(String.class, "Player", "Player name") {
			@Override
			public String getValue(PlayerInfo o) {
				return o.name;
			}
		});
		cols.add(new MyTableModelColumn<PlayerInfo,Integer>(Integer.class, "Games", "Number of game types") {
			@Override
			public Integer getValue(PlayerInfo o) {
				return o.games.size();
			}
		});
		cols.add(new MyTableModelColumn<PlayerInfo,Integer>(Integer.class, "Hands", "Number of hands") {
			@Override
			public Integer getValue(PlayerInfo o) {
				return o.hands;
			}
		});
		cols.add(new MyTableModelColumn<PlayerInfo,Date>(Date.class, "Last", "Date of last hand") {
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
