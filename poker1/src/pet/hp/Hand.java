package pet.hp;

import java.io.Serializable;
import java.util.*;

/**
 * represents a single hand at a table.
 * No analysis - see HandUtil, HandInfo and HandState
 */
public class Hand implements Serializable {
	private static final long serialVersionUID = 1;
	/** hand id */
	public long id;
	/** game type */
	public Game game;
	/** hand date */
	public Date date;
	/** big blind (if any?) */
	public int bb;
	/** small blind (if any?) */
	public int sb;
	/** dead blinds */
	public int db;
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
	/** original hole cards dealt to player */
	public String[] myhole;
	/** name of table */
	public String tablename;
	/** button seat number */
	public int button;
	/** uncalled action amount */
	public int uncall;
	@Override
	public String toString() {
		return String.format("Hand[%s '%s' at '%s' on %s seats=%s str=%d]", 
				id, game, tablename, date, seats != null ? seats.length : -1, streets != null ? streets.length : -1);
	}
	
}
