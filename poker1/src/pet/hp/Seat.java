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
	/** amount bet as blind */
	//int blind;
	/** seats final hole cards */
	public String[] hole;
	/** cards drawn as { prev hand, kept, drawn } */
	public String[][] drawn; // 99876 { prev hand, kept, drawn }
	/** amount won */
	public int won;
	/** amount put in pot, equal to sum of amount of player actions in hand */
	public int pip;
	/** any uncalled amount returned to seat */
	public int uncalled;
	/** number of cards discarded */
	public int discards;
	/** true if seat won without a showdown */
	public boolean defaultwin;
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