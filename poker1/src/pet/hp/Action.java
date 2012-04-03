package pet.hp;

import java.io.Serializable;

/**
 * An action that a player performs during a hand
 */
public class Action implements Serializable {
	
	public static final String[] TYPENAME = new String[] { 
		null, "check", "fold", "raise", "call", "bet", "post",
		"muck", "doesn't show", "show", "draw", "stand pat" 
	};
	public static final byte CHECK_TYPE = 1;
	public static final byte FOLD_TYPE = 2;
	public static final byte RAISE_TYPE = 3;
	public static final byte CALL_TYPE = 4;
	public static final byte BET_TYPE = 5;
	public static final byte POST_TYPE = 6;
	public static final byte MUCK_TYPE = 7;
	public static final byte DOESNTSHOW_TYPE = 8;
	public static final byte SHOW_TYPE = 9;
	public static final byte DRAW_TYPE = 10;
	public static final byte STANDPAT_TYPE = 11;
	public static final int TYPES = 12;
	
	private static final long serialVersionUID = 1;
	
	/** seat performing the action */
	public Seat seat;
	/** action type */
	public byte type;
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
