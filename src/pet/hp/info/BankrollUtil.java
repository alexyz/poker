package pet.hp.info;

import java.text.DateFormat;
import java.util.*;
import pet.hp.*;
import pet.ui.gr.*;

/**
 * Methods for getting bank roll graph data
 */
public class BankrollUtil {

	/**
	 * get the graph data for the players all time bankroll
	 */
	public static GraphData<Hand> getBankRoll(final String player, final List<Hand> hands, final String title) {
		if (hands.size() <= 1) {
			System.out.println("not enough hands for bankroll");
			return null;
		}

		Collections.sort(hands, HandUtil.idCmp);
		final Hand firstHand = hands.get(0);
		final char currency = firstHand.game.currency;
		
		final GraphData<Hand> data = new GraphData<Hand>(title, "hand", "money") {
			@Override
			public String getYName(int y) {
				return GameUtil.formatMoney(currency, y);
			}
			@Override
			public String getXDesc(int x) {
				if (x > 0 && x < points.size()) {
					GraphDataPoint<Hand> p = points.get(x);
					return DateFormat.getDateTimeInstance().format(p.ref.date);
				} else {
					return null;
				}
			}
		};
		
		int won = 0;
		
		for (int n = 0; n < hands.size(); n++) {
			final Hand hand = hands.get(n);
			for (Seat seat : hand.seats) {
				if (seat.name.equals(player)) {
					won += seat.won - seat.pip;
					data.points.add(new GraphDataPoint<Hand>(hand, n, won));
					break;
				}
			}
		}
		
		System.out.println("bank roll data points: " + data.points.size());
		return data;
	}
}
