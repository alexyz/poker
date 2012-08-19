package pet.hp.state;

import java.util.Arrays;
import java.util.List;

import pet.eq.DrawPoker;

/**
 * player hole cards as string array, plus meta data to indicate discarded cards
 * and if cards were guessed
 */
public class CardsState {
	/** hole cards for display/equity purposes - may contain nulls for stud */
	public final String[] cards;
	/** discarded cards if any */
	public final String[] discarded;
	/** are hole cards guessed */
	public final boolean guess;
	public final List<DrawPoker.Draw> suggestedDraws;
	
	public CardsState(String[] hole, String[] discarded, boolean guess, List<DrawPoker.Draw> l) {
		this.cards = hole;
		this.discarded = discarded;
		this.guess = guess;
		this.suggestedDraws = l;
	}
	
	@Override
	public String toString() {
		return String.format("Cards[%s %s %s]", Arrays.toString(cards), Arrays.toString(discarded), guess);
	}
}
