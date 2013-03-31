package pet.hp.impl;

import java.util.*;

import pet.hp.*;

/**
 * common utilities for parsers
 */
public abstract class Parser2 extends Parser {

	// stuff for current hand, cleared on clear()
	// TODO should all be private...
	
	/** map of player name to seat for current hand */
	protected final Map<String,Seat> seatsMap = new TreeMap<>();
	/** array of seat num to seat pip for this street. seat numbers are 1-10 */
	private final int[] seatPip = new int[11];
	/** running pot - updated when pip() is called */
	protected int pot;
	/** streets of action for current hand */
	protected final List<List<Action>> streets = new ArrayList<>();
	/** current hand */
	protected Hand hand;
	/** hand reached showdown */
	protected boolean showdown;
	/** is in summary phase */
	protected boolean summaryPhase;
	/** has live sb been posted (others are dead) */
	protected boolean sbposted;
	
	/**
	 * get the current street number, starting at 0
	 */
	protected int currentStreetIndex() {
		return streets.size() - 1;
	}
	
	/**
	 * add a new street
	 */
	protected List<Action> newStreet() {
		println("new street");
		streets.add(new ArrayList<Action>());
		return currentStreet();
	}
	
	protected List<Action> currentStreet() {
		return streets.get(streets.size() - 1);
	}

	/**
	 * seat puts in pot amount
	 */
	protected void seatPip(Seat seat, int amount) {
		seatPip[seat.num] += amount;
	}
	
	/**
	 * get pip total for seat
	 */
	protected int seatPip(Seat seat) {
		return seatPip[seat.num];
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
	
	public Parser2(History history) {
		super(history);
	}
	
}
