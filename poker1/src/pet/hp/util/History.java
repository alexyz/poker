package pet.hp.util;

import java.io.*;
import java.util.*;
import pet.hp.*;

/**
 * History analysis
 */
public class History {
	
	private static final PrintStream out = System.out;
	private static final Set<String> acts = new TreeSet<String>(Arrays.asList("folds", "checks", "calls", "bets", "raises", "shows"));
	
	private final Map<String, PlayerInfo> pmap = new TreeMap<String, PlayerInfo>();
	
	/**
	 * Get the player info map
	 */
	public Map<String, PlayerInfo> getInfo() {
		return pmap;
	}
	
	/**
	 * Add one more hand to player info map
	 */
	public void addHand(Hand h) {
		// interesting actions

		String game = h.gamename;
		char type = h.gametype;
		for (int s = 0; s < h.streets.length; s++) {
			Action[] str = h.streets[s];
			for (Action a : str) {
				String player = a.seat.name;
				PlayerInfo pi = getInfo(player);
				PlayerGameInfo gi = getInfo(pi, game, type);
				if (acts.contains(a.act)) {
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
			PlayerInfo pi = getInfo(s.name);
			// FIXME overestimates hands
			// could be sitting without hand?
			pi.hands++;
			PlayerGameInfo gi = getInfo(pi, game, type);
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
	
	private static PlayerGameInfo getInfo(PlayerInfo pi, String gamename, char gametype) {
		PlayerGameInfo gi = pi.gmap.get(gamename);
		if (gi == null) {
			pi.gmap.put(gamename, gi = new PlayerGameInfo(gametype));
		}
		return gi;
	}
	
	public PlayerInfo getInfo(String player) {
		PlayerInfo pi = pmap.get(player);
		if (pi == null) {
			pmap.put(player, pi = new PlayerInfo(player));
		}
		return pi;
	}
	
	
}
