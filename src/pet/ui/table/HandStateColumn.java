package pet.ui.table;

import java.awt.Color;
import java.awt.Font;

import pet.hp.Action;
import pet.hp.state.HandState;

abstract class HandStateColumn extends MyColumn<HandState> {

	private static final Color playerColour = new Color(192, 255, 192);
	private static final Color winColour = new Color(255, 255, 128);
	
	public HandStateColumn(Class<?> cl, String name, String desc) {
		super(cl, name, desc);
	}
	
	@Override
	public Color getColour(HandState hs) {
		if (hs.actionSeatIndex == -1) {
			return Color.lightGray;
			
		} else if (hs.action.type == Action.Type.COLLECT) {
			return winColour;
			
		} else if (hs.actionSeat().seat == hs.hand.myseat) {
			return playerColour;
		} 
		
		return null;
	}
	
	@Override
	public Font getFont(HandState hs) {
		if (hs.actionSeatIndex < 0) {
			return MyJTable.boldTableFont;
		} 
		return null;
	}
	
}