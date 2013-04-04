package pet.hp;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.*;

/**
 * represents a single hand at a table.
 * No analysis - see HandUtil, HandInfo and HandState.
 * This object should be considered immutable.
 */
public class Hand implements Serializable {
	
	public static final long ROOM = 0xfL << 60;
	public static final long PS_ROOM = 1L << 60;
	public static final long FT_ROOM = 2L << 60;
	
	/**
	 * hand id combined with poker room - object so it can be used in map/set
	 * without being recreated
	 */
	public Long id;
	/** game type */
	public Game game;
	/** tournament instance */
	public Tourn tourn;
	/** tournament round */
	public int round;
	/** tournament level */
	// XXX should be short as there are never more than 100 or so levels
	public int level;
	/** hand date, as long to save memory */
	public long date;
	/** formal big blind amount (may not be posted) */
	public int bb;
	/** formal small blind amount (may not be posted) */
	public int sb;
	/** formal ante amount (may not be posted) */
	public int ante;
	/** actual dead blinds (including antes, dead blinds) */
	public int db = 0;
	/** participants in hand in seat order (no null elements, missing empty seats) */
	public Seat[] seats;
	/** current players seat */
	public Seat myseat;
	/** action on each street */
	public Action[][] streets;
	/** did hand reach showdown (can't just count streets as could have folded on river) */
	public boolean showdown;
	/** showdown for hi/lo game type did not have a low hand. only use if game type is hilo */
	public boolean showdownNoLow;
	/** community cards if any */
	public String[] board;
	/** total pot size */
	public int pot;
	/** pokerstars wealth delta */
	public int rake;
	/**
	 * Draw games only - original and subsequent hole cards dealt to
	 * player. since we only ever know the original cards for current player,
	 * these are here and not on current players seat instance.
	 */
	// TODO should probably be [][]
	public String[] myDrawCards0, myDrawCards1, myDrawCards2, myDrawCards3;
	/** name of table */
	public String tablename;
	/** button seat number. note that stud games do not have a button */
	public byte button;
	/** the street index the deck was reshuffled on (default max, i.e. never reshuffled) */
	public byte reshuffleStreetIndex = Byte.MAX_VALUE;
	
	public Hand() {
		//
	}
	
	public String[] myDrawCards(int n) {
		switch (n) {
			case 0: return myDrawCards0;
			case 1: return myDrawCards1;
			case 2: return myDrawCards2;
			case 3: return myDrawCards3;
			default: throw new RuntimeException("no such draw " + n);
		}
	}
	
	// XXX should be like method above to be more regular
	public void addMyDrawCards(String[] h) {
		if (myDrawCards0 == null) {
			// first hand for all games
			myDrawCards0 = h;
			
		} else if (myDrawCards1 == null) {
			// second hand for draw and triple draw
			myDrawCards1 = h;
			
		} else if (myDrawCards2 == null) {
			// third hand for triple draw
			myDrawCards2 = h;
			
		} else if (myDrawCards3 == null) {
			// fourth and final hand for triple draw
			myDrawCards3 = h;
			
		} else {
			throw new RuntimeException("too many hole cards");
		}
	}

	@Override
	public String toString() {
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		return String.format("Hand[%s '%s' at '%s' on %s seats=%s str=%d]", 
				id & ~ROOM, game, tablename, df.format(new Date(date)), seats != null ? seats.length : -1, streets != null ? streets.length : -1);
	}
	
}
