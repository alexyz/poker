package pet.ui.rep;

import pet.eq.HandEq;
import pet.hp.Seat;

/**
 * represents current state of seat
 */
class SeatState implements Cloneable {
	final Seat seat;
	int stack;
	int bet;
	String[] hole;
	boolean folded;
	boolean won;
	HandEq eq;
	
	public SeatState(Seat seat) {
		this.seat = seat;
	}
	
	@Override
	public SeatState clone() {
		try {
			return (SeatState) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}