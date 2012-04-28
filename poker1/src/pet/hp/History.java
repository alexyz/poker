package pet.hp;

import java.util.*;

import pet.eq.Poker;

/**
 * stores hands, games and tournaments.
 * thread safe
 */
public class History {

	/** string cache to avoid multiple instances of same string */
	private final Map<String,String> cache = new HashMap<String,String>();
	/** game instances */
	private final ArrayList<Game> games = new ArrayList<Game>();
	/** tournament instances */
	private final Map<Long,Tourn> tourns = new HashMap<Long,Tourn>();
	/** hands seen so far */
	private final List<Hand> hands = new ArrayList<Hand>();
	/** hand ids seen so far */
	private final Set<Long> handIds = new HashSet<Long>();
	private final List<HistoryListener> listeners = new ArrayList<HistoryListener>();
	
	public History() {
		for (String c : Poker.FULL_DECK) {
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
		return gameids;
	}

	/**
	 * get the game for the hand line and table details
	 */
	public synchronized Game getGame(char currency, char mix, char type, int subtype, char limit, int max, int sb, int bb) { 
		if (type == 0 || limit == 0 || max == 0 || currency == 0) {
			throw new RuntimeException("invalid game");
		}
		
		if (currency == Game.TOURN_CURRENCY) {
			// don't store blinds for tournament hands as they are variable
			sb = 0;
			bb = 0;
		}
		
		// find game, otherwise create it
		for (Game game : games) {
			if (game.currency == currency && game.type == type && game.limit == limit
					&& game.subtype == subtype && game.sb == sb && game.bb == bb && game.mix == mix
					&& game.max == max) {
				return game;
			}
		}

		Game game = new Game();
		game.currency = currency;
		game.type = type;
		game.limit = limit;
		game.subtype = subtype;
		game.sb = sb;
		game.bb = bb;
		game.max = max;
		game.mix = mix;
		game.id = GameUtil.getGameId(game);
		games.add(game);

		System.out.println("created game " + game);
		return game;
	}

	/**
	 * get tournament instance, possibly creating it
	 */
	public synchronized Tourn getTourn(long id) {
		Tourn t = tourns.get(id);
		if (t == null) {
			tourns.put(id, t = new Tourn(id));
		}
		return t;
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


}
