package pet.hp.state;

import pet.hp.Action;
import pet.hp.Hand;

/**
 * represents current state of hand.
 * cloned - do not duplicate references
 */
public class HandState implements Cloneable {
	public static final int NO_SEAT = -1;
	/** the hand the states were derived from */
	public final Hand hand;
	/** seats in hand (where seat 1 is index 0), elements can be null */
	public SeatState[] seats;
	/** community cards */
	public String[] board;
	/** current pot */
	public int pot;
	/** seat index of button */
	public int button;
	/** index of seat state of current action, NO_SEAT if no action */
	public int actionSeat;
	/** current action */
	public Action action;
	/** information */
	public String note;
	/** current street, starting at 0 */
	public int streetIndex;
	
	public HandState(Hand hand) {
		this.hand = hand;
		this.seats = new SeatState[hand.game.max];
	}
	
	public SeatState actionSeat() {
		return actionSeat >= 0 ? seats[actionSeat] : null;
	}

	@Override
	public HandState clone() {
		try {
			HandState hs = (HandState) super.clone();
			// deep copy of seat states
			hs.seats = hs.seats.clone();
			for (int n = 0; n < hs.seats.length; n++) {
				if (hs.seats[n] != null) {
					hs.seats[n] = hs.seats[n].clone();
				}
			}
			return hs;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String toString() {
		if (actionSeat >= 0) {
			return String.valueOf(action);
		}
		return note;
	}
	
}
