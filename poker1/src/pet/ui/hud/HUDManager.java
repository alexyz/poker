package pet.ui.hud;

import java.util.*;

import javax.swing.*;

import pet.hp.*;

/**
 * Manages the hud frames for the tables
 */
public class HUDManager implements HistoryListener {
	
	private final TreeMap<String,HUDsFrame> hudsFrames = new TreeMap<String,HUDsFrame>();
	
	@Override
	public synchronized void handAdded(final Hand hand) {
		long t = System.currentTimeMillis() - (1000 * 60 * 10);
		if (hand.date.getTime() > t) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					showHand(hand);
				}
			});
		}
	}
	
	public synchronized void showHand(final Hand hand) {
		HUDsFrame f = hudsFrames.get(hand.tablename);
		if (f == null) {
			System.out.println("create hud group");
			hudsFrames.put(hand.tablename, f = new HUDsFrame(this, hand.tablename, hand.game.max));
			// TODO remove from map on close
		}
		f.updateHuds(hand);
	}
	
	public synchronized void remove(String table) {
		System.out.println("remove hud group");
		hudsFrames.remove(table);
	}

	@Override
	public void gameAdded(Game game) {
		//
	}
}

