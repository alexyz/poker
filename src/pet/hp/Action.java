
package pet.hp;

import java.io.Serializable;

/**
 * An action that a player performs during a hand. This object should be
 * considered immutable.
 */
public class Action implements Serializable {
	
	public static String getTypeName (Type type) {
		switch (type) {
			case ANTE:
				return "ante";
			case BET:
				return "bet";
			case BRINGSIN:
				return "bring in";
			case CALL:
				return "call";
			case CHECK:
				return "check";
			case COLLECT:
				return "collect";
			case DOESNTSHOW:
				return "doesn't show";
			case DRAW:
				return "draw";
			case FOLD:
				return "fold";
			case MUCK:
				return "muck";
			case POST:
				return "post";
			case RAISE:
				return "raise";
			case SHOW:
				return "show";
			case STANDPAT:
				return "stand pat";
			case UNCALL:
				return "uncall";
			default:
				throw new RuntimeException();
		}
	}
	
	public enum Type {
		CHECK, FOLD, RAISE, CALL, BET, 
		/** post blind, not ante */
		POST, MUCK, DOESNTSHOW, SHOW, DRAW, STANDPAT,
		/** uncalled bet returned to player */
		UNCALL,
		/** win */
		COLLECT, BRINGSIN,
		/** posts ante */
		ANTE,
	}
	
	public Action(Seat seat) {
		this.seat = seat;
	}
	
	/** seat performing the action */
	public final Seat seat;
	/** action type */
	public Action.Type type;
	/** amount put in pot - can be negative if getting a refund */
	public int amount;
	/** this action put the player all in */
	public boolean allin;
	
	/**
	 * return string rep of action
	 */
	@Override
	public String toString () {
		String s = seat.name + " " + Action.getTypeName(type);
		if (amount != 0) {
			s += " " + amount;
		}
		if (allin) {
			s += " all in";
		}
		return s;
	}
}
