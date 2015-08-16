package pet.hp.info;

import pet.eq.*;
import pet.hp.*;

/**
 * statistics for a particular player and game.
 */
public class PlayerGameInfo {
	/** the player in question */
	public final PlayerInfo player;
	/** the game in question (from the hand parser) */
	public final Game game;

	/** how much rake this player contributed */
	public int rake = 0;
	/** hands for this game */
	public int hands;
	/** hands where the player won something */
	public int handswon = 0;
	/** total amount won (not lost) */
	public int won = 0;
	/** total amount put in pot (and called?) */
	public int pip = 0;
	
	/** number of hands won by rank */
	private final int[] rankwon;
	/** number of hands lost by rank */
	private final int[] ranklost;
	/** amount won - pip by rank */
	private final int[] rankam;
	
	/** number of times initiative was taken on each street */
	private final int[] streetinits;
	/** number of times each street was seen */
	private final int[] streetsseen;
	/** number of times each action performed */
	private final int[] actionCount = new int[Action.Type.values().length];
	
	/** number of hands where player raised preflop */
	private int pfr;
	/** number of hands where money voluntarily put in pot */
	private int vpip;
	/** number of hands that were won at show down */
	private int handswonshow;
	/** hands that went to show down */
	private int showdownsseen;
	/** number of times check/folded, called and raised */
	private int checkfold, checkcall, checkraise;
	

	/** create play game info for the given player and game */
	public PlayerGameInfo(PlayerInfo player, Game game) {
		this.player = player;
		this.game = game;

		int s = GameUtil.getStreets(game.type);
		streetinits = new int[s];
		streetsseen = new int[s];
		
		int r = GameUtil.getRanksHi(game.type).length;
		rankwon = new int[r];
		ranklost = new int[r];
		rankam = new int[r];
	}

	/**
	 * add this hand at this seat to the players game info
	 */
	public void add(Hand hand, Seat seat) {
		if (hand.game.type != game.type) {
			throw new RuntimeException();
		}
		
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
			if (hand.rake > 0) {
				int winners = 0;
				// divide rake if split pot
				for (Seat s : hand.seats) {
					if (s.won > 0) {
						winners++;
					}
				}
				rake += (hand.rake / winners);
			}
		}
		
		// count winning rank.. if you dare!
		if (seat.showdown && (!GameUtil.isHilo(game.type) || hand.showdownNoLow)) {
			Poker p = GameUtil.getPoker(game.type);
			String[] cards = seat.cards();
			int v = p.value(hand.board, cards);
			int r = Poker.rank(v);
			//System.out.println("cards: " + Arrays.toString(cards) + " value: " + Poker.valueString(v) + " rank: " + r);
			//(seat.wonMainHigh ? rankwon : ranklost)[r]++;
			(seat.won > 0 ? rankwon : ranklost)[r]++;
			int am = seat.won - seat.pip;
			rankam[r] += am;
			
			// TODO
			// count low ranks - but need to know which halfs we won for hi/lo split
		}

		boolean hasVpip = false;
		boolean hasPfr = false;

		// update player game info with actions
		streets: for (int s = 0; s < hand.streets.length; s++) {
			Action[] street = hand.streets[s];
			streetsseen[s]++;

			Action init = null;
			boolean hasChecked = false;

			for (Action act : street) {
				if (act.seat == seat) {
					actionCount[act.type.ordinal()]++;
					
					// has previously checked this street
					if (hasChecked) {
						if (act.type == Action.Type.FOLD) {
							checkfold++;
						} else if (act.type == Action.Type.CALL) {
							checkcall++;
						} else if (act.type == Action.Type.RAISE) {
							checkraise++;
						}
					}
					
					// pre flop raise
					if (s == 0 && act.type == Action.Type.RAISE) {
						hasPfr = true;
					}
					
					if (act.type == Action.Type.CHECK) {
						hasChecked = true;
					}
					
					// voluntarily put money in pot
					if (act.type != Action.Type.POST && act.amount > 0) {
						hasVpip = true;
					}
					
					if (act.type == Action.Type.FOLD) {
						// no more actions for us
						break streets;
					}
				}
				
				if (act.type == Action.Type.BET || act.type == Action.Type.RAISE) {
					// get last action on street with initiative
					init = act;
				}
			}

			// initiative
			// TODO sustained initiative, fold to init?
			if (init != null && init.seat == seat) {
				streetinits[s]++;
			}
		}

		if (hasVpip) {
			vpip++;
		}
		
		if (hasPfr) {
			pfr++;
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
		if (hands > 0) {
			return (showdownsseen * 100f) / hands;
		} else {
			return 0;
		}
	}

	/**
	 * show downs won as percentage of all show downs.
	 * returns null if this player has not seen any showdowns
	 */
	public Float sw() {
		if (showdownsseen > 0) {
			return (handswonshow * 100f) / showdownsseen;
		} else {
			return null;
		}
	}

	/**
	 * hands won as percentage of all hands
	 */
	public float hw() {
		if (hands > 0) {
			return (handswon*100f) / hands;
		} else {
			return 0;
		}
	}
	
	/**
	 * preflop raise as percentage of all hands
	 */
	public float pfr() {
		if (hands > 0) {
			return (pfr*100f) / hands;
		} else {
			return 0;
		}
	}

	/**
	 * check, check fold, check call, check raise count
	 */
	public String cx() {
		return String.format("%d-%d-%d", checkfold, checkcall, checkraise);
	}

	/**
	 * check fold, check call, check raise to checks ratio
	 */
	public String cxr() {
		int checks = actionCount[Action.Type.CHECK.ordinal()];
		if (checks > 0) {
			float f = (checkfold * 100f) / checks;
			float c = (checkcall * 100f) / checks;
			float r = (checkraise * 100f) / checks;
			return String.format("%2.0f-%2.0f-%2.0f", f, c, r);
		} else {
			return "";
		}
	}

	/** aggression factor count - afvol removed as it gets weird with all ins */
	public Float af() {
		return af(false);
	}
	
	/** aggression factor count */
	public Float af(boolean ch) {
		// amount bet+raise / call
		float a = actionCount[Action.Type.BET.ordinal()] + actionCount[Action.Type.RAISE.ordinal()];
		float p = actionCount[Action.Type.CALL.ordinal()];
		if (ch) {
				p += actionCount[Action.Type.CHECK.ordinal()];
		}
		if (p > 0) {
			return a / p;
		} else {
			return null;
		}
	}
	
	/**
	 * return vpip as percentage of hands
	 */
	public float vpip() {
		if (hands > 0) {
			return (vpip * 100f) / hands;
		} else {
			return 0;
		}
	}
	
	/**
	 * amount won
	 */
	public int am() {
		 return won - pip;
	}
	
	/**
	 * amount won per hand
	 */
	public float amph() {
		if (hands > 0) {
			return (am() * 1f) / hands;
		} else {
			return 0;
		}
	}

	@Override
	public String toString() {
		return "PlayerGameInfo[game=" + game + " hands=" + hands + "]";
	}

	public String toLongString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Hands:  " + hands + "  Hands Won:  " + handswon + "\n");
		sb.append("Amount won:  " + won + "  Put in pot:  " + pip + "  Rake:  " + rake + "\n");
		sb.append("Check-x count: " + cx() + "\n");
		sb.append("Check-x ratio: " + cxr() + "\n");
		sb.append("Initiatives: " + isstr() + "\n");
		sb.append("Show downs:  " + showdownsseen + "\n");
		sb.append("Show down wins:  " + handswonshow + "\n");

		sb.append("Actions:\n");
		for (int n = 0; n < actionCount.length; n++) {
			if (actionCount[n] > 0) {
				sb.append("  " + Action.Type.values()[n].desc + " times: " + actionCount[n]);
				sb.append("\n");
			}
		}
		
		sb.append("Showdown ranks (hi only for hi/lo):\n");
		String[] ranks = GameUtil.getRanksHi(game.type);
		for (int n = 0; n < ranks.length; n++) {
			sb.append("  ").append(ranks[n]);
			sb.append(" times won ").append(rankwon[n]);
			sb.append(" times lost ").append(ranklost[n]);
			sb.append(" amount ").append(rankam[n]);
			sb.append("\n");
		}
		return sb.toString();
	}

}
