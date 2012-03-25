package pet.hp.util;

import java.util.*;
import pet.hp.*;

/**
 * History analysis
 */
public class History implements FollowListener {
	
	private static final Set<String> actions = new TreeSet<String>(Arrays.asList("folds", "checks", "calls", "bets", "raises", "shows"));
	
	private final Map<String, PlayerInfo> playerMap = new TreeMap<String, PlayerInfo>();
	private final List<Hand> hands = new ArrayList<Hand>();
	
	/**
	 * Get the player info map
	 */
	public synchronized Map<String,PlayerInfo> getPlayers(String pattern) {
		pattern = pattern.toLowerCase();
		System.out.println("get players " + pattern);
		Map<String, PlayerInfo> playerMap = new TreeMap<String, PlayerInfo>();
		for (Map.Entry<String,PlayerInfo> e : this.playerMap.entrySet()) {
			if (e.getKey().toLowerCase().contains(pattern)) {
				playerMap.put(e.getKey(), e.getValue());
			}
		}
		return playerMap;
	}
	
	public synchronized List<HandInfo> getHands(String player, String game) {
		List<HandInfo> handInfos = new ArrayList<HandInfo>();
		
		for (Hand h : hands) {
			if (h.gamename.equals(game)) {
				for (Seat s : h.seats) {
					if (s.name.equals(player)) {
						handInfos.add(new HandInfo(h));
						break;
					}
				}
			}
		}
		
		return handInfos;
	}
	
	/**
	 * Add one more hand to player info map
	 */
	@Override
	public synchronized void nextHand(Hand h) {
		// interesting actions
		
		hands.add(h);

		String game = h.gamename;
		char type = h.gametype;
		for (int s = 0; s < h.streets.length; s++) {
			Action[] str = h.streets[s];
			for (Action a : str) {
				String player = a.seat.name;
				PlayerInfo pi = getPlayerInfo(player, true);
				PlayerGameInfo gi = getPlayerGameInfo(pi, game, type);
				if (actions.contains(a.act)) {
					int[] c = gi.getAction(a.act);
					c[0]++;
					c[1] += a.amount;
					gi.lost += a.amount;
					// TODO push folded on into seat
					if (a.act.equals("folds")) {
						gi.foldedon[s]++;
					}
				}
				// TODO push showdown into seat
				if (a.act.equals("shows") && HandUtil.isShowdown(type, s)) {
					gi.showdown++;
				}
			}
		}
		
		for (Seat s : h.seats) {
			PlayerInfo pi = getPlayerInfo(s.name, true);
			// FIXME overestimates hands
			// could be sitting without hand?
			pi.hands++;
			PlayerGameInfo gi = getPlayerGameInfo(pi, game, type);
			gi.hands++;
			if (s.won > 0) {
				gi.won += s.won;
				// XXX probably double counts rake
				gi.rake += h.rake;
				gi.woncount++;
			}
			if (s.uncalled > 0) {
				gi.lost -= s.uncalled;
			}
			if (s.defaultwin) {
				gi.defaultwin++;
			}
			
		}

	}
	
	private synchronized static PlayerGameInfo getPlayerGameInfo(PlayerInfo pi, String gamename, char gametype) {
		PlayerGameInfo gi = pi.gmap.get(gamename);
		if (gi == null) {
			pi.gmap.put(gamename, gi = new PlayerGameInfo(gametype));
		}
		return gi;
	}
	
	public synchronized PlayerInfo getPlayerInfo(String player, boolean create) {
		PlayerInfo pi = playerMap.get(player);
		if (pi == null && create) {
			playerMap.put(player, pi = new PlayerInfo(player));
		}
		return pi;
	}
	
}
