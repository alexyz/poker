package pet.hp.info;

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
	
	/** number of hands that were won at showdown */
	private int handswonshow;
	/** hands that went to showdown */
	private int showdownsseen;
	private int checkfold, checkcall, checkraise;
	/** number of times each action performed */
	private final int[] actionCount = new int[Action.TYPES];
	/** total amount of each action performed */
	private final int[] actionAmount = new int[Action.TYPES];
	private final int[] streetinits;
	private final int[] streetsseen;
	
	public PlayerGameInfo(PlayerInfo player, Game game) {
		this.player = player;
		this.game = game;
		int s = GameUtil.getMaxStreets(game.type);
		streetinits = new int[s];
		streetsseen = new int[s];
	}
	
	/**
	 * add this hand to the players game info
	 */
	public void add(Hand hand, Seat seat) {
		hands++;
		pip += seat.pip;
		
		if (seat.showdown) {
			showdownsseen++;
			if (seat.won > 0) {
				handswonshow++;
			}
		}
		
		if (seat.won > 0) {
			handswon++;
			won += seat.won;
			// over counts rake if split
			rake += hand.rake;
		}
		
		// update player game info with actions
		for (int streetno = 0; streetno < hand.streets.length; streetno++) {
			Action[] street = hand.streets[streetno];
			streetsseen[streetno]++;
			
			Action init = null;
			boolean hasChecked = false;
			for (Action act : street) {
				if (act.seat == seat) {
					addAction(act, hasChecked);
					if (act.type == Action.CHECK_TYPE) {
						hasChecked = true;
					}
					if (act.type == Action.FOLD_TYPE) {
						// no more actions for us
						return;
					}
				}
				if (act.type == Action.BET_TYPE || act.type == Action.RAISE_TYPE) {
					// last action on street with initiative
					init = act;
				}
			}

			// initiative
			// TODO sustained initiative, fold to init?
			if (init != null && init.seat == seat) {
				streetinits[streetno]++;
			}
		}

	}
	
	/**
	 * Add action on street
	 */
	private void addAction(Action action, boolean hasChecked) {
		actionCount[action.type]++;
		actionAmount[action.type] += action.amount;
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
	
	//
	// derived methods
	//
	
	// initiative
	// play cbet freq
	
	/**
	 * initiatives per street
	 */
	public String isstr() {
		StringBuilder sb = new StringBuilder();
		for (int n = 0; n < streetsseen.length; n++) {
			if (sb.length() > 0) {
				sb.append("-");
			}
			float i = (streetinits[n] * 100f) / streetsseen[n];
			sb.append(String.format("%2.0f", i));
		}
		return sb.toString();
	}
	
	/**
	 * flops seen as percentage of hands
	 */
	public float fs() {
		return (streetsseen[1] * 100f) / hands;
	}
	
	/**
	 * show downs seen as percentage of all hands
	 */
	public float ss() {
		return (showdownsseen * 100f) / hands;
	}
	
	/**
	 * show downs won as percentage of all show downs
	 */
	public float sw() {
		return (handswonshow * 100f) / showdownsseen;
	}
	
	/**
	 * hands won as percentage of all hands
	 */
	public float hw() {
		return (handswon*100f) / hands;
	}
	
	/**
	 * check, check fold, check call, check raise count
	 */
	public String cx() {
		int checks = actionCount[Action.CHECK_TYPE];
		return String.format("%d-%d-%d-%d", checks, checkfold, checkcall, checkraise);
	}
	
	/**
	 * check fold, check call, check raise to checks ratio
	 */
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
		sb.append("Show downs:  " + showdownsseen + "\n");
		sb.append("Showdown wins:  " + handswonshow + "\n");
		return sb.toString();
	}

}
