package pet.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.*;

import javax.swing.*;

import pet.hp.*;
import pet.hp.info.Info;
import pet.hp.info.PlayerGameInfo;

public class HUDManager implements HistoryListener {
	
	private final TreeMap<String,HUDGroup> map = new TreeMap<String,HUDGroup>();
	
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
		HUDGroup g = map.get(hand.tablename);
		if (g == null) {
			map.put(hand.tablename, g = new HUDGroup(hand.tablename, hand.game.max));
			// TODO remove from map on close
		}
		g.update(hand);
	}

	@Override
	public void gameAdded(Game game) {
		//
	}
}

class HUDGroup {
	private final JFrame f;
	private final Info info = new Info();
	final HUD[] huds;
	public HUDGroup(String title, int max) {
		JPanel p = new JPanel(new GridLayout(max, 1));
		huds = new HUD[max];
		for (int n = 0; n < max; n++) {
			HUD h = new HUD(this, n + 1);
			huds[n] = h;
			p.add(h);
		}
		f = new JFrame();
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.setTitle(title);
		f.setContentPane(p);
		f.pack();
		f.show();
	}
	public void update(Hand hand) {
		info.handAdded(hand);
		for (Seat seat : hand.seats) {
			PlayerGameInfo pgi = info.getPlayerInfo(seat.name).games.get(hand.game.id);
			huds[seat.num - 1].update(pgi);
		}
		f.pack();
		if (!f.isShowing()) {
			f.show();
		}
	}
}

// TODO menu: raise main window, load/store pos, reset player, close table, always on top, stats...
// last hand value
// dialog or jframe? both can be always on top/undecorated - dialogs block
// try jwindow
class HUD extends JPanel {
	private final JLabel nameLabel = new JLabel();
	private final ArrayList<JLabel> statLabels = new ArrayList<JLabel>();
	private final int seat;
	private final HUDGroup group;
	
	public HUD(HUDGroup g, int seat) {
		super(new BorderLayout());
		this.group = g;
		this.seat = seat;
		setBorder(BorderFactory.createLineBorder(Color.blue));
		nameLabel.setFont(PokerFrame.boldfont);
		nameLabel.setText("name");
		add(nameLabel, BorderLayout.NORTH);
		JPanel p = new JPanel(new GridLayout(3,2));
		for (int n = 0; n < 6; n++) {
			JLabel l = new JLabel(String.valueOf(n));
			statLabels.add(l);
			p.add(l);
		}
		add(p, BorderLayout.CENTER);
	}
	
	public void update(PlayerGameInfo pgi) {
		nameLabel.setText(seat + ": " + pgi.player.name);
		// compare to table, player history and pop
		statLabels.get(0).setText("Hands: " + pgi.hands);
		statLabels.get(1).setText("FlSeen: " + pgi.fs());
		statLabels.get(2).setText("ShSeen: " + pgi.ss());
		statLabels.get(3).setText("ShWon: " + pgi.sw());
		statLabels.get(4).setText("Vpip: " + pgi.vp());
		statLabels.get(5).setText("AF: " + pgi.afam());
	}
}



