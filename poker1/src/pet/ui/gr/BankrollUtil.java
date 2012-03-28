package pet.ui.gr;

import java.text.DateFormat;
import java.util.*;
import pet.hp.*;

public class BankrollUtil {

	public static final GraphNameFunction nf = new GraphNameFunction() {
		@Override
		public String getXName(int x) {
			return dayname(x);
		}
		@Override
		public String getYName(int y) {
			float f = y / 100f;
			return "$" + f;
		}
	};

	public static List<GraphDataPoint> getBankRoll (Parser hp, String player, String game) {
		List<GraphDataPoint> data = new ArrayList<GraphDataPoint>();
		List<Hand> hands = hp.getHands();
		Collections.sort(hands, HandUtil.idCmp);
		int won = 0, date = 0;
		for (Hand hand : hands) {
			if (game == null || game.equals(hand.gamename)) {
				for (Seat seat : hand.seats) {
					if (seat.name.equals(player)) {
						int handdate = daynumber(hand.date);
						if (date != handdate) {
							data.add(new GraphDataPoint(handdate, won));
							date = handdate;
						}
						won += seat.won - seat.pip + seat.uncalled;
					}
				}
			}
		}
		System.out.println("bankroll data: " + data.size());
		return data;
	}
	

	private static String dayname(int x) {
		int y = (x / 366) + 2000;
		int d = (x % 366) + 1;
		Calendar cal = new GregorianCalendar(y, 0, 1);
		cal.add(Calendar.DAY_OF_YEAR, d);
		String s = DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).format(cal.getTime());
		return s;
	}
	
	private static int daynumber(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		int y = cal.get(Calendar.YEAR) - 2000;
		int d = cal.get(Calendar.DAY_OF_YEAR) - 1;
		int id = y * 366 + d;
		return id;
	}
	
}
