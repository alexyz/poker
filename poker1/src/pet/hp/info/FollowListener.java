package pet.hp.info;

import pet.hp.Hand;

public interface FollowListener {
	public void nextHand(Hand h);
	public void doneFile(int done, int total);
}
