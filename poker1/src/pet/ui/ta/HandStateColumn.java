package pet.ui.ta;

import java.awt.Color;
import java.awt.Font;

import pet.hp.Action;
import pet.hp.state.HandState;

abstract class HandStateColumn extends MyColumn<HandState> {

	private static final Color playerColour = new Color(192, 255, 192);
	private static final Color winColour = new Color(255, 255, 128);
	private static final Font boldfont = MyJTable.deffont.deriveFont(Font.BOLD);
	
	public HandStateColumn(Class<?> cl, String name, String desc) {
		super(cl, name, desc);
	}
	
	@Override
	public Color getColour(HandState hs) {
		if (hs.actionSeat == -1) {
			return Color.lightGray;
			
		} else if (hs.action.type == Action.COLLECT_TYPE) {
			return winColour;
			
		} else if (hs.seats[hs.actionSeat].seat == hs.hand.myseat) {
			return playerColour;
		} 
		
		return null;
	}
	
	@Override
	public Font getFont(HandState hs) {
		if (hs.actionSeat == -1) {
			return boldfont;
		} 
		return null;
	}
	
}