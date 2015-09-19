package pet.hp.state;

import pet.hp.Action;
import pet.hp.GameUtil;
import pet.hp.Hand;

/**
 * represents current state of hand.
 * cloned - do not duplicate references
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
	/** seat index of button (starting at 0) */
	public int buttonIndex = -1;
	/** index of seat state of current action (starting at 0) */
	public int actionSeatIndex = -1;
	/** current action */
	public Action action;
	/** information */
	public String note;
	/** current street, starting at 0 */
	public int streetIndex = -1;
	
	public HandState(Hand hand) {
		this.hand = hand;
		this.seats = new SeatState[hand.game.max];
	}
	
	public SeatState actionSeat() {
		return actionSeatIndex >= 0 ? seats[actionSeatIndex] : null;
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
	
	/**
	 * Return string describing action (but not player)
	 */
	public String actionString() {
		StringBuilder sb = new StringBuilder();
		sb.append(action.type.desc);
		if (action.type == Action.Type.DRAW) {
			sb.append(" ").append(action.seat.drawn(streetIndex - 1));
		} else if (action.amount != 0) {
			sb.append(" ").append(GameUtil.formatMoney(hand.game.currency, action.amount));
			if (action.allin) {
				sb.append(" all in");
			}
		}
		return sb.toString();
	}
	
	@Override
	public String toString() {
		if (actionSeatIndex >= 0) {
			return String.valueOf(action);
		}
		return note;
	}
	
}
