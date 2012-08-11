package pet.hp.state;

import java.util.Arrays;

/**
 * player hole cards as string array, plus meta data to indicate discarded cards
 * and if cards were guessed
 */
// might be better in state package as its only used there
public class CardsState {
	/** hole cards for display/equity purposes */
	public final String[] hole;
	/** discarded cards if any */
	public final String[] discarded;
	/** are hole cards guessed */
	public final boolean guess;
	
	public CardsState(String[] hole, String[] discarded, boolean guess) {
		this.hole = hole;
		this.discarded = discarded;
		this.guess = guess;
	}
	
	public CardsState(int numHole, int numDiscarded) {
		this(new String[numHole], numDiscarded > 0 ? new String[numDiscarded] : null, false);
	}
	
	public CardsState(String[] hole) {
		this(hole, null, false);
	}
	
	@Override
	public String toString() {
		return String.format("Cards[%s %s %s]", Arrays.toString(hole), Arrays.toString(discarded), guess);
	}
}
