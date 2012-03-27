package pet.hp;

import java.io.Serializable;
import java.util.*;

/**
 * represents a single hand at a table
 */
public class Hand implements Serializable {
	private static final long serialVersionUID = 1;
	/** hand id */
	public long id;
	/** game name including blinds */
	public String gamename;
	/** hand date */
	public Date date;
	/** hand currency, $ or 0 */
	public char currency;
	/** participants in hand in seat order */
	public Seat[] seats;
	/** current players seat */
	public Seat myseat;
	/** action on each street */
	public Action[][] streets;
	/** did hand reach showdown (could have folded on river) */
	public boolean showdown;
	/** community cards if any */
	public String[] board;
	/** total pot size */
	public int pot;
	/** pokerstars wealth delta */
	public int rake;
	/** cards discarded by current player (can never see opponents). probably not needed with seat.drawn */
	public String[] mydiscard;
	/** name of table */
	public String tablename;
	/** max number of players at table */
	public int max;
	/** button seat number */
	public int button;
	/** type of game for street and hand analysis purposes */
	public char gametype; 
	@Override
	public String toString() {
		return String.format("Game[%s '%s' at '%s' on %s seats=%s str=%d]", 
				id, gamename, tablename, date, seats != null ? seats.length : -1, streets != null ? streets.length : -1);
	}
	
}
