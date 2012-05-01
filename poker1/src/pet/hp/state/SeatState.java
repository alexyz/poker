package pet.hp.state;

import pet.eq.*;
import pet.hp.Seat;

/**
 * represents current state of seat
 */
public class SeatState implements Cloneable {
	/** seat of seat state */
	public final Seat seat;
	/** current stack */
	public int stack;
	/** stack to pot ratio */
	public float spr;
	/** amount pushed toward pot for this round */
	public int amount;
	/** bet to pot ratio as percent (100 is pot bet) */
	public float bpr;
	/** current hole cards (changes in games like draw) */
	public String[] hole;
	/** has folded yet */
	public boolean folded;
	/** has won - only true at end, unlike seat.won > 0 */
	public boolean won;
	/** current hand equity, if any */
	public MEquity meq;
	/** number of actions on this street starting at 1 */
	public int acts;
	
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
