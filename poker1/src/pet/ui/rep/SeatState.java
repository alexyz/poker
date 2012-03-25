package pet.ui.rep;

/**
 * represents current state of seat
 */
class SeatState implements Cloneable {
	String name;
	int stack;
	int bet;
	String cards;
	boolean folded;
	@Override
	public SeatState clone() {
		try {
			return (SeatState) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}