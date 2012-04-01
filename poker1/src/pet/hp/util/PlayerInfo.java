package pet.hp.util;

import java.util.*;

import pet.hp.Game;
import pet.hp.Hand;
import pet.hp.Seat;

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
	
	public PlayerGameInfo getGameInfo(Game game) {
		PlayerGameInfo gi = games.get(game.id);
		if (gi == null) {
			games.put(game.id, gi = new PlayerGameInfo(this, game));
		}
		return gi;
	}
	
	public void add(Hand h, Seat s) {
		hands++;
		if (date == null || date.before(h.date)) {
			date = h.date;
		}
		
		PlayerGameInfo gi = getGameInfo(h.game);
		gi.add(s, h);
	}

	@Override
	public String toString() {
		return "PlayerInfo[" + name + " games=" + games.size() + " hands=" + hands + "]";
	}

}
