package pet.ui.hud;

import java.util.*;

import javax.swing.*;

import pet.hp.*;

/**
 * Manages the hud frames for all tables
 */
public class HUDManager implements HistoryListener {
	
	private final TreeMap<String,HUDsFrame> hudsFrames = new TreeMap<>();
	private boolean create = false;
	
	@Override
	public synchronized void handAdded(final Hand hand) {
		long t = System.currentTimeMillis() - (1000 * 60 * 10);
		if (hand.date > t) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					showHand(hand);
				}
			});
		}
	}
	
	/**
	 * show hud for hand
	 */
	public synchronized void showHand(final Hand hand) {
		HUDsFrame f = hudsFrames.get(hand.tablename);
		if (f == null && create) {
			System.out.println("create hud group");
			hudsFrames.put(hand.tablename, f = new HUDsFrame(this, hand.tablename, hand.game.max));
			// TODO don't display if table ignored
			f.setVisible(true);
		}
		if (f != null) {
			f.updateHuds(hand);
		}
	}
	
	public synchronized void remove(String table) {
		System.out.println("remove hud group");
		hudsFrames.remove(table);
	}

	@Override
	public void gameAdded(Game game) {
		//
	}
	
	public boolean isCreate() {
		return create;
	}

	public void setCreate(boolean create) {
		this.create = create;
	}
}

