package pet.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import pet.hp.*;
import pet.hp.info.*;

public class HUDManager implements HistoryListener {
	
	private final TreeMap<String,HUDGroupFrame> map = new TreeMap<String,HUDGroupFrame>();
	
	@Override
	public void handAdded(final Hand hand) {
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
	
	public void showHand(final Hand hand) {
		HUDGroupFrame g = map.get(hand.tablename);
		if (g == null) {
			System.out.println("create hud group");
			map.put(hand.tablename, g = new HUDGroupFrame(this, hand.tablename, hand.game.max));
			// TODO remove from map on close
		}
		g.update(hand);
	}
	
	public void remove(String table) {
		System.out.println("remove hud group");
		map.remove(table);
	}

	@Override
	public void gameAdded(Game game) {
		//
	}
}

class HUDGroupFrame extends JFrame {
	private final HUDPanel[] huds;
	private final Info info = new Info();
	private final HUDManager man;
	
	public HUDGroupFrame(HUDManager man, String title, int max) {
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setTitle(title + " (" + max + "-max)");
		this.man = man;
		
		this.huds = new HUDPanel[max];
		JPanel p = new JPanel(null);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		for (int n = 0; n < max; n++) {
			HUDPanel h = new HUDPanel(this, n + 1);
			huds[n] = h;
			p.add(h);
		}
		
		setContentPane(p);
		pack();
		show();
	}
	
	public void remove(HUDPanel hud) {
		Container p = getContentPane();
		p.remove(hud);
		pack();
		if (p.getComponentCount() == 0) {
			hide();
		}
	}
	
	public void add(HUDPanel hud) {
		loop: {
			Container p = getContentPane();
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
			show();
		}
	}
	
	public void trans(int dx, int dy) {
		Point l = getLocation();
		setLocation(l.x + dx, l.y + dy);
	}
	
	public void clear() {
		System.out.println("clear huds");
		// TODO should just reset specific player
		info.clear();
		for (HUDPanel hud : huds) {
			hud.clear();
		}
	}
	
	public void close() {
		System.out.println("close all huds");
		for (HUDPanel hud : huds) {
			hud.close();
		}
		hide();
		dispose();
		man.remove(getTitle());
	}
	
	public void update(Hand hand) {
		System.out.println("update huds with " + hand);
		info.handAdded(hand);
		// XXX clear those who have left (though they could be sitting out)
		for (Seat seat : hand.seats) {
			PlayerGameInfo pgi = info.getPlayerInfo(seat.name).games.get(hand.game.id);
			huds[seat.num - 1].update(pgi);
		}
		if (getComponentCount() > 0) {
			pack();
			if (!isShowing()) {
				show();
			}
		}
	}
}

// TODO last hole cards, hand value, eq on each street...
class HUDPanel extends JPanel {
	private static final Font boldfont = new Font("SansSerif", Font.BOLD, 11);
	private static final Font plainfont = new Font("SansSerif", Font.PLAIN, 11);
	
	public final HUDGroupFrame group;
	public final int seat;
	
	private final JLabel nameLabel = new JLabel();
	private final ArrayList<JLabel> statLabels = new ArrayList<JLabel>();
	private JWindow win;
	
	public HUDPanel(HUDGroupFrame g, int seat) {
		super(new BorderLayout());
		this.group = g;
		this.seat = seat;
		nameLabel.setText("" + seat);
		nameLabel.setFont(boldfont);
		add(nameLabel, BorderLayout.NORTH);
		JPanel p = new JPanel(new GridLayout(3,3));
		for (int n = 0; n < 9; n++) {
			JLabel l = new JLabel();
			l.setFont(plainfont);
			statLabels.add(l);
			p.add(l);
		}
		add(p, BorderLayout.CENTER);
		addMouseListener(HUDMouseAdapter.instance);
		addMouseMotionListener(HUDMouseAdapter.instance);
	}
	
	public boolean isWin() {
		return win != null;
	}
	
	public void close() {
		System.out.println("close hud " + nameLabel.getText());
		if (win != null) {
			win.hide();
			win.dispose();
			win = null;
		}
	}
	
	public void win() {
		if (win != null) {
			System.out.println("close window " + nameLabel.getText());
			win.getContentPane().remove(this);
			win.hide();
			win.dispose();
			win = null;
			setBorder(null);
			group.add(this);
			
		} else {
			System.out.println("make window " + nameLabel.getText());
			Point p = getLocationOnScreen();
			group.remove(this);
			setBorder(BorderFactory.createLineBorder(Color.black));
			win = new JWindow();
			win.setLocation(p);
			win.setAlwaysOnTop(true);
			//win.setUndecorated(true);
			win.setContentPane(this);
			win.pack();
			win.show();
		}
	}
	
	public void trans(int dx, int dy) {
		if (win != null) {
			Point l = win.getLocation();
			win.setLocation(l.x + dx, l.y + dy);
		} else {
			group.trans(dx, dy);
		}
	}
	
	public void clear() {
		nameLabel.setText("" + seat);
		for (JLabel l : statLabels) {
			l.setText("");
		}
	}
	
	public void update(PlayerGameInfo pgi) {
		System.out.println("update " + nameLabel.getText() + " with " + pgi);
		nameLabel.setText(seat + ": " + pgi.player.name + " (" + pgi.hands + "h)");
		// compare to table, player history and pop
		int i = 0;
		statLabels.get(i++).setText(String.format("VP: %.1f", pgi.vpip()));
		statLabels.get(i++).setText(String.format("PFR: %.1f", pgi.pfr()));
		float af = pgi.af();
		statLabels.get(i++).setText(Float.isNaN(af) ? "AF: " : String.format("AF: %.2f", af));
		statLabels.get(i++).setText(String.format("FS: %.1f", pgi.fs()));
		statLabels.get(i++).setText(String.format("SS: %.1f", pgi.ss()));
		float sw = pgi.sw();
		statLabels.get(i++).setText(Float.isNaN(sw) ? "SW: " : String.format("SW: %.1f", sw));
		statLabels.get(i++).setText("Am: " + GameUtil.formatMoney(pgi.game.currency, pgi.am()));
		statLabels.get(i++).setText("Am/H: " + GameUtil.formatMoney(pgi.game.currency, (int) pgi.amph()));
		statLabels.get(i++).setText("Cxc: " + pgi.cx());
	}

}

class HUDMouseAdapter extends MouseAdapter {
	public static final MouseAdapter instance = new HUDMouseAdapter();
	private Point p;
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			System.out.println("double click");
			HUDPanel hud = (HUDPanel) e.getSource();
			hud.win();
		}
	}
	@Override
	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			p = null;
			popup(e);
		} else {
			p = e.getPoint();
		}
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		p = null;
		if (e.isPopupTrigger()) {
			popup(e);
		}
	}
	@Override
	public void mouseDragged(MouseEvent e) {
		if (p != null) {
			//System.out.println("move hud");
			Point d = e.getPoint();
			int dx = d.x - p.x;
			int dy = d.y - p.y;
			HUDPanel hud = (HUDPanel) e.getSource();
			hud.trans(dx, dy);
		}
	}
	/** show menu */
	private void popup(final MouseEvent e) {
		if (e.isPopupTrigger()) {
			// detach/attach, raise main window, load/store pos, reset player, close table, always on top, stats selector...
			System.out.println("show hud menu");
			final HUDPanel hud = (HUDPanel) e.getSource();
			JPopupMenu menu = new JPopupMenu("Table");
			menu.add(new JMenuItem(new AbstractAction(hud.isWin() ? "Attach" : "Detach") {
				@Override
				public void actionPerformed(ActionEvent e) {
					hud.win();
				}
			}));
			menu.add(new JMenuItem(new AbstractAction("Clear All") {
				@Override
				public void actionPerformed(ActionEvent e) {
					hud.group.clear();
				}
			}));
			menu.add(new JMenuItem(new AbstractAction("Close All") {
				@Override
				public void actionPerformed(ActionEvent e) {
					hud.group.close();
				}
			}));
			menu.show((Component) e.getSource(), e.getX(), e.getY());
		}
	}
}

