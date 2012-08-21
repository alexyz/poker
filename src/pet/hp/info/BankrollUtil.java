package pet.hp.info;

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
	public static GraphData getBankRoll(final List<Hand> hands, final String player, final String game) {
		if (hands.size() <= 1) {
			System.out.println("not enough hands for bankroll");
			return null;
		}

		Collections.sort(hands, HandUtil.idCmp);
		final Hand firstHand = hands.get(0);
		final char currency = firstHand.game.currency;
		
		final GraphData data = new GraphData(player + " * " + game, "hand", "money") {
			@Override
			public String getYName(int y) {
				return GameUtil.formatMoney(currency, y);
			}
			@Override
			public String getXDesc(int x) {
				return "" + x;
			}
		};
		
		int won = 0;
		
		for (int n = 0; n < hands.size(); n++) {
			final Hand hand = hands.get(n);
			for (Seat seat : hand.seats) {
				if (seat.name.equals(player)) {
					won += seat.won - seat.pip;
					data.points.add(new GraphDataPoint(n, won));
				}
			}
		}
		
		System.out.println("bank roll data points: " + data.points.size());
		return data;
	}
}
