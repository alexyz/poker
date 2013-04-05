
package pet.eq;

public class EquityUtil {
	
	/**
	 * get the array of rank names for the equity type. can't use current value
	 * to get type because it might not be set
	 */
	public static String[] getRankNames (Equity.Type eqtype) {
		switch (eqtype) {
			case DSLO_ONLY:
				return Poker.dsLowRankNames;
			case AFLO_ONLY:
			case HILO_AFLO8_HALF:
			case AFLO8_ONLY:
				return Poker.afLowRankNames;
			case HI_ONLY:
			case HILO_HI_HALF:
				return Poker.ranknames;
			default:
				throw new RuntimeException("no such equity type: " + eqtype);
		}
	}
	
	/** get name of equity type */
	public static String getEqTypeName (Equity.Type eqtype) {
		switch (eqtype) {
			case DSLO_ONLY:
				return "2-7 Low Only";
			case AFLO_ONLY:
				return "A-5 Low Only";
			case AFLO8_ONLY:
				return "A-5 Low (8) Only";
			case HI_ONLY:
				return "High Only";
			case HILO_HI_HALF:
				return "High Half";
			case HILO_AFLO8_HALF:
				return "A-5 Low (8) Half";
			default:
				throw new RuntimeException("no such equity type: " + eqtype);
		}
	}
	
}
