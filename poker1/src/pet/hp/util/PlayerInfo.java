package pet.hp.util;

import java.util.*;

public class PlayerInfo {

	public static final Comparator<PlayerInfo> handscmp = new Comparator<PlayerInfo>() {
		@Override
		public int compare(PlayerInfo pi1, PlayerInfo pi2) {
			return pi1.hands - pi2.hands;
		}
	};
	
	public final String name;
	public final Map<String,PlayerGameInfo> gmap = new TreeMap<String,PlayerGameInfo>();
	public int hands;
	
	public PlayerInfo(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "PlayerInfo[" + name + " games=" + gmap.size() + " hands=" + hands + "]";
	}

	public String toLongString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append("\n");
		for (Map.Entry<String,PlayerGameInfo> e : gmap.entrySet()) {
			sb.append("Game ").append(e.getKey()).append("\n");
			sb.append(e.getValue().toLongString()).append("\n");
			sb.append("\n");
		}
		return sb.toString();
	}
}
