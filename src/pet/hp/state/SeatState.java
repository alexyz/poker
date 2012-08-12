package pet.hp.state;

import pet.eq.*;
import pet.hp.Seat;

/**
 * represents current state of seat.
 * can be cloned
 */
public class SeatState implements Cloneable {
	
	/** seat of seat state, never null */
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
	public CardsState cardsState;
	/** has folded yet */
	public boolean folded;
	/** has won - only true at end, unlike seat.won > 0 */
	public boolean won;
	/** current hand equity, if any, can be misleading if no showdown */
	public MEquity meq;
	/** actual hand equity as percentage */
	public float deq;
	/** number of actions on this street starting at 1 */
	public int actionNum;
	/** expected value! */
	public float ev;
	/** total expected value! */
	public float tev;
	
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
