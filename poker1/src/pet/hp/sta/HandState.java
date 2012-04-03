package pet.hp.sta;

/**
 * represents current state of hand
 */
public class HandState implements Cloneable {
	public HandState(int max) {
		seats = new SeatState[max];
	}
	/** seats in hand (where seat 1 is index 0), elements can be null */
	public SeatState[] seats;
	/** community cards */
	public String[] board;
	/** current pot */
	public int pot;
	/** seat index of button */
	public int button;
	/** seat index of current action, -1 if no action */
	public int actionSeat;
	/** current action */
	public String action;
	/** information */
	public String note;
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