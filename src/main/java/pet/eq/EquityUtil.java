
package pet.eq;

public class EquityUtil {
	
	/**
	 * get the array of rank names for the equity type. can't use current value
	 * to get type because it might not be set
	 */
	public static String[] getRankNames (Equity.Type eqtype) {
		switch (eqtype) {
			case DSLO_ONLY:
				return Poker.dsLowShortRankNames;
			case AFLO_ONLY:
			case HILO_AFLO8_HALF:
			case AFLO8_ONLY:
				return Poker.afLowShortRankNames;
			case HI_ONLY:
			case HILO_HI_HALF:
				return Poker.shortRankNames;
			case BADUGI_ONLY:
				return Badugi.shortRankNames;
			default:
				throw new RuntimeException("no such equity type: " + eqtype);
		}
	}
	
}
