package pet.ui.hp;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import pet.hp.Hand;
import pet.hp.state.HandState;
import pet.hp.state.HandStateUtil;

/** represents a list of hand states for a hand */
class HandStateItem {
	public final List<HandState> states;
	public final Hand hand;
	public HandStateItem(Hand hand) {
		this.hand = hand;
		this.states = HandStateUtil.getStates(hand);
	}
	@Override
	public String toString() {
		// user readable description of hand
		return hand.tablename + " " + DateFormat.getDateTimeInstance().format(new Date(hand.date)) + (hand.showdown ? " *" : "");
	}
}