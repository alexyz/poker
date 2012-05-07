package pet.hp.state;

import pet.hp.Action;
import pet.hp.Hand;

/**
 * represents current state of hand
 */
public class HandState implements Cloneable {
	
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
	/** seat index of current action, one less than seat number, -1 if no action */
	public int actionSeat;
	/** current action */
	public Action action;
	/** information */
	public String note;
	/** current street */
	public int streetIndex;
	
	public HandState(Hand hand) {
		this.hand = hand;
		this.seats = new SeatState[hand.game.max];
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
			SeatState s = seats[actionSeat];
			if (s != null) {
				return String.valueOf(action);
			}
		}
		return note;
	}
	
}
