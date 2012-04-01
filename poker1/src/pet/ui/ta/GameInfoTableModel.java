package pet.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import pet.hp.util.PlayerGameInfo;

class GameTableModel extends AbstractTableModel {
	private static final List<TableModelColumn<PlayerGameInfo,?>> cols = new ArrayList<TableModelColumn<PlayerGameInfo,?>>();
	static {
		cols.add(new TableModelColumn<PlayerGameInfo,String>("Game", String.class) {
			@Override
			public String getValue(PlayerGameInfo o) {
				return o.game.name;
			}
		});
		cols.add(new TableModelColumn<PlayerGameInfo,Integer>("Hands", Integer.class) {
			@Override
			public Integer getValue(PlayerGameInfo o) {
				return o.hands;
			}
		});
		cols.add(new TableModelColumn<PlayerGameInfo,Float>("PcHandsShow", Float.class) {
			@Override
			public Float getValue(PlayerGameInfo o) {
				return (o.showdown*100f) / o.hands;
			}
		});
		cols.add(new TableModelColumn<PlayerGameInfo,Float>("PcHandsWon", Float.class) {
			@Override
			public Float getValue(PlayerGameInfo o) {
				return (o.handswon*100f) / o.hands;
			}
		});
		cols.add(new TableModelColumn<PlayerGameInfo,Float>("PcHandsWonShow", Float.class) {
			@Override
			public Float getValue(PlayerGameInfo o) {
				return (o.handswonshow*100f) / o.handswon;
			}
		});
		cols.add(new TableModelColumn<PlayerGameInfo,Integer>("AmWon", Integer.class) {
			@Override
			public Integer getValue(PlayerGameInfo o) {
				return o.won;
			}
		});
		cols.add(new TableModelColumn<PlayerGameInfo,Integer>("AmPip", Integer.class) {
			@Override
			public Integer getValue(PlayerGameInfo o) {
				return o.pip;
			}
		});
		cols.add(new TableModelColumn<PlayerGameInfo,Integer>("AmTot", Integer.class) {
			@Override
			public Integer getValue(PlayerGameInfo o) {
				return o.won - o.pip;
			}
		});
		cols.add(new TableModelColumn<PlayerGameInfo,Integer>("Rake", Integer.class) {
			@Override
			public Integer getValue(PlayerGameInfo o) {
				return o.rake;
			}
		});
		cols.add(new TableModelColumn<PlayerGameInfo,Float>("AFc", Float.class) {
			@Override
			public Float getValue(PlayerGameInfo o) {
				return o.af(false);
			}
		});
		cols.add(new TableModelColumn<PlayerGameInfo,Float>("AFv", Float.class) {
			@Override
			public Float getValue(PlayerGameInfo o) {
				return o.af(true);
			}
		});
	}
	private final List<PlayerGameInfo> games = new ArrayList<PlayerGameInfo>();
	public GameTableModel(Map<String,PlayerGameInfo> games) {
		this.games.addAll(games.values());
	}
	public PlayerGameInfo getRow(int r) {
		return games.get(r);
	}
	@Override
	public int getColumnCount() {
		return cols.size();
	}
	
	@Override
	public String getColumnName(int c) {
		return cols.get(c).name;
	}

	@Override
	public int getRowCount() {
		return games.size();
	}
	
	@Override
	public Class<?> getColumnClass(int c) {
		return cols.get(c).cl;
	}

	@Override
	public Object getValueAt(int r, int c) {
		if (r < games.size()) {
			PlayerGameInfo gi = games.get(r);
			return cols.get(c).getValue(gi);
		}
		return null;
	}
	
}