package pet.hp.info;

import java.util.*;
import pet.hp.*;
import pet.ui.gr.DateGraphData;
import pet.ui.gr.GraphData;
import pet.ui.gr.GraphDataPoint;

/**
 * Hand analysis gateway
 */
public class History implements FollowListener {

	/**
	 * players seen
	 */
	private final Map<String, PlayerInfo> playerMap = new TreeMap<String, PlayerInfo>();
	/**
	 * hands seen so far
	 */
	private final List<Hand> hands = new ArrayList<Hand>();
	/**
	 * hand ids seen so far
	 */
	private final Set<Long> handIds = new TreeSet<Long>();
	/**
	 * the player info representing the whole population
	 */
	private final PlayerInfo population = new PlayerInfo("*");

	public PlayerInfo getPopulation() {
		return population;
	}

	/**
	 * Get the list of players matching the given pattern
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

	/**
	 * Get hands for the player
	 */
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

	/**
	 * get the graph data for the players all time bankroll
	 */
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
				return GameUtil.formatMoney(currency, y);
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
	public synchronized void nextHand(Hand hand) {
		if (handIds.contains(hand.id)) {
			throw new RuntimeException("already has hand " + hand);
		}
		hands.add(hand);
		handIds.add(hand.id);

		// update player info with seat
		for (Seat s : hand.seats) {
			PlayerInfo pi = getPlayerInfo(s.name, true);
			pi.add(hand, s);

			// add to population, but XXX careful not to over count hand stuff
			population.add(hand, s);
		}

	}
	
	@Override
	public void doneFile(int done, int total) {
		//
	}

	public synchronized PlayerInfo getPlayerInfo(String player) {
		return playerMap.get(player);
	}

	private synchronized PlayerInfo getPlayerInfo(String player, boolean create) {
		PlayerInfo pi = playerMap.get(player);
		if (pi == null && create) {
			playerMap.put(player, pi = new PlayerInfo(player));
		}
		return pi;
	}

}
