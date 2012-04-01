package pet.hp;

import java.io.Serializable;
import java.util.Arrays;

public class Seat implements Serializable {
	private static final long serialVersionUID = 1;
	/** seat number */
	public byte num;
	/** player name */
	public String name;
	/** starting chips */
	public int chips;
	/** seats final hole cards */
	public String[] hole;
	/** amount won */
	public int won;
	/** amount put in pot, equal to sum of amount of player actions in hand */
	public int pip;
	/** number of cards discarded */
	public byte discards;
	/** seats hand reached showdown */
	public boolean showdown;
	public boolean bigblind;
	public boolean smallblind;
	@Override
	public String toString() {
		String s = num + ":" + name + "(" + chips + ")";
		if (hole != null) {
			s += " " + Arrays.asList(hole);
		}
		if (discards > 0) {
			s += " discards " + discards;
		}
		return s;
	}
}