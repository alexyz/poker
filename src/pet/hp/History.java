package pet.hp;

import java.util.*;

import pet.eq.Poker;

/**
 * stores hands, games and tournaments.
 * thread safe (all methods synchronized)
 */
public class History {

	/** string cache to avoid multiple instances of same string */
	private final HashMap<String,String> cache = new HashMap<String,String>(1000);
	/** game instances */
	private final ArrayList<Game> games = new ArrayList<Game>();
	/** tournament instances */
	private final TreeMap<Long,Tourn> tourns = new TreeMap<Long,Tourn>();
	/** hands seen so far */
	private final ArrayList<Hand> hands = new ArrayList<Hand>();
	/** hand ids seen so far */
	private final HashSet<Long> handIds = new HashSet<Long>();
	/** listeners for history changes */
	private final ArrayList<HistoryListener> listeners = new ArrayList<HistoryListener>();
	/** tournament players seen - XXX inefficient hack */
	private final TreeMap<Long,TreeSet<String>> tp = new TreeMap<Long,TreeSet<String>>();
	/** current player names - i.e. the names of players the hand is played from the perspective of */
	private final Set<String> self = new TreeSet<String>();
	
	public History() {
		for (String c : Poker.deck) {
			getString(c);
		}
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
	 * get cached string instance
	 */
	public synchronized String getString(String s) {
		if (s != null) {
			String s2 = cache.get(s);
			if (s2 != null) {
				return s2;
			}
			// create a new string that does not share the potentially large
			// backing array of the original
			s = new String(s);
			cache.put(s, s);
		}
		return s;
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
		ArrayList<String> gameids = new ArrayList<String>(games.size());
		for (Game game : games) {
			gameids.add(game.id);
		}
		Collections.sort(gameids);
		return gameids;
	}

	/**
	 * get the game for the hand line and table details
	 */
	public synchronized Game getGame(final Game game) { 
		if (game.type == 0 || game.limit == 0 || game.max == 0 || game.currency == 0 || game.id != null) {
			throw new RuntimeException("invalid game");
		}
		
		if (game.currency == Game.TOURN_CURRENCY) {
			// don't store blinds for tournament hands as they are variable
			game.sb = 0;
			game.bb = 0;
		}
		
		// find game, otherwise create it
		for (Game g : games) {
			if (g.currency == game.currency && g.type == game.type && g.limit == game.limit
					&& g.subtype == game.subtype && g.sb == game.sb && g.bb == game.bb && g.mix == game.mix
					&& g.max == game.max) {
				return g;
			}
		}

		game.id = GameUtil.getGameId(game);
		switch (game.type) {
			case Game.STUDHL_TYPE:
			case Game.OMHL_TYPE:
				game.hilo = true;
				break;
			default:
		}
		games.add(game);
		System.out.println("new game " + game);
		
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
			tp.put(tournidobj, tps = new TreeSet<String>());
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
	 * Get hands for the tournament.
	 * always returns new list
	 */
	public synchronized List<Hand> getHands(long tournid) {
		System.out.println("get hands for tourn " + tournid);
		List<Hand> hands = new ArrayList<Hand>();

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

}
