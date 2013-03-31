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
	protected final Map<String,Seat> seatsMap = new TreeMap<>();
	/** hand reached showdown */
	protected boolean showdown;
	/** streets of action for current hand */
	protected final List<List<Action>> streets = new ArrayList<>();
	/** is in summary phase */
	protected boolean summaryPhase;
	
	public Parser2(History history) {
		super(history);
	}
	
	protected void assert_ (boolean cond, String desc) {
		if (!cond) {
			throw new RuntimeException("Assertion failed: " + desc);
		}
	}
	
	@Override
	public void clear() {
		super.clear();
		streets.clear();
		seatsMap.clear();
		Arrays.fill(seatPip, 0);
		pot = 0;
		hand = null;
		sbposted = false;
	}
	
	protected List<Action> currentStreet() {
		return streets.get(streets.size() - 1);
	}
	
	/**
	 * get the current street number, starting at 0
	 */
	protected int currentStreetIndex() {
		return streets.size() - 1;
	}
	
	protected void fail (String desc) {
		throw new RuntimeException("Failure: " + desc);
	}
	
	/**
	 * validate and finalise hand
	 */
	protected void finish() {
		assert_ (hand.date != 0, "has date");
		assert_ (hand.button != 0, "has button");
		assert_ (hand.game != null, "has game");
		assert_ (hand.id != 0, "has id");
		assert_ (hand.myseat != null, "has my seat");
		
		// finalise hand and return
		validatePot();
		
		// get seats
		hand.seats = seatsMap.values().toArray(new Seat[seatsMap.size()]);
		Arrays.sort(hand.seats, HandUtil.seatCmp);
		
		// get actions
		hand.streets = new Action[streets.size()][];
		for (int n = 0; n < streets.size(); n++) {
			List<Action> street = streets.get(n);
			hand.streets[n] = street.toArray(new Action[street.size()]);
		}
		
		hand.showdown = showdown;
		
		history.addHand(hand);
		if (hand.tourn != null) {
			history.addTournPlayers(hand.tourn.id, seatsMap.keySet());
		}
		
		println("end of hand " + hand);
		
		clear();
	}
	
	/**
	 * add a new street
	 */
	protected List<Action> newStreet() {
		println("new street");
		streets.add(new ArrayList<Action>());
		return currentStreet();
	}
	
	/**
	 * put in pot - update running pot with seat pips
	 */
	protected void pip() {
		for (Seat seat : seatsMap.values()) {
			int pip = seatPip[seat.num];
			if (pip > 0) {
				println("seat " + seat + " pip " + pip); 
				pot += pip;
				seat.pip += pip;
				seatPip[seat.num] = 0;
			}
		}
		println("pot now " + pot);
	}

	/**
	 * put an amount in the pot without using the player pip
	 */
	protected void post(int amount) {
		assert_ (amount > 0, "am > 0");
		pot += amount;
	}
	
	protected int pot() {
		return pot;
	}

	/**
	 * get pip total for seat
	 */
	protected int seatPip(Seat seat) {
		return seatPip[seat.num];
	}
	
	/**
	 * seat puts in pot amount
	 */
	protected void seatPip(Seat seat, int amount) {
		seatPip[seat.num] += amount;
	}
	
	protected void validatePot() {
		// validate pot size
		assert_(hand.pot == pot, "total pot " + hand.pot + " equal to running pot " + pot);
		
		int won = 0;
		int lost = 0;
		for (Seat seat : seatsMap.values()) {
			won += seat.won;
			lost += seat.pip;
		}
		assert_(won == (hand.pot - hand.rake), "pot " + pot + " equal to total won " + won + " - rake " + hand.rake);
		assert_(won == (lost - hand.rake + hand.antes), "won " + won + " equal to lost " + lost);
		
		int asum = hand.antes - hand.rake;
		for (List<Action> str : streets) {
			for (Action ac : str) {
				asum += ac.amount;
			}
		}
		assert_(asum == 0, "actsum " + asum + " zero");
		
	}
	
}
