package pet.hp.util;

import java.util.*;

import pet.hp.*;

public class PlayerGameInfo {
	public final PlayerInfo player;
	public final Game game;
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
	public int checkfold, checkcall, checkraise;
	
	// not public
	
	/** number of times each action performed */
	final int[] actionCount = new int[Action.TYPES];
	/** total amount of each action performed */
	final int[] actionAmount = new int[Action.TYPES];
	
	public PlayerGameInfo(PlayerInfo player, Game game) {
		this.player = player;
		this.game = game;
		this.foldedon = new int[HandUtil.getMaxStreets(game.type)];
	}
	
	public void add(Seat s, Hand h) {
		hands++;
		pip += s.pip;
		if (s.showdown) {
			showdown++;
		}
		if (s.won > 0) {
			handswon++;
			won += s.won;
			// overcounts rake if split?
			rake += h.rake;
		}
		if (s.showdown && s.won > 0) {
			handswonshow++;
		}
	}
	
	/**
	 * Add action on street
	 */
	void addAction(int street, Action action, boolean hasChecked) {
		actionCount[action.type]++;
		actionAmount[action.type] += action.amount;
		
		if (action.type == Action.FOLD_TYPE) {
			foldedon[street]++;
		}
		
		if (hasChecked) {
			if (action.type == Action.FOLD_TYPE) {
				checkfold++;
			} else if (action.type == Action.CALL_TYPE) {
				checkcall++;
			} else if (action.type == Action.RAISE_TYPE) {
				checkraise++;
			}
		}
	}
	
	public String cx() {
		int checks = actionCount[Action.CHECK_TYPE];
		return String.format("%d-%d-%d-%d", checks, checkfold, checkcall, checkraise);
	}
	
	public String cxr() {
		int checks = actionCount[Action.CHECK_TYPE];
		if (checks > 0) {
			float f = (checkfold * 100f) / checks;
			float c = (checkcall * 100f) / checks;
			float r = (checkraise * 100f) / checks;
			return String.format("%2.0f-%2.0f-%2.0f", f, c, r);
		} else {
			return "";
		}
	}
	
	public int getActionSum(byte actiontype) {
		return actionAmount[actiontype];
	}
	
	// player c/r freq
	// play cbet freq
	// player agr fac
	
	/** aggression factor count */
	public float afcount() {
		return af(actionCount);
	}
	
	/** aggression factor volume */
	public float afam() {
		return af(actionAmount);
	}
	
	private float af(int[] a) {
		// amount bet+raise / call
		int b = a[Action.BET_TYPE];
		int c = a[Action.CALL_TYPE];
		int r = a[Action.RAISE_TYPE];
		int ch = a[Action.CHECK_TYPE];
		return (b + r + 0f) / (c + ch);
	}
	
	@Override
	public String toString() {
		return "PlayerGameInfo[game=" + game + " hands=" + hands + "]";
	}
	
	public String toLongString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Hands:  %d  Won:  %d\n", hands, handswon));
		sb.append(String.format("Amount won:  %d  Lost:  %d\n", won, pip));
		sb.append("Actions:\n");
		for (int n = 0; n < actionCount.length; n++) {
			if (actionCount[n] > 0) {
				sb.append("  " + Action.TYPENAME[n] + " times: " + actionCount[n]);
				if (actionAmount[n] > 0) {
					sb.append(" amount: " + actionAmount[n]);
				}
				sb.append("\n");
			}
		}
		sb.append("Folded on:\n");
		for (int s = 0; s < foldedon.length; s++) {
			if (foldedon[s] > 0) {
				sb.append("  " + HandUtil.getStreetName(game.type, s) + ":  " + foldedon[s] + "\n");
			}
		}
		sb.append("Show downs:  " + showdown + "\n");
		sb.append("Showdown wins:  " + handswonshow + "\n");
		return sb.toString();
	}
}
