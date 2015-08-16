package pet.hp;

import java.util.*;

/**
 * stores hands, games and tournaments.
 * thread safe (all methods synchronized)
 */
public class History {

	/** game instances */
	private final ArrayList<Game> games = new ArrayList<>();
	/** tournament instances */
	private final TreeMap<Long,Tourn> tourns = new TreeMap<>();
	/** hands seen so far */
	private final ArrayList<Hand> hands = new ArrayList<>();
	/** hand ids seen so far */
	private final HashSet<Long> handIds = new HashSet<>();
	/** listeners for history changes */
	private final ArrayList<HistoryListener> listeners = new ArrayList<>();
	/** tournament players seen - XXX inefficient hack */
	private final TreeMap<Long,TreeSet<String>> tp = new TreeMap<>();
	/** current player names - i.e. the names of players the hand is played from the perspective of */
	private final Set<String> self = new TreeSet<>();
	
	public History() {
		//
	}
	
	/**
	 * add listener for new data
	 */
	public synchronized void addListener(HistoryListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * add new hand from parser
	 */
	public synchronized void addHand(Hand hand) {
		if (handIds.contains(hand.id)) {
			throw new RuntimeException("duplicate hand id");
		}
		self.add(hand.myseat.name);
		hands.add(hand);
		for (HistoryListener l : listeners) {
			l.handAdded(hand);
		}
	}

	/**
	 * Get game instance by game id string
	 */
	public synchronized Game getGame(String id) {
		for (Game game : games) {
			if (game.id.equals(id)) {
				return game;
			}
		}
		return null;
	}
	
	/**
	 * Get a list of all game ids.
	 * always returns a new list
	 */
	public synchronized List<String> getGames() {
		ArrayList<String> gameids = new ArrayList<>(games.size());
		for (Game game : games) {
			gameids.add(game.id);
		}
		Collections.sort(gameids);
		return gameids;
	}

	/**
	 * get the definitive game instance for the given game.
	 * requires game type, limit, max, currency, bb/sb for non tourn, optionally mix, subtype
	 */
	public synchronized Game getGame(final Game game) { 
		if (game.type == null || game.limit == null || game.max == 0 || game.currency == 0 || game.id != null) {
			throw new RuntimeException("invalid game");
		}
		
		if (game.currency == Game.TOURN_CURRENCY) {
			// don't store blinds for tournament hands as they are variable
			game.sb = 0;
			game.bb = 0;
			game.ante = 0;
		}
		
		// find game, otherwise create it
		for (Game g : games) {
			if (g.currency == game.currency && g.type == game.type && g.limit == game.limit
					&& g.subtype == game.subtype && g.sb == game.sb && g.bb == game.bb && g.mix == game.mix
					&& g.max == game.max && g.ante == game.ante) {
				return g;
			}
		}

		game.id = GameUtil.getGameId(game);
		
		games.add(game);
		System.out.println("added game " + game);
		
		for (HistoryListener l : listeners) {
			l.gameAdded(game);
		}
		
		return game;
	}

	/**
	 * get tournament instance, possibly creating it
	 */
	public synchronized Tourn getTourn(long id, char cur, int buyin, int cost) {
		Tourn t = tourns.get(id);
		if (t == null) {
			tourns.put(id, t = new Tourn(id));
			t.currency = cur;
			t.buyin = buyin;
			t.cost = cost;
		} else {
			if (t.currency != cur || t.buyin != buyin || t.cost != cost) {
				throw new RuntimeException("tournament changed: " + t);
			}
		}
		return t;
	}
	
	/**
	 * Mark players as seen in tournament
	 */
	public synchronized void addTournPlayers(Long tournidobj, Collection<String> players) {
		TreeSet<String> tps = tp.get(tournidobj);
		if (tps == null) {
			tp.put(tournidobj, tps = new TreeSet<>());
		}
		for (String p : players) {
			if (!tps.contains(p)) {
				tps.add(p);
			}
		}
		Tourn t = tourns.get(tournidobj);
		if (t.players < tps.size()) {
			t.players = tps.size();
		}
	}
	
	/**
	 * Get hands for the player.
	 * Always returns new list
	 */
	public synchronized List<Hand> getHands(String player, String gameid) {
		System.out.println("get hands for " + player + " gameid " + gameid);
		List<Hand> hands = new ArrayList<>();

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
	 * Get hands for the tournament.
	 * always returns new list
	 */
	public synchronized List<Hand> getHands(long tournid) {
		System.out.println("get hands for tourn " + tournid);
		List<Hand> hands = new ArrayList<>();

		for (Hand hand : this.hands) {
			if (hand.tourn != null && hand.tourn.id == tournid) {
				hands.add(hand);
			}
		}

		System.out.println("got " + hands.size() + " hands");
		return hands;
	}
	
	/**
	 * return names of current player
	 */
	public synchronized Collection<String> getSelf() {
		return Arrays.asList(self.toArray(new String[self.size()]));
	}

	public synchronized int getHands() {
		return hands.size();
	}
	
	public synchronized int getTourns() {
		return tourns.size();
	}
	
}
