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
	private final Set<Long> handIds = new TreeSet<Long>();
	private final PlayerInfo population = new PlayerInfo("*");
	
	public PlayerInfo getPopulation() {
		return population;
	}
	
	/**
	 * Get the player info map
	 */
	public synchronized List<PlayerInfo> getPlayers(String pattern) {
		pattern = pattern.toLowerCase();
		System.out.println("get players " + pattern);
		List<PlayerInfo> players = new ArrayList<PlayerInfo>();
		for (Map.Entry<String,PlayerInfo> e : this.playerMap.entrySet()) {
			if (e.getKey().toLowerCase().contains(pattern)) {
				players.add(e.getValue());
			}
		}
		System.out.println("got " + players.size() + " players");
		return players;
	}
	
	public synchronized List<Hand> getHands(String player, String gameid) {
		System.out.println("get hands for " + player + " gameid " + gameid);
		List<Hand> hands = new ArrayList<Hand>();
		
		for (Hand hand : this.hands) {
			if (hand.game.id.equals(gameid)) {
				for (Seat seat : hand.seats) {
					if (seat.name.equals(player)) {
						hands.add(hand);
						break;
					}
				}
			}
		}
		
		System.out.println("got " + hands.size() + " hands");
		return hands;
	}

	public GraphData getBankRoll(String player, String game) {
		List<Hand> hands = getHands(player, game);
		if (hands.size() <= 1) {
			System.out.println("not enough hands for bankroll");
			return null;
		}
		
		Hand fh = hands.get(0);
		final char currency = fh.game.currency;
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
		System.out.println("bank roll data points: " + data.points.size());
		return data;
	}
	
	/**
	 * Add one more hand to player info map
	 */
	@Override
	public synchronized void nextHand(Hand h) {
		// interesting actions
		
		if (handIds.contains(h.id)) {
			throw new RuntimeException("already has hand " + h);
		}
		hands.add(h);
		handIds.add(h.id);
		
		// TODO population
		
		for (Seat s : h.seats) {
			PlayerInfo pi = getPlayerInfo(s.name, true);
			pi.add(h, s);
			
			// add to population, but XXX careful not to overcount hand stuff
			population.add(h, s);
		}

		
		for (int s = 0; s < h.streets.length; s++) {
			Action[] str = h.streets[s];
			int checked = 0;
			
			for (Action a : str) {
				PlayerInfo pi = getPlayerInfo(a.seat.name, true);
				PlayerGameInfo gi = pi.getGameInfo(h.game);
				int seatmask = 1 << a.seat.num;
				boolean hasChecked = (checked & seatmask) != 0;
				gi.addAction(s, a, hasChecked);
				
				PlayerGameInfo popgi = population.getGameInfo(h.game);
				popgi.addAction(s, a, hasChecked);
				//System.out.println("str " + s + " act " + a + " checked " + hasChecked);
				
				if (a.type == Action.CHECK_TYPE) {
					checked |= seatmask;
				}
			}
		}
		

	}
	
	public synchronized PlayerInfo getPlayerInfo(String player, boolean create) {
		PlayerInfo pi = playerMap.get(player);
		if (pi == null && create) {
			playerMap.put(player, pi = new PlayerInfo(player));
		}
		return pi;
	}
	
}
