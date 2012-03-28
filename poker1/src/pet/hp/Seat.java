package pet.hp;

import java.io.Serializable;
import java.util.Arrays;

public class Seat implements Serializable {
	private static final long serialVersionUID = 1;
	/** seat number */
	public int num;
	/** player name */
	public String name;
	/** starting chips */
	public int chips;
	/** seats final hole cards */
	public String[] hole;
	/** cards drawn as { prev hand, kept, drawn } */
	public String[][] drawn; // 99876 { prev hand, kept, drawn }
	/** amount won */
	public int won;
	/** amount put in pot, equal to sum of amount of player actions in hand */
	public int pip;
	/** number of cards discarded */
	public int discards;
	/** seats hand reached showdown */
	public boolean showdown;
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