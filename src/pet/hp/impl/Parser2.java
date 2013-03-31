package pet.hp.impl;

import java.util.*;

import pet.hp.*;


public abstract class Parser2 extends Parser {

	// stuff for current hand, cleared on clear()
	/** map of player name to seat for current hand */
	protected final Map<String,Seat> seatsMap = new TreeMap<>();
	/** array of seat num to seat pip for this street. seat numbers are 1-10 */
	protected final int[] seatPip = new int[11];
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
	/** hand game instance */
	protected Game game;
	
	public Parser2(History history) {
		super(history);
	}
	
}
