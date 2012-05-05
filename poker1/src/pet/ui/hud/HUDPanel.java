package pet.ui.hud;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;

import pet.hp.GameUtil;
import pet.hp.info.PlayerGameInfo;
import pet.ui.PokerFrame;

// TODO last hole cards, hand value, eq on each street...
class HUDPanel extends JPanel {

	private static final Font boldfont = new Font("SansSerif", Font.BOLD, 11);
	private static final Font plainfont = new Font("SansSerif", Font.PLAIN, 11);

	public final int seat;
	private final HUDsFrame hudsFrame;
	private final JLabel nameLabel = new JLabel();
	private final ArrayList<JLabel> statLabels = new ArrayList<JLabel>();
	/** the window the hud is displayed in, af any */
	private JWindow window;

	public HUDPanel(HUDsFrame hudsFrame, int seat) {
		super(new BorderLayout());
		this.hudsFrame = hudsFrame;
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
		MouseAdapter a = new HUDMouseAdapter();
		addMouseListener(a);
		addMouseMotionListener(a);
	}

	/**
	 * close hud permanently
	 */
	public void closeHud() {
		System.out.println("close hud " + nameLabel.getText());
		if (window != null) {
			window.setVisible(false);
			window.dispose();
			window = null;
		}
	}

	/**
	 * swap between being a window hud or being in huds frame
	 */
	private void toggleWindowHud() {
		if (window != null) {
			System.out.println("close window " + nameLabel.getText());
			window.getContentPane().remove(this);
			window.setVisible(false);
			window.dispose();
			window = null;
			setBorder(null);
			hudsFrame.addHud(this);

		} else {
			System.out.println("make window " + nameLabel.getText());
			Point p = getLocationOnScreen();
			hudsFrame.removeHud(this);
			setBorder(BorderFactory.createLineBorder(Color.black));
			window = new JWindow();
			window.setLocation(p);
			window.setAlwaysOnTop(hudsFrame.isHudsAlwaysOnTop());
			window.setContentPane(this);
			window.pack();
			window.setVisible(true);
		}
	}

	/**
	 * clear the information displayed in the hud
	 */
	public void clearHud() {
		nameLabel.setText("" + seat);
		for (JLabel l : statLabels) {
			l.setText("");
		}
	}

	/**
	 * update the information displayed in the hud
	 */
	public void updateHud(PlayerGameInfo pgi) {
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

	public void setHudAlwaysOnTop(boolean top) {
		if (window != null) {
			window.setAlwaysOnTop(top);
			if (top) {
				window.toFront();
			} else {
				window.toBack();
			}
		}
	}

	/** show menu */
	private void popup(final MouseEvent e) {
		if (e.isPopupTrigger()) {
			// detach/attach, raise main window, load/store pos, reset player, close table, always on top, stats selector...
			System.out.println("show hud menu");
			JPopupMenu menu = new JPopupMenu("Table");

			menu.add(new JMenuItem(new AbstractAction(window != null ? "Attach" : "Detach") {
				@Override
				public void actionPerformed(ActionEvent e) {
					toggleWindowHud();
				}
			}));
			if (window != null) {
				JCheckBoxMenuItem cb = new JCheckBoxMenuItem(new AbstractAction("Always On Top All") {
					@Override
					public void actionPerformed(ActionEvent e) {
						hudsFrame.setHudsAlwaysOnTop(!hudsFrame.isHudsAlwaysOnTop());
					}
				});
				cb.setSelected(hudsFrame.isHudsAlwaysOnTop());
				menu.add(cb);
				
			} else {
				JCheckBoxMenuItem cb = new JCheckBoxMenuItem(new AbstractAction("Always On Top") {
					@Override
					public void actionPerformed(ActionEvent e) {
						hudsFrame.setAlwaysOnTop(!hudsFrame.isAlwaysOnTop());
					}
				});
				cb.setSelected(hudsFrame.isAlwaysOnTop());
				menu.add(cb);
			}
			menu.add(new JSeparator());
			menu.add(new JMenuItem(new AbstractAction("Main Window") {
				@Override
				public void actionPerformed(ActionEvent e) {
					hudsFrame.setHudsAlwaysOnTop(false);
					PokerFrame.getInstance().toFront();
				}
			}));
			menu.add(new JMenuItem(new AbstractAction("Clear All") {
				@Override
				public void actionPerformed(ActionEvent e) {
					hudsFrame.clearHuds();
				}
			}));
			menu.add(new JMenuItem(new AbstractAction("Close All") {
				@Override
				public void actionPerformed(ActionEvent e) {
					hudsFrame.closeHuds();
				}
			}));

			menu.show((Component) e.getSource(), e.getX(), e.getY());
		}
	}

	class HUDMouseAdapter extends MouseAdapter {
		private Point p;
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				System.out.println("double click");
				toggleWindowHud();
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
				if (window != null) {
					Point l = window.getLocation();
					window.setLocation(l.x + dx, l.y + dy);
				} else {
					hudsFrame.translateHudsFrame(dx, dy);
				}
			}
		}
	}

}
