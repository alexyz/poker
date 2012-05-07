package pet.hp.info;

import java.util.*;

import pet.hp.Game;
import pet.hp.Hand;
import pet.hp.Seat;

/**
 * overall statistics for player. not much to say that isn't game specific
 */
public class PlayerInfo {

	/** player name */
	public final String name;
	/** player hands */
	public int hands;
	/** date last played */
	public Date date;
	
	/** players games */
	private final TreeMap<String,PlayerGameInfo> games = new TreeMap<String,PlayerGameInfo>();
	
	public PlayerInfo(String name) {
		this.name = name;
	}
	
	/**
	 * get player game info, returns null if there is no game info
	 */
	public synchronized PlayerGameInfo getGameInfo(String gameid) {
		return games.get(gameid);
	}
	
	/**
	 * Get the number of games
	 */
	public synchronized int getGameCount() {
		return games.size();
	}
	
	/**
	 * Get the games
	 */
	public synchronized Map<String, PlayerGameInfo> getGames() {
		// not strictly thread safe...
		return Collections.unmodifiableMap(games);
	}
	
	/**
	 * add hand to player
	 */
	public synchronized void add(Hand h, Seat s) {
		hands++;
		if (date == null || date.before(h.date)) {
			date = h.date;
		}
		
		Game game = h.game;
		PlayerGameInfo gi = games.get(game.id);
		if (gi == null) {
			games.put(game.id, gi = new PlayerGameInfo(this, game));
		}
		gi.add(h, s);
	}

	@Override
	public String toString() {
		return "PlayerInfo[" + name + " games=" + games.size() + " hands=" + hands + "]";
	}

}
