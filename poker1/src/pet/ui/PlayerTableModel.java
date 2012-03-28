package pet.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import pet.hp.util.PlayerInfo;

class PlayerTableModel extends AbstractTableModel {
	private static final List<TableModelColumn<PlayerInfo,?>> cols = new ArrayList<TableModelColumn<PlayerInfo,?>>();
	static {
		cols.add(new TableModelColumn<PlayerInfo,String>("Player", String.class) {
			@Override
			public String getValue(PlayerInfo o) {
				return o.name;
			}
		});
		cols.add(new TableModelColumn<PlayerInfo,Integer>("Games", Integer.class) {
			@Override
			public Integer getValue(PlayerInfo o) {
				return o.games.size();
			}
		});
		cols.add(new TableModelColumn<PlayerInfo,Integer>("Hands", Integer.class) {
			@Override
			public Integer getValue(PlayerInfo o) {
				return o.hands;
			}
		});
		cols.add(new TableModelColumn<PlayerInfo,Date>("Last", Date.class) {
			@Override
			public Date getValue(PlayerInfo o) {
				return o.date;
			}
		});
	}
	private final List<String> players = new ArrayList<String>();
	private final Map<String,PlayerInfo> playerMap;
	public PlayerTableModel(Map<String, PlayerInfo> playerMap) {
		this.playerMap = playerMap;
		players.addAll(playerMap.keySet());
		Collections.sort(players);
	}
	public PlayerInfo getRow(int r) {
		return playerMap.get(players.get(r));
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
		return players.size();
	}
	
	@Override
	public Class<?> getColumnClass(int c) {
		return cols.get(c).cl;
	}

	@Override
	public Object getValueAt(int r, int c) {
		if (r < players.size()) {
			String player = players.get(r);
			PlayerInfo pi = playerMap.get(player);
			return cols.get(c).getValue(pi);
		}
		return null;
	}
	
}