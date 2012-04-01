package pet.hp;

import java.io.Serializable;

/**
 * An action that a player performs during a hand
 */
public class Action implements Serializable {
	
	// XXX these are stars specific...
	public static final String CHECK_TYPE = "checks";
	public static final String FOLD_TYPE = "folds";
	public static final String RAISE_TYPE = "raises";
	public static final String CALL_TYPE = "calls";
	public static final String BET_TYPE = "bets";
	public static final String POST_TYPE = "posts";
	private static final long serialVersionUID = 1;
	
	/** seat performing the action */
	public Seat seat;
	/** action string */
	// make this byte to save space?
	public String type;
	/** amount put in pot - note that not all bets/raises will be called */
	public int amount;
	/** this action put the player all in */
	public boolean allin;
	@Override
	public String toString() {
		String s = seat.name + " " + type;
		if (amount > 0) {
			s += " " + amount;
		}
		if (allin) {
			s += " all in";
		}
		return s;
	}
}
