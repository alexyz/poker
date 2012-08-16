package pet.hp;

import java.text.NumberFormat;
import java.util.Comparator;

import pet.eq.*;

/**
 * Utility methods for game objects, most methods are just interested in game type
 */
public class GameUtil {
	
	/** poker equity functions */
	private static final DrawPoker drawPoker = new DrawPoker(true);
	private static final DrawPoker lowDrawPoker = new DrawPoker(false);
	private static final HEPoker holdemPoker = new HEPoker(false, false);
	private static final HEPoker omahaPoker = new HEPoker(true, false);
	private static final HEPoker omahaHLPoker = new HEPoker(true, true);
	private static final StudPoker studPoker = new StudPoker(Value.hiValue, false);
	private static final StudPoker studHLPoker = new StudPoker(Value.hiValue, true);
	private static final StudPoker razzPoker = new StudPoker(Value.afLowValue, false);
	private static final String[] hestreetnames = { "Pre-flop", "Flop", "Turn", "River" };
	private static final String[] drawstreetnames = { "Pre-draw", "Post-draw" };
	private static final String[] tripdrawstreetnames = { "Pre-draw", "Post-draw 1", "Post-draw 2", "Post-draw 3" };
	private static final String[] studstreetnames = { "3rd Street", "4th Street", "5th Street", "6th Street", "River" };
	
	public static Comparator<Game> idCmp = new Comparator<Game>() {
		@Override
		public int compare(Game g1, Game g2) {
			return g1.id.compareTo(g2.id);
		}
	};
	
	/** get full name of currency */
	public static String getCurrencyName(char currency) {
		switch (currency) {
			case '$': 
				return "USD";
			case '€': 
				return "EUR";
			case Game.PLAY_CURRENCY: 
				return "Play";
			case Game.TOURN_CURRENCY:
				return "Tourn";
			default: 
				throw new RuntimeException("no such currency " + currency);
		}
	}
	
	/** get full name of limit type */
	public static String getLimitName(int limittype) {
		switch (limittype) {
			case Game.POT_LIMIT: 
				return "PL"; //"Pot Limit";
			case Game.NO_LIMIT: 
				return "NL"; //"No Limit";
			case Game.FIXED_LIMIT: 
				return "FL"; //"Fixed Limit";
			default: 
				throw new RuntimeException("no such limit " + limittype);
		}
	}
	
	/** get full name of game */
	public static String getGameTypeName(int gametype) {
		switch (gametype) {
			case Game.OM_TYPE: 
				return "Omaha";
			case Game.OMHL_TYPE:
				return "Omaha H/L";
			case Game.HE_TYPE: 
				return "Hold'em";
			case Game.FCD_TYPE: 
				return "5 Card Draw";
			case Game.DSTD_TYPE:
				return "2-7 Triple Draw";
			case Game.DSSD_TYPE:
				return "2-7 Single Draw";
			case Game.RAZZ_TYPE:
				return "Razz";
			case Game.STUD_TYPE:
				return "7 Card Stud";
			case Game.STUDHL_TYPE:
				return "7 Card Stud H/L";
			default: 
				throw new RuntimeException("no such game type " + gametype);
		}
	}
	
	/** get full name of mixed game type */
	public static String getMixTypeName(int mixtype) {
		switch (mixtype) {
			case Game.HE_OM_MIX: 
				return "Mixed HE/OM";
			case Game.TRIPSTUD_MIX:
				return "Triple Stud";
			case Game.EIGHT_MIX:
				return "8-Game";
			case Game.HORSE_MIX:
				return "HORSE";
			default: 
				throw new RuntimeException("unknown mix type " + mixtype);
		}
	}
	
	/** get full name of game variant */
	public static String getSubTypeName(int subtype) {
		String s = null;
		if ((subtype & Game.ZOOM_SUBTYPE) != 0) {
			s = "Zoom";
		}
		return s;
	}
	
	/**
	 * return string description of game
	 */
	public static String getGameId(Game game) {
		// Play 5 Card Draw NL 6max Zoom 0.01/0.02
		// (curr) [mix] (type) (lim) (max) [zoom] (blinds)
		StringBuilder sb = new StringBuilder();
		sb.append(getCurrencyName(game.currency)).append(" ");
		if (game.mix != 0) {
			sb.append(getMixTypeName(game.mix)).append(": ");
		}
		sb.append(getGameTypeName(game.type)).append(" ");
		sb.append(getLimitName(game.limit)).append(" ");
		sb.append(game.max).append("-max ");
		if (game.subtype != 0) {
			sb.append(getSubTypeName(game.subtype)).append(" ");
		}
		sb.append(formatMoney(game.currency, game.sb)).append("/");
		sb.append(formatMoney(game.currency, game.bb));
		return sb.toString();
	}
	
	/** return the number of hole cards for this game */
	public static int getHoleCards(int gametype) {
		switch (gametype) {
			case Game.HE_TYPE:
				return 2;
			case Game.OM_TYPE:
			case Game.OMHL_TYPE:
				return 4;
			case Game.FCD_TYPE:
			case Game.DSTD_TYPE:
			case Game.DSSD_TYPE:
				return 5;
			case Game.STUD_TYPE:
			case Game.RAZZ_TYPE:
			case Game.STUDHL_TYPE:
				return 7;
			default: 
				throw new RuntimeException("unknown game type " + gametype);
		}
	}
	
	/** return the minimum number of hole cards required for an equity calculation for this game */
	public static int getMinHoleCards(int gametype) {
		// should probably get this from poker instance
		switch (gametype) {
			case Game.HE_TYPE:
			case Game.FCD_TYPE:
			case Game.DSTD_TYPE:
			case Game.DSSD_TYPE:
			case Game.STUD_TYPE:
			case Game.STUDHL_TYPE:
			case Game.RAZZ_TYPE:
				return 1;
			case Game.OM_TYPE:
			case Game.OMHL_TYPE:
				return 2;
			default: 
				throw new RuntimeException("unknown game type " + gametype);
		}
	}
	
	/** return a string representing unknown hole cards for this game */
	public static String unknownCardsString(int gametype) {
		// return string constant instead of making string
		switch (gametype) {
			case Game.HE_TYPE:
				return "[ ][ ]";
			case Game.OM_TYPE:
			case Game.OMHL_TYPE:
				return "[ ][ ][ ][ ]";
			case Game.FCD_TYPE:
			case Game.DSTD_TYPE:
			case Game.DSSD_TYPE:
				return "[ ][ ][ ][ ][ ]";
			case Game.STUD_TYPE:
			case Game.RAZZ_TYPE:
			case Game.STUDHL_TYPE:
				return "[ ][ ][ ][ ][ ][ ][ ]";
			default:
				throw new RuntimeException("unknown game type " + gametype);
		}
	}
	
	/** get street names for game type */
	private static String[] getStreetNames (int gametype) {
		switch (gametype) {
			case Game.FCD_TYPE:
			case Game.DSSD_TYPE:
				return drawstreetnames;
			case Game.HE_TYPE:
			case Game.OM_TYPE:
			case Game.OMHL_TYPE:
				return hestreetnames;
			case Game.DSTD_TYPE:
				return tripdrawstreetnames;
			case Game.STUD_TYPE:
			case Game.STUDHL_TYPE:
			case Game.RAZZ_TYPE:
				return studstreetnames;
			default:
				throw new RuntimeException("no such game type " + gametype);
		}
	}
	
	/** return true if this street is the showdown street for the given game type */
	public static boolean isShowdown (int gametype, int streetIndex) {
		return streetIndex == getStreetNames(gametype).length - 1;
	}
	
	/** return the maximum number of streets in this game type */
	public static int getMaxStreets (int gametype) {
		return getStreetNames(gametype).length;
	}
	
	/** get the name of the street for this game type (starting at 0) */
	public static String getStreetName (int gametype, int streetIndex) {
		return getStreetNames(gametype)[streetIndex];
	}
	
	public static String formatMoney(char currency, int amount) {
		NumberFormat nf = NumberFormat.getNumberInstance();
		switch (currency) {
			case '$':
			case '€':
				// TODO $2 instead of $2.00
				return String.format("%c%.2f", currency, amount / 100f);
			case Game.TOURN_CURRENCY:
			case Game.PLAY_CURRENCY:
				return nf.format(amount);
			default: throw new RuntimeException("unknown currency " + currency);
		}
	}
	
	/**
	 * Get poker equity function for game type.
	 */
	public static Poker getPoker(Game game) {
		switch (game.type) {
			case Game.FCD_TYPE:
				return drawPoker;
			case Game.HE_TYPE:
				return holdemPoker;
			case Game.OM_TYPE:
				return omahaPoker;
			case Game.OMHL_TYPE:
				return omahaHLPoker;
			case Game.DSTD_TYPE:
			case Game.DSSD_TYPE:
				return lowDrawPoker;
			case Game.STUD_TYPE:
				return studPoker;
			case Game.RAZZ_TYPE:
				return razzPoker;
			case Game.STUDHL_TYPE:
				return studHLPoker;
			default:
				throw new RuntimeException("no poker for game " + game);
		}
	}
	
	public static String[] getRanksHi(int gameType) {
		switch (gameType) {
			case Game.FCD_TYPE:
			case Game.HE_TYPE:
			case Game.OM_TYPE:
			case Game.OMHL_TYPE:
			case Game.STUD_TYPE:
			case Game.STUDHL_TYPE:
				return Poker.ranknames;
			case Game.DSTD_TYPE:
			case Game.DSSD_TYPE:
				return Poker.dsLowRankNames;
			case Game.RAZZ_TYPE:
				return Poker.afLowRankNames;
			default:
				throw new RuntimeException("no hi ranks for game " + gameType);
		}
	}
	
}
