package pet.hp.info;

import java.util.*;
import pet.hp.*;

/**
 * Analyses players and hands.
 * This class is thread safe (all methods should be synchronized)
 */
public class Info implements HistoryListener {

	/**
	 * players seen
	 */
	private final Map<String, PlayerInfo> playerMap = new TreeMap<>();
	/**
	 * the player info representing the whole population
	 */
	private final PlayerInfo population = new PlayerInfo("*");
	
	private final Map<Long,TournInfo> tournInfos = new TreeMap<>();
	
	public PlayerInfo getPopulation() {
		return population;
	}

	public Info() {
		playerMap.put("*", population);
	}
	
	/**
	 * forget any players
	 */
	public synchronized void clear() {
		playerMap.clear();
		tournInfos.clear();
	}

	/**
	 * Get the list of players matching the given pattern
	 */
	public synchronized List<PlayerInfo> getPlayers(String pattern) {
		pattern = pattern.toLowerCase();
		System.out.println("get players " + pattern);
		List<PlayerInfo> players = new ArrayList<>();
		for (Map.Entry<String,PlayerInfo> e : this.playerMap.entrySet()) {
			if (e.getKey().toLowerCase().contains(pattern)) {
				players.add(e.getValue());
			}
		}
		System.out.println("got " + players.size() + " players");
		return players;
	}
	
	public synchronized List<PlayerInfo> getPlayers(Collection<String> names) {
		System.out.println("get players " + names);
		List<PlayerInfo> pinfos = new ArrayList<>();
		for (String name : names) {
			PlayerInfo pinfo = playerMap.get(name);
			if (pinfo != null) {
				pinfos.add(pinfo);
			}
		}
		System.out.println("got " + pinfos.size() + " players");
		return pinfos;
	}

	/**
	 * get player game infos for all players for the given game
	 */
	public synchronized List<PlayerGameInfo> getGameInfos(String gameid) {
		System.out.println("get game infos for " + gameid);
		List<PlayerGameInfo> gameinfos = new ArrayList<>();
		for (PlayerInfo pi : playerMap.values()) {
			PlayerGameInfo pgi = pi.getGameInfo(gameid);
			if (pgi != null) {
				gameinfos.add(pgi);
			}
		}
		System.out.println("got " + gameinfos.size() + " game infos");
		return gameinfos;
	}

	public synchronized PlayerInfo getPlayerInfo(String player) {
		return getPlayerInfo(player, false);
	}

	/**
	 * get player info for player, optionally creating
	 */
	private synchronized PlayerInfo getPlayerInfo(String player, boolean create) {
		PlayerInfo pi = playerMap.get(player);
		if (pi == null && create) {
			playerMap.put(player, pi = new PlayerInfo(player));
		}
		return pi;
	}
	
	/**
	 * get list of tourn infos.
	 * always returns new list
	 */
	public synchronized List<TournInfo> getTournInfos() {
		ArrayList<TournInfo> l = new ArrayList<>(tournInfos.values());
		return l;
	}

	/**
	 * Add just one more hand to player info map
	 */
	@Override
	public synchronized void handAdded(Hand hand) {
		// update player info with seat
		for (Seat s : hand.seats) {
			PlayerInfo pi = getPlayerInfo(s.name, true);
			pi.add(hand, s);

			// add to population, but XXX careful not to over count hand stuff
			population.add(hand, s);
			
			// XXX if recent, fire pgi updated?
		}
		
		Tourn t = hand.tourn;
		if (t != null) {
			TournInfo ti = tournInfos.get(t.id);
			if (ti == null) {
				tournInfos.put(t.id, ti = new TournInfo(t));
			}
			ti.addHand(hand);
			
			// XXX if recent, fire tourn info updated?
		}
	}

	@Override
	public void gameAdded(Game game) {
		// yawn
	}

}
