package pet.hp.util;

import java.util.*;

public class PlayerInfo {

	/** player name */
	public final String name;
	public final Map<String,PlayerGameInfo> games = new TreeMap<String,PlayerGameInfo>();
	/** player hands */
	public int hands;
	public Date date;
	
	public PlayerInfo(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "PlayerInfo[" + name + " games=" + games.size() + " hands=" + hands + "]";
	}

}
