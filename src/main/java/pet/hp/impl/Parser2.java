
package pet.hp.impl;

import java.util.*;

import pet.hp.*;

/**
 * common utilities for parsers
 */
public abstract class Parser2 extends Parser {
	
	// stuff for current hand, cleared on clear()
	// TODO should all be private...
	
	/** current hand */
	protected Hand hand;
	/** running pot - updated when pip() is called */
	private int pot;
	/** has live sb been posted (others are dead) */
	protected boolean sbposted;
	/** array of seat num to seat pip for this street. seat numbers are 1-10 */
	private final int[] seatPip = new int[11];
	/** map of player name to seat for current hand */
	protected final Map<String, Seat> seatsMap = new TreeMap<>();
	/** streets of action for current hand */
	protected final List<List<Action>> streets = new ArrayList<>();
	
	public Parser2() {
		//
	}
	
	/**
	 * throw exception if condition is false
	 */
	protected void assert_ (boolean cond, String condDesc) {
		if (!cond) {
			throw new RuntimeException("Assertion failed: " + condDesc);
		}
	}
	
	/**
	 * throw exception if object is false
	 */
	protected void assertObj (Object obj, String objDesc) {
		if (obj == null) {
			throw new RuntimeException("Assertion failed: object is null: " + objDesc);
		}
	}
	
	@Override
	public void clear () {
		super.clear();
		streets.clear();
		seatsMap.clear();
		Arrays.fill(seatPip, 0);
		pot = 0;
		hand = null;
		sbposted = false;
	}
	
	protected List<Action> currentStreet () {
		return streets.get(streets.size() - 1);
	}
	
	/**
	 * get the current street number, starting at 0
	 */
	protected int currentStreetIndex () {
		return streets.size() - 1;
	}
	
	/**
	 * throw runtime exception
	 */
	protected void fail (String desc) {
		throw new RuntimeException("Failure: " + desc);
	}
	
	/**
	 * validate and finalise hand
	 */
	protected void finish () {
		validateHand();
		validatePot();
		validateSeats();
		validateActions();
		
		// get seats
		hand.seats = seatsMap.values().toArray(new Seat[seatsMap.size()]);
		Arrays.sort(hand.seats, HandUtil.seatCmp);
		
		// get actions
		hand.streets = new Action[streets.size()][];
		for (int n = 0; n < streets.size(); n++) {
			List<Action> street = streets.get(n);
			hand.streets[n] = street.toArray(new Action[street.size()]);
		}
		
		getHistory().addHand(hand);
		if (hand.tourn != null) {
			getHistory().addTournPlayers(hand.tourn.id, seatsMap.keySet());
		}
		
		println("end of hand " + hand);
		println("seats: " + seatsMap);
		
		clear();
	}
	
	private void validateActions () {
		for (List<Action> actions : streets) {
			for (Action a : actions) {
				switch (a.type) {
					case BRINGSIN:
					case BET:
					case RAISE:
					case CALL:
					case ANTE:
					case POST:
						assert_(a.amount > 0, "am > 0");
						break;
						
					case COLLECT:
					case UNCALL:
						assert_(a.amount < 0, "am < 0");
						break;
						
					case DRAW:
					case CHECK:
					case DOESNTSHOW:
					case SHOW:
					case STANDPAT:
					case MUCK:
					case FOLD:
						assert_(a.amount == 0, "am = 0");
						break;
						
					default:
						throw new RuntimeException();
				}
				// this isn't really asserting very much
				if (a.type == Action.Type.POST || a.type == Action.Type.ANTE) {
					assert_(a.amount <= hand.bb + hand.sb, "post/ante " + a + " <= bb+sb");
				}
			}
		}
	}
	
	private void validateSeats () {
		// can't check bb or sb as there may be 0-many
		assert_(seatsMap.size() <= hand.game.max, "seats < max");
		boolean sd = false;
		int s = 0;
		int hc = GameUtil.getHoleCards(hand.game.type);
		int uc = GameUtil.getUpCards(hand.game.type);
		for (Seat seat : seatsMap.values()) {
			assert_(seat.pip <= seat.chips, "chips");
			int c = 0;
			if (seat.downCards != null) {
				c += seat.downCards.length;
			}
			if (seat.upCards != null) {
				c += seat.upCards.length;
				assert_(seat.upCards.length <= uc, "uc");
			}
			if (GameUtil.isStud(hand.game.type) && hand.board != null) {
				// incredibly rare community card in stud
				c += hand.board.length;
			}
			if (seat.showdown) {
				s++;
				assert_(c == hc, "hole cards");
			} else {
				assert_(c <= hc, "hole cards");
			}
			sd |= seat.showdown;
		}
		if (sd) {
			assert_(hand.showdown, "ssd = hsd");
		}
		if (hand.showdown) {
			assert_(s >= 2, "2 seat showdown");
		} else {
			assert_(s == 0, "no seats showdown");
		}
	}
	
	private void validateHand () {
		assert_(hand.date != 0, "has date");
		assertObj(hand.game, "game");
		assert_(hand.bb > 0, "bb");
		assert_(hand.sb > 0 && hand.sb < hand.bb, "sb");
		assert_(hand.ante >= 0, "ante");
		if (GameUtil.isStud(hand.game.type)) {
			assert_(hand.button == 0, "no button: " + hand.button);
		} else {
			assert_(hand.button != 0, "has button");
		}
		int bs = hand.board != null ? hand.board.length : 0;
		assert_(bs <= GameUtil.getBoard(hand.game.type), "board");
		assert_(hand.id != 0, "has id");
		assert_((hand.id & Hand.ROOM) != 0, "hand room");
		if (hand.tourn != null) {
			assert_((hand.tourn.id & Hand.ROOM) != 0, "tourn room");
		}
		assertObj(hand.myseat, "my seat");
		int maxstr = GameUtil.getStreets(hand.game.type);
		if (hand.showdown) {
			assert_(streets.size() == maxstr, "streets " + streets.size() + " = max str " + maxstr + " for showdown");
		} else {
			assert_(streets.size() <= maxstr, "streets");
		}
		if (!GameUtil.isHilo(hand.game.type)) {
			assert_(!hand.showdownNoLow, "no low for non hilo");
		}
		if (GameUtil.isDraw(hand.game.type)) {
			int d = GameUtil.getHoleCards(hand.game.type);
			assert_(hand.myDrawCards0.length == d, "draw0");
			for (int n = 1; n < 3; n++) {
				String[] c = hand.myDrawCards(n);
				assert_(c == null || c.length == d, "draw: " + Arrays.toString(c));
			}
		}
	}
	
	/**
	 * add a new street
	 */
	protected List<Action> newStreet () {
		println("new street");
		streets.add(new ArrayList<Action>());
		return currentStreet();
	}
	
	/**
	 * put in pot - update running pot with seat pips
	 */
	protected void pip () {
		for (Seat seat : seatsMap.values()) {
			int pip = seatPip[seat.num];
			if (pip > 0) {
				println("seat " + seat + " pip " + pip);
				pot += pip;
				seat.pip += pip;
				seatPip[seat.num] = 0;
			}
		}
		// could check if pip amounts are same for those not all in
		println("pip: pot now " + pot);
	}
	
	/**
	 * put an amount in the running pot anonymously (dead blinds and antes).
	 * also updates hand.db
	 */
	protected void anonPip (int amount) {
		assert_(amount > 0, "am > 0");
		pot += amount;
		hand.db += amount;
		println("anonPip: pot now " + pot);
	}
	
	/**
	 * literally steal money from the running pot
	 */
	protected void anonPop (int amount) {
		assert_(amount > 0, "am > 0");
		pot -= amount;
		println("anonPop: pot now " + pot);
	}
	
	protected int pot () {
		return pot;
	}
	
	/**
	 * get pip total for seat for this street (before pip() is called)
	 */
	protected int seatPip (Seat seat) {
		return seatPip[seat.num];
	}
	
	/**
	 * seat puts in pot amount
	 */
	protected void seatPip (Seat seat, int amount) {
		seatPip[seat.num] += amount;
	}
	
	private void validatePot () {
		println("hand pot=" + hand.pot + " rake=" + hand.rake + " antes=" + hand.db);
		// validate pot size
		assert_(hand.pot == pot, "total pot " + hand.pot + " equal to running pot " + pot);
		
		int won = 0;
		int pip = 0;
		for (Seat seat : seatsMap.values()) {
			won += seat.won;
			pip += seat.pip;
		}
		println("won=" + won + " pip=" + pip);
		
		assert_(won == (hand.pot - hand.rake), "sum(seat.won) " + won + " equal to running pot " + pot + " - rake " + hand.rake);
		assert_(won == (pip - hand.rake + hand.db), "sum(seat.won) " + won + " equal to sum(seat.pip) " + pip + " - rake " + hand.rake
				+ " + antes " + hand.db);
		
		int asum = -hand.rake;
		for (List<Action> str : streets) {
			for (Action ac : str) {
				if (ac.amount != 0) {
					println("actsum " + ac);
					asum += ac.amount;
				}
			}
		}
		assert_(asum == 0, "actsum " + asum + " = zero");
		// could check if seat pip = seat pot then one action is all in
	}
	
}
