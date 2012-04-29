package pet.hp.info;

import pet.hp.Hand;
import pet.hp.Tourn;

// TODO
public class TournInfo {
	public final Tourn tourn;
	public int hands;
	public TournInfo(Tourn tourn) {
		this.tourn = tourn;
	}
	public void addHand(Hand h) {
		hands++;
	}
	// tourn type
	// all in eq product?
	// no of hands
	// aggr/vpip? playergameinfo of participants?
}
