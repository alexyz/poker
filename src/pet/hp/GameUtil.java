package pet.hp;

import java.text.NumberFormat;
import java.util.Comparator;

import pet.eq.*;

/**
 * Utility methods for game objects, most methods are just interested in game
 * type and return various fixed properties of that game
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
	public static String getLimitName(Game.Limit limittype) {
		switch (limittype) {
			case PL: 
				return "PL"; //"Pot Limit";
			case NL: 
				return "NL"; //"No Limit";
			case FL: 
				return "FL"; //"Fixed Limit";
			default: 
				throw new RuntimeException("no such limit " + limittype);
		}
	}
	
	/** get full name of game */
	public static String getGameTypeName(Game.Type gametype) {
		switch (gametype) {
			case OM: 
				return "Omaha";
			case OMHL:
				return "Omaha H/L";
			case HE: 
				return "Hold'em";
			case FCD: 
				return "5 Card Draw";
			case DSTD:
				return "2-7 Triple Draw";
			case DSSD:
				return "2-7 Single Draw";
			case RAZZ:
				return "Razz";
			case STUD:
				return "7 Card Stud";
			case STUDHL:
				return "7 Card Stud H/L";
			case OM5:
				return "5 Card Omaha";
			case OM51:
				return "5 Card Omaha + 1";
			case OM5HL:
				return "5 Card Omaha H/L";
			case OM51HL:
				return "5 Card Omaha + 1 H/L";
			default: 
				throw new RuntimeException("no such game type " + gametype);
		}
	}
	
	/** get full name of mixed game type */
	public static String getMixTypeName(Game.Mix mixtype) {
		switch (mixtype) {
			case HE_OM_MIX: 
				return "Mixed HE/OM";
			case TRIPSTUD_MIX:
				return "Triple Stud";
			case EIGHT_MIX:
				return "8-Game";
			case HORSE_MIX:
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
		if (game.mix != null) {
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
	public static int getHoleCards(Game.Type gametype) {
		switch (gametype) {
			case HE:
				return 2;
			case OM:
			case OMHL:
				return 4;
			case FCD:
			case DSTD:
			case DSSD:
			case OM5:
			case OM51:
			case OM51HL:
			case OM5HL:
				return 5;
			case STUD:
			case RAZZ:
			case STUDHL:
				return 7;
			default: 
				throw new RuntimeException("unknown game type " + gametype);
		}
	}
	
	/** return the minimum number of hole cards required for an equity calculation for this game */
	public static int getMinHoleCards(Game.Type gametype) {
		// should probably get this from poker instance
		switch (gametype) {
			case HE:
			case FCD:
			case DSTD:
			case DSSD:
			case STUD:
			case STUDHL:
			case RAZZ:
				return 1;
			case OM:
			case OMHL:
			case OM5:
			case OM51:
			case OM5HL:
			case OM51HL:
				return 2;
			default: 
				throw new RuntimeException("unknown game type " + gametype);
		}
	}
	
	/** return a string representing unknown hole cards for this game */
	public static String unknownCardsString(Game.Type gametype) {
		int c = getHoleCards(gametype);
		StringBuilder sb = new StringBuilder(c*3);
		for (int n = 0; n < c; n++) {
			sb.append("[ ]");
		}
		return sb.toString();
	}
	
	/** get street names for game type */
	private static String[] getStreetNames (Game.Type gametype) {
		switch (gametype) {
			case FCD:
			case DSSD:
				return drawstreetnames;
			case HE:
			case OM:
			case OMHL:
			case OM5:
			case OM51:
			case OM5HL:
			case OM51HL:
				return hestreetnames;
			case DSTD:
				return tripdrawstreetnames;
			case STUD:
			case STUDHL:
			case RAZZ:
				return studstreetnames;
			default:
				throw new RuntimeException("no such game type " + gametype);
		}
	}
	
	/** return true if this street is the showdown street for the given game type */
	public static boolean isShowdown (Game.Type gametype, int streetIndex) {
		return streetIndex == getStreetNames(gametype).length - 1;
	}
	
	/** return the maximum number of streets in this game type */
	public static int getMaxStreets (Game.Type gametype) {
		return getStreetNames(gametype).length;
	}
	
	/** get the name of the street for this game type (starting at 0) */
	public static String getStreetName (Game.Type gametype, int streetIndex) {
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
	public static Poker getPoker(Game.Type gameType) {
		switch (gameType) {
			case FCD:
				return drawPoker;
			case HE:
				return holdemPoker;
			case OM:
			case OM5:
			case OM51:
				return omahaPoker;
			case OMHL:
			case OM5HL:
			case OM51HL:
				return omahaHLPoker;
			case DSTD:
			case DSSD:
				return lowDrawPoker;
			case STUD:
				return studPoker;
			case RAZZ:
				return razzPoker;
			case STUDHL:
				return studHLPoker;
			default:
				throw new RuntimeException("no poker for game " + gameType);
		}
	}
	
	/**
	 * get the hi rank names for the game type (not low ranks if hi/lo split)
	 */
	public static String[] getRanksHi(Game.Type gameType) {
		switch (gameType) {
			case DSTD:
			case DSSD:
				return Poker.dsLowRankNames;
			case RAZZ:
				return Poker.afLowRankNames;
			default:
				return Poker.ranknames;
		}
	}
	
	/**
	 * get the number of draws remaining for the game type and street index
	 */
	public static int getDraws(Game.Type gameType, int streetIndex) {
		switch (gameType) {
			case FCD:
			case DSSD:
				// 1, 0 draws
				return 1 - streetIndex;
			case DSTD:
				// 3, 2, 1, 0 draws
				return 3 - streetIndex; 
			default:
				return 0;
		}
	}
}
