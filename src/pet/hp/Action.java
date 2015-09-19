
package pet.hp;

import java.io.Serializable;

/**
 * An action that a player performs during a hand. This object should be
 * considered immutable.
 */
public class Action implements Serializable {
	
	/** action types */
	public enum Type {
		CHECK("check"), 
		FOLD("fold"), 
		RAISE("raise"), 
		CALL("call"), 
		BET("bet"), 
		/** post blind, not ante */
		POST("post"), 
		MUCK("muck"), 
		DOESNTSHOW("doesn't show"), 
		SHOW("show"), 
		DRAW("draw"), 
		STANDPAT("stand pat"),
		/** uncalled bet returned to player */
		UNCALL("returned"),
		/** win */
		COLLECT("collect"), 
		BRINGSIN("bring in"),
		/** posts ante */
		ANTE("ante");
		public String desc;
		private Type(String desc) {
			this.desc = desc;
		}
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
		String s = seat.name + " " + type.desc;
		if (amount != 0) {
			s += " " + amount;
		}
		if (allin) {
			s += " all in";
		}
		return s;
	}
}
