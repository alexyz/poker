package pet.hp;

/**
 * An action that a player performs during a hand
 */
public class Action {
	
	public static final String[] TYPENAME = new String[] { 
		null, "check", "fold", "raise", "call", "bet", "post",
		"muck", "doesn't show", "show", "draw", "stand pat", "uncall", "collect"
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
	public static final byte UNCALL_TYPE = 12;
	public static final byte COLLECT_TYPE = 13;
	/** total number of types */
	public static final int TYPES = 14;
	
	public Action(Seat seat) {
		this.seat = seat;
	}
	
	/** seat performing the action */
	public final Seat seat;
	/** action type */
	public byte type;
	/** amount put in pot - can be negative if getting a refund */
	public int amount;
	/** this action put the player all in */
	public boolean allin;
	
	@Override
	public String toString() {
		String s = seat.name + " " + TYPENAME[type];
		if (amount != 0) {
			s += " " + amount;
		}
		if (allin) {
			s += " all in";
		}
		return s;
	}
}
