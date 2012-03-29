package pet.hp.util;

import java.util.*;
import pet.hp.*;
import pet.ui.gr.DateGraphData;
import pet.ui.gr.GraphData;
import pet.ui.gr.GraphDataPoint;

/**
 * History analysis
 */
public class History implements FollowListener {
	
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
	
	public synchronized List<Hand> getHands(String player, String game) {
		System.out.println("get hands for " + player + " game " + game);
		List<Hand> hands = new ArrayList<Hand>();
		
		for (Hand hand : this.hands) {
			if (hand.gamename.equals(game)) {
				for (Seat seat : hand.seats) {
					if (seat.name.equals(player)) {
						hands.add(hand);
						break;
					}
				}
			}
		}
		
		return hands;
	}

	public GraphData getBankRoll(String player, String game) {
		List<Hand> hands = getHands(player, game);
		if (hands.size() <= 1) {
			System.out.println("not enough hands for bankroll");
			return null;
		}
		
		Hand fh = hands.get(0);
		final char currency = fh.currency;
		GraphData data = new DateGraphData() {
			@Override
			public String getYName(int y) {
				return HandUtil.formatMoney(currency, y);
			}
		};
		data.name = player + " * " + game;
		
		Collections.sort(hands, HandUtil.idCmp);
		int won = 0, day = 0;
		for (Hand hand : hands) {
			for (Seat seat : hand.seats) {
				if (seat.name.equals(player)) {
					int handDay = DateGraphData.getDayNumber(hand.date);
					if (day != handDay) {
						data.points.add(new GraphDataPoint(handDay, won));
						day = handDay;
					}
					won += seat.won - seat.pip;
				}
			}
		}
		System.out.println("bankroll data: " + data.points.size());
		return data;
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
				int[] c = gi.getAction(a.act);
				c[0]++;
				c[1] += a.amount;
				//gi.pip += a.amount;
				
				// TODO push folded on into seat
				if (a.act.equals("folds")) {
					gi.foldedon[s]++;
				}
			}
		}
		
		for (Seat s : h.seats) {
			PlayerInfo pi = getPlayerInfo(s.name, true);
			// could be sitting without hand?
			pi.hands++;
			if (pi.date == null || pi.date.before(h.date)) {
				pi.date = h.date;
			}
			
			PlayerGameInfo gi = getPlayerGameInfo(pi, game, type);
			gi.hands++;
			gi.pip += s.pip;
			if (s.showdown) {
				gi.showdown++;
			}
			if (s.won > 0) {
				gi.handswon++;
				gi.won += s.won;
				// overcounts rake if split?
				gi.rake += h.rake;
			}
			if (s.showdown && s.won > 0) {
				gi.handswonshow++;
			}
			
		}

	}
	
	private synchronized static PlayerGameInfo getPlayerGameInfo(PlayerInfo pi, String gamename, char gametype) {
		PlayerGameInfo gi = pi.games.get(gamename);
		if (gi == null) {
			pi.games.put(gamename, gi = new PlayerGameInfo(pi, gamename, gametype));
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
