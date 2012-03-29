package pet.hp.util;

import java.util.*;

import pet.hp.HandUtil;

public class PlayerGameInfo {
	public final PlayerInfo player;
	public final String gameName;
	public int rake = 0;
	/** hands in this game */
	public int hands;
	/** hands where the player won something */
	public int handswon = 0;
	/** amount won and lost */
	public int won = 0;
	public int pip = 0;
	final int[] foldedon;
	/** number of hands that were won at showdown */
	public int handswonshow;
	/** hands that went to showdown and were shown (should be all hands) */
	public int showdown;
	
	// not public
	
	/** action map: int[] { count, amount } */
	final Map<String,int[]> amap = new TreeMap<String,int[]>();
	final char type;
	
	public PlayerGameInfo(PlayerInfo player, String name, char type) {
		this.player = player;
		this.gameName = name;
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
		sb.append(String.format("Hands:  %d  Won:  %d\n", hands, handswon));
		sb.append(String.format("Amount won:  %d  Lost:  %d\n", won, pip));
		sb.append("Actions:\n");
		for (Map.Entry<String,int[]> e : amap.entrySet()) {
			int[] c = e.getValue();
			if (c[0] > 0) {
				String act = e.getKey();
				sb.append("  " + act + " times: " + c[0]);
				if (c[1] > 0) {
					sb.append(" amount: " + c[1]);
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
		sb.append("Show downs:  " + showdown + "\n");
		sb.append("Showdown wins:  " + handswonshow + "\n");
		return sb.toString();
	}
}
