package pet.ui.hud;

import java.awt.Container;
import java.awt.Point;

import javax.swing.*;

import pet.hp.*;
import pet.hp.info.*;

/**
 * initial hud frame that shows all huds for a table
 * 
 */
class HUDsFrame extends JFrame {
	
	private final HUDPanel[] hudPanels;
	private final Info info = new Info();
	private final HUDManager man;
	private boolean hudsAlwaysOnTop = true;
	
	public HUDsFrame(HUDManager man, String title, int max) {
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setAlwaysOnTop(true);
		setTitle(title + " (" + max + "-max)");
		this.man = man;
		this.hudPanels = new HUDPanel[max];
		JPanel p = new JPanel(null);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		for (int n = 0; n < max; n++) {
			HUDPanel h = new HUDPanel(this, n + 1);
			this.hudPanels[n] = h;
			p.add(h);
		}
		setContentPane(p);
		pack();
		setVisible(true);
	}
	
	/**
	 * remove panel from frame so it can be added to a new window
	 */
	public void removeHud(HUDPanel hud) {
		// do this as new window will be in same place also on top
		setAlwaysOnTop(false);
		Container p = getContentPane();
		p.remove(hud);
		pack();
		if (p.getComponentCount() == 0) {
			// none left in frame
			setVisible(false);
			dispose();
		}
	}
	
	/**
	 * Add this hud back to the frame
	 */
	public void addHud(HUDPanel hud) {
		Container p = getContentPane();
		loop: {
			for (int n = 0; n < p.getComponentCount(); n++) {
				HUDPanel h = (HUDPanel) p.getComponent(n);
				if (hud.seat < h.seat) {
					System.out.println("add hud to hud group at " + n);
					p.add(hud, n);
					break loop;
				}
			}
			System.out.println("add hud to hud group");
			p.add(hud);
		}
		pack();
		if (!isShowing()) {
			System.out.println("show hud group");
			setVisible(true);
		}
		if (p.getComponentCount() == hudPanels.length) {
			setAlwaysOnTop(true);
		}
	}
	
	public void translateHudsFrame(int dx, int dy) {
		Point l = getLocation();
		setLocation(l.x + dx, l.y + dy);
		// TODO close popup
	}
	
	public void clearHuds() {
		System.out.println("clear huds");
		// TODO should just reset specific player
		info.clear();
		for (HUDPanel hud : hudPanels) {
			hud.clearHud();
		}
	}
	
	/** 
	 * close all huds for this table, dispose frame and remove from hud manager
	 */
	public void closeHuds() {
		System.out.println("close all huds");
		for (HUDPanel hud : hudPanels) {
			hud.closeHud();
		}
		setVisible(false);
		dispose();
		man.remove(getTitle());
	}
	
	public void updateHuds(Hand hand) {
		System.out.println("update huds with " + hand);
		info.handAdded(hand);
		// XXX clear those who have left (though they could be sitting out)
		for (Seat seat : hand.seats) {
			PlayerGameInfo pgi = info.getPlayerInfo(seat.name).games.get(hand.game.id);
			hudPanels[seat.num - 1].updateHud(pgi);
		}
		if (getContentPane().getComponentCount() > 0) {
			pack();
			if (!isVisible()) {
				setVisible(true);
			}
		}
	}
	
	public boolean isHudsAlwaysOnTop() {
		return hudsAlwaysOnTop;
	}

	/**
	 * Set whether the floating hud windows are always on top
	 */
	public void setHudsAlwaysOnTop(boolean top) {
		hudsAlwaysOnTop = top;
		for (HUDPanel hud : hudPanels) {
			hud.setHudAlwaysOnTop(top);
		}
	}
}
