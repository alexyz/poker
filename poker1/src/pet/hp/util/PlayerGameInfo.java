package pet.hp.util;

import java.util.*;

import pet.hp.HandUtil;

public class PlayerGameInfo {
	final char type;
	int rake = 0;
	/** number of seats player had */
	int hands = 0;
	/** hands where the player won something */
	int woncount = 0;
	/** amount won and lost */
	int won = 0, lost = 0;
	final int[] foldedon;
	/** number of hands that were won without showdown */
	int defaultwin;
	/** hands that went to showdown and were shown (should be all hands) */
	int showdown;
	/** action map: int[] { count, amount } */
	final Map<String,int[]> amap = new TreeMap<String,int[]>();
	public PlayerGameInfo(char type) {
		this.type = type;
		this.foldedon = new int[HandUtil.getMaxStreets(type)];
	}
	int[] getAction(String action) {
		int[] c = amap.get(action);
		if (c == null) {
			amap.put(action, c = new int[2]);
		}
		return c;
	}
	@Override
	public String toString() {
		return "GameInfo[hands=" + hands + "]";
	}
	public String toLongString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Hands:  %d  Won:  %d\n", hands, woncount));
		sb.append(String.format("Amount won:  %d  Lost:  %d\n", won, lost));
		sb.append("Actions:\n");
		for (Map.Entry<String,int[]> e : amap.entrySet()) {
			int[] c = e.getValue();
			if (c[0] > 0) {
				String act = e.getKey();
				sb.append("  " + act + ":  " + c[0]);
				if (c[1] > 0) {
					sb.append("  amount: " + c[1]);
				}
				sb.append("\n");
			}
		}
		sb.append("Folded on:\n");
		for (int s = 0; s < foldedon.length; s++) {
			if (foldedon[s] > 0) {
				sb.append("  " + HandUtil.getStreetName(type, s) + ":  " + foldedon[s] + "\n");
			}
		}
		sb.append("Default wins:  " + defaultwin + "\n");
		sb.append("Show downs:  " + showdown);
		return sb.toString();
	}
}
