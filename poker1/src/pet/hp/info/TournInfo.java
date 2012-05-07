package pet.hp.info;

import pet.hp.Hand;
import pet.hp.Tourn;

/**
 * derived tournament information
 */
public class TournInfo {
	// TODO tourn type, e.g. super, hyper, matrix
	// all in eq product?
	// no of hands
	// aggr/vpip? playergameinfo of participants?
	
	public final Tourn tourn;
	public int hands;
	
	public TournInfo(Tourn tourn) {
		this.tourn = tourn;
	}
	
	public void addHand(Hand h) {
		hands++;
	}
}
