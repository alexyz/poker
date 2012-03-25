package pet.hp.util;

import pet.hp.Hand;
import pet.hp.Seat;

public class HandInfo {
	public final Hand hand;
	private Seat winner;
	private int wonOn;
	private int value;
	// "Winner", "WonOn", "Value"
	// position, stacks, spr, n-flop/draw,
	public HandInfo(Hand hand) {
		this.hand = hand;
		
	}
}
