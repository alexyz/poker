package pet.hp;

import java.io.Serializable;

/**
 * An action that a player performs during a hand
 */
public class Action implements Serializable {
	private static final long serialVersionUID = 1;
	/** seat performing the action */
	public Seat seat;
	/** action string */
	public String type;
	/** amount put in pot */
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