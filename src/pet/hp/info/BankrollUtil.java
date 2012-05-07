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
	public static GraphData getBankRoll(List<Hand> hands, String player, String game) {
		if (hands.size() <= 1) {
			System.out.println("not enough hands for bankroll");
			return null;
		}

		Hand fh = hands.get(0);
		final char currency = fh.game.currency;
		GraphData data = new DateGraphData() {
			@Override
			public String getYName(int y) {
				return GameUtil.formatMoney(currency, y);
			}
		};
		data.name = player + " * " + game;

		Collections.sort(hands, HandUtil.idCmp);
		int won = 0, day = 0;
		for (Hand hand : hands) {
			for (Seat seat : hand.seats) {
				if (seat.name.equals(player)) {
					int handDay = DateGraphData.getDayNumber(hand.date);
					if (day != handDay) {
						data.points.add(new GraphDataPoint(handDay, won));
						day = handDay;
					}
					won += seat.won - seat.pip;
				}
			}
		}
		System.out.println("bank roll data points: " + data.points.size());
		return data;
	}
}
