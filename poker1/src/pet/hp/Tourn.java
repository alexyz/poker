package pet.hp;

import java.util.Date;

/**
 * represents a tournament.
 * This object should be considered immutable.
 */
public class Tourn {
	/** unique identifier */
	public final long id;
	/** prize buy in */
	public int buyin;
	/** cost of joining */
	public int cost;
	/** number of players - summary only */
	public int players;
	/** prize pool - summary only */
	public int pool;
	/** tournament start date - summary only */
	public Date date;
	/** buy in currency */
	public char currency;
	/** winner */
	public String winner;
	/** players final position */
	public int pos;
	/** players win amount */
	public int won;
	
	public Tourn(long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Tourn[id=" + id + " buyin=" + buyin + " cost=" + cost + " players=" + players + " pool=" + pool
				+ " date=" + date + " currency=" + currency + "]";
	}

}
