package pet.ui.ta;

import java.util.*;

import pet.hp.util.PlayerInfo;

public class PlayerInfoTableModel extends MyTableModel<PlayerInfo> {
	
	private static final List<MyTableModelColumn<PlayerInfo,?>> cols = new ArrayList<MyTableModelColumn<PlayerInfo,?>>();
	
	static {
		cols.add(new MyTableModelColumn<PlayerInfo,String>("Player", "Player name", String.class) {
			@Override
			public String getValue(PlayerInfo o) {
				return o.name;
			}
		});
		cols.add(new MyTableModelColumn<PlayerInfo,Integer>("Games", "Number of game types", Integer.class) {
			@Override
			public Integer getValue(PlayerInfo o) {
				return o.games.size();
			}
		});
		cols.add(new MyTableModelColumn<PlayerInfo,Integer>("Hands", "Number of hands", Integer.class) {
			@Override
			public Integer getValue(PlayerInfo o) {
				return o.hands;
			}
		});
		cols.add(new MyTableModelColumn<PlayerInfo,Date>("Last", "Date of last hand", Date.class) {
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
