package pet.hp.state;

import pet.eq.HandEq;
import pet.hp.Seat;

/**
 * represents current state of seat
 */
public class SeatState implements Cloneable {
	/** seat of seat */
	public final Seat seat;
	/** current stack */
	public int stack;
	/** stack to pot ratio */
	public float spr;
	/** amount pushed toward pot for this round */
	public int amount;
	/** bet to pot ratio as percent (100 is pot bet) */
	public float bpr;
	/** current hole cards */
	public String[] hole;
	/** has folded yet */
	public boolean folded;
	/** has won */
	public boolean won;
	/** current hand equity, if any */
	public HandEq eq;
	
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