package pet.hp.info;

import java.util.*;

import pet.eq.Poker;
import pet.hp.*;

/**
 * Provides extra information about a hand suitable for displaying in table,
 * such as position, number to flop, etc, particularly from the point of view of
 * the current player
 */
public class HandInfo {

	/** convert hands to hand infos */
	public static List<HandInfo> getHandInfos(List<Hand> hands) {
		List<HandInfo> handInfos = new ArrayList<>();
		for (Hand h : hands) {
			handInfos.add(new HandInfo(h));
		}
		return handInfos;
	}

	public final Hand hand;
	/** the current players hole cards */
	private HoleCards hole;
	/** the current players hand rank */
	private HandValue rank;

	/** create hand info for the given hand */
	public HandInfo(Hand hand) {
		this.hand = hand;
	}
	
	public HoleCards mydowncards() {
		if (hole == null) {
			// hole cards for my seat should never be null
			String[] cards = hand.myseat.downCards;
			if (cards.length == 3) {
				// just show first two for stud
				cards = Arrays.copyOf(cards, 2);
			}
			hole = new HoleCards(cards);
		}
		return hole;
	}
	
	public HandValue rank() {
		if (rank == null) {
			Poker p = GameUtil.getPoker(hand.game.type);
			// XXX high only...
			int v = p.value(hand.board, hand.myseat.cards());
			rank = new HandValue(v);
		}
		return rank;
	}

	/**
	 * Calculate position of player where 0=button
	 */
	public int mypos() {
		// but, but+1, ... btn+5 (utg) (sb) (bb)
		Seat[] seats = hand.seats;
		int p;
		// first find button seat (note the actual button may not be at any live seat)
		for (int s = 0; s < seats.length; s++) {
			if (seats[s].num >= hand.button) {
				// now find how far we are from it
				for (p = 0; p < seats.length; p++) {
					if (seats[(s + p) % seats.length] == hand.myseat) {
						// p is distance between button and me clockwise
						// so invert it
						return p == 0 ? 0 : seats.length - p;
					}
				}
			}
		}
		return -1;
	}

	/**
	 * Position and extra relative information
	 */
	public String myposdesc() {
		int p = mypos();
		boolean bb = hand.myseat.bigblind;
		boolean sb = hand.myseat.smallblind;
		boolean utg = false;
		// utg is first to act after posts
		Action[] acts = hand.streets[0];
		for (int n = 0; n < acts.length; n++) {
			Action act = acts[n];
			if (act.type != Action.Type.POST && act.type != Action.Type.ANTE) {
				utg = act.seat == hand.myseat;
				break;
			}
		}
		return p + (bb ? " (bb)" : "") + (sb ? " (sb)" : "") + (utg ? " (utg)" : "");
	}

	/**
	 * number of people who saw flop (second street)
	 */
	public int numtoflop() {
		// a ch, b bet, c call, a call
		if (hand.streets.length >= 2) {
			int a = 0;
			Action[] acts = hand.streets[1];
			for (int n = 0; n < acts.length; n++) {
				Action act = acts[n];
				// seat bit mask
				int m = 1 << act.seat.num;
				if ((a & m) != 0) {
					// someone's acted twice
					return n;
				} else {
					// mark seat as having acted
					a |= m;
				}
			}
			// everyone checked
			return acts.length;
		}
		return 0;
	}

	/**
	 * was player last to act on flop?
	 * return 0 = not in flop, 1 = last, -1 = not last
	 */
	public int lastonflop() {
		// a ch, b bet, c call, a call
		if (hand.streets.length >= 2) {
			int a = 0;
			boolean found = false;
			Action[] acts = hand.streets[1];
			for (int n = 0; n < acts.length; n++) {
				Action act = acts[n];
				if (act.seat == hand.myseat) {
					found = true;
				}
				// seat bit mask
				int m = 1 << act.seat.num;
				if ((a & m) != 0) {
					// someone's acted twice
					// was prev me?
					return found ? acts[n - 1].seat == hand.myseat ? 1 : -1 : 0;
				} else {
					// mark seat as having acted
					a |= m;
				}
			}
			// everyone checked
			return found ? acts[acts.length - 1].seat == hand.myseat ? 1 : -1 : 0;
		}
		return 0;
	}

	/**
	 * amount player won in this hand
	 */
	public int myvalue() {
		return hand.myseat.won - hand.myseat.pip;
	}

	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("button " + hand.button + "\n");
		sb.append("game " + hand.game + "\n");
		sb.append("date " + hand.date + "\n");
		sb.append("original draw cards " + Arrays.toString(hand.myDrawCards0) + "\n");
		sb.append("id " + HandUtil.getId(hand) + "\n");
		sb.append("room " + HandUtil.getRoom(hand) + "\n");
		sb.append("pot " + hand.pot + "\n");
		sb.append("rake " + hand.rake + "\n");
		sb.append("showdown " + hand.showdown + "\n");
		sb.append("table " + hand.tablename + "\n");
		
		for (Seat seat : hand.seats) {
			sb.append("  seat " + seat + "\n");
		}
		
		for (int s = 0; s < hand.streets.length; s++) {
			sb.append("street " + s + " board " + Arrays.toString(HandUtil.getStreetBoard(hand, s)) + "\n");
			for (Action a : hand.streets[s]) {
				sb.append("  act " + a + "\n");
			}
		}
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return "HandInfo[" + HandUtil.getId(hand) + "]";
	}
}
