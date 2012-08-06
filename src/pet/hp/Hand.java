package pet.hp;

import java.io.Serializable;
import java.util.*;

/**
 * represents a single hand at a table.
 * No analysis - see HandUtil, HandInfo and HandState.
 * This object should be considered immutable.
 */
public class Hand implements Serializable {
	/** hand id - object so it can be used in map/set */
	public final Long id;
	/** game type */
	public Game game;
	/** tournament instance */
	public Tourn tourn;
	/** tournament round */
	public int round;
	/** tournament level */
	public int level;
	/** hand date */
	public Date date;
	/** big blind amount (may not be posted) */
	public int bb;
	/** small blind amount (may not be posted) */
	public int sb;
	/** dead blinds posted */
	public int antes;
	/** participants in hand in seat order */
	public Seat[] seats;
	/** current players seat */
	public Seat myseat;
	/** action on each street */
	public Action[][] streets;
	/** did hand reach showdown (can't just count streets as could have folded on river) */
	public boolean showdown;
	/** community cards if any */
	public String[] board;
	/** total pot size */
	public int pot;
	/** pokerstars wealth delta */
	public int rake;
	/**
	 * original (all games) and subsequent (draw games) hole cards dealt to
	 * player. since we only ever know the original cards for current player,
	 * this is on hand and not seat.
	 */
	public String[] myHoleCards0, myHoleCards1, myHoleCards2, myHoleCards3;
	/** name of table */
	public String tablename;
	/** button seat number */
	public int button;
	
	public Hand(long id) {
		this.id = id;
	}
	
	public String[] myHoleCards(int n) {
		switch (n) {
			case 0: return myHoleCards0;
			case 1: return myHoleCards1;
			case 2: return myHoleCards2;
			case 3: return myHoleCards3;
			default: throw new RuntimeException();
		}
	}
	
	public void addMyHoleCards(String[] h) {
		if (myHoleCards0 == null) {
			// first hand for all games
			myHoleCards0 = h;
			
		} else if (myHoleCards1 == null) {
			// second hand for draw and triple draw
			myHoleCards1 = h;
			
		} else if (myHoleCards2 == null) {
			// third hand for triple draw
			myHoleCards2 = h;
			
		} else if (myHoleCards3 == null) {
			// fourth and final hand for triple draw
			myHoleCards3 = h;
			
		} else {
			throw new RuntimeException("too many hole cards");
		}
	}

	@Override
	public String toString() {
		return String.format("Hand[%s '%s' at '%s' on %s seats=%s str=%d]", 
				id, game, tablename, date, seats != null ? seats.length : -1, streets != null ? streets.length : -1);
	}
	
}
