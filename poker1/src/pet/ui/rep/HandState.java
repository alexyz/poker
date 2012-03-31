package pet.ui.rep;

/**
 * represents current state of hand
 */
class HandState implements Cloneable {
	public HandState(int max) {
		seats = new SeatState[max];
	}
	/** seats in hand, elements can be null */
	SeatState[] seats;
	/** final community cards */
	String[] board;
	/** current pot */
	int pot;
	/** seat index of button */
	int button;
	/** seat index of current action, -1 if no action */
	int actionSeat;
	/** current action */
	String action;
	/** information */
	String note;
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
				return s.seat.name + " " + action;
			}
		}
		return note;
	}
}