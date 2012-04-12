package pet.hp.info;

import pet.hp.Hand;

/**
 * interface for receiving hands from the follow thread
 */
public interface FollowListener {
	/** a hand has just been parsed */
	public void nextHand(Hand h);
	/** a whole file has just been parsed */
	public void doneFile(int done, int total);
}
