package pet.hp.info;

import java.text.DateFormat;
import java.util.*;

import pet.hp.*;
import pet.ui.graph.*;

/**
 * Methods for getting bank roll graph data
 */
public class BankrollUtil {

	/**
	 * get the graph data for the players all time bankroll
	 */
	public static GraphData getBankRoll(final String player, final List<Hand> hands, final String title) {
		if (hands.size() <= 1) {
			System.out.println("not enough hands for bankroll");
			return null;
		}

		Collections.sort(hands, HandUtil.idCmp);
		final Hand firstHand = hands.get(0);
		final char currency = firstHand.game.currency;
		final List<Hand> pointHands = new ArrayList<>();
		
		final GraphData data = new GraphData(title, "hand", "money") {
			@Override
			public String getYName(int y) {
				return GameUtil.formatMoney(currency, y);
			}
			@Override
			public String getXDesc(int x) {
				if (x > 0 && x < pointHands.size()) {
					return DateFormat.getDateTimeInstance().format(new Date(pointHands.get(x).date));
				} else {
					return null;
				}
			}
		};
		
		int won = 0;
		long prevdate = 0;
		final long t = 1000L * 60L * 60L;
		data.pointsMap.put("x", new ArrayList<GraphDataPoint>());
		
		for (int n = 0; n < hands.size(); n++) {
			final Hand hand = hands.get(n);
			long d = hand.date;
			boolean newsession = false;
			if (d > (prevdate + t)) {
				newsession = true;
				// new session
				// just do vertical line?
			}
			prevdate = d;
			for (Seat seat : hand.seats) {
				if (seat.name.equals(player)) {
					won += seat.won - seat.pip;
					data.pointsMap.get("x").add(new GraphDataPoint(n, won, newsession));
					pointHands.add(hand);
					break;
				}
			}
		}
		
		System.out.println("bank roll data points: " + data.pointsMap.get("x").size());
		return data;
	}
	

	/**
	 * get the graph data for the players all time bankroll
	 */
	public static GraphData getMultiBankRoll(final Set<String> players, final List<Hand> hands, final String title) {
		if (hands.size() <= 1) {
			System.out.println("not enough hands for bankroll");
			return null;
		}

		Collections.sort(hands, HandUtil.idCmp);
		final Hand firstHand = hands.get(0);
		final char currency = firstHand.game.currency;
		
		final Map<Long,Hand> ids = new TreeMap<>();
		for (Hand h : hands) {
			ids.put(h.id, h);
		}
		
		final long[] idarr = new long[ids.size()];
		{
			int n = 0;
			for (Long id : ids.keySet()) {
				idarr[n++] = id;
			}
		}
		
		final GraphData data = new GraphData(title, "hand", "money") {
			@Override
			public String getYName(int y) {
				return GameUtil.formatMoney(currency, y);
			}
		};
		
		int won = 0;
		long prevdate = 0;
		final long t = 1000L * 60L * 60L;
		
		for (Hand hand : hands) {
			long d = hand.date;
			boolean newsession = false;
			if (d > (prevdate + t)) {
				newsession = true;
				// new session
				// just do vertical line?
			}
			prevdate = d;
			for (Seat seat : hand.seats) {
				if (players.contains(seat.name)) {
					List<GraphDataPoint> points = data.pointsMap.get(seat.name);
					if (points == null) {
						data.pointsMap.put(seat.name, points = new ArrayList<>());
					}
					won += seat.won - seat.pip;
					int n = Arrays.binarySearch(idarr, hand.id);
					points.add(new GraphDataPoint(n, won, newsession));
					break;
				}
			}
		}
		
		System.out.println("bank roll data points: " + data.pointsMap.size());
		return data;
	}
}
