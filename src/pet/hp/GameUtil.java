package pet.hp;

import java.text.NumberFormat;
import java.util.Comparator;

import pet.eq.*;

/**
 * Utility methods for game objects, most methods are just interested in game type
 */
public class GameUtil {
	
	/** poker equity functions */
	private static final Poker drawPoker = new DrawPoker(true);
	private static final Poker tripleDrawPoker = new DrawPoker(false);
	private static final Poker holdemPoker = new HEPoker(false, false);
	private static final Poker omahaPoker = new HEPoker(true, false);
	private static final Poker omahaHLPoker = new HEPoker(true, true);
	private static final String[] hestreetnames = { "Pre-flop", "Flop", "Turn", "River" };
	private static final String[] drawstreetnames = { "Pre-draw", "Post-draw" };
	private static final String[] tripdrawstreetnames = { "Pre-draw", "Post-draw 1", "Post-draw 2", "Post-draw 3" };
	
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
	public static String getLimitName(char limittype) {
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
	public static String getGameTypeName(char gametype) {
		switch (gametype) {
			case Game.OM_TYPE: 
				return "Omaha";
			case Game.OMHL_TYPE:
				return "Omaha H-L";
			case Game.HE_TYPE: 
				return "Hold'em";
			case Game.FCD_TYPE: 
				return "5 Card Draw";
			case Game.DSTD_TYPE:
				return "2-7 Triple Draw";
			default: 
				throw new RuntimeException("no such game type " + gametype);
		}
	}
	
	/** get full name of mixed game type */
	public static String getMixTypeName(char mixtype) {
		switch (mixtype) {
			case Game.HE_OM_MIX: 
				return "Mixed HE/OM";
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
	public static int getHoleCards(char gametype) {
		switch (gametype) {
			case Game.FCD_TYPE:
			case Game.DSTD_TYPE:
				return 5;
			case Game.HE_TYPE:
				return 2;
			case Game.OM_TYPE:
			case Game.OMHL_TYPE:
				return 4;
			default: 
				throw new RuntimeException("unknown game type " + gametype);
		}
	}
	
	/** return the minimum number of hole cards required for an equity calculation for this game */
	public static int getMinHoleCards(char gametype) {
		// should probably get this from poker instance
		switch (gametype) {
			case Game.FCD_TYPE:
			case Game.DSTD_TYPE:
				return 1;
			case Game.HE_TYPE:
				return 1;
			case Game.OM_TYPE:
			case Game.OMHL_TYPE:
				return 2;
			default: 
				throw new RuntimeException("unknown game type " + gametype);
		}
	}
	
	/** return a string representing unknown hole cards for this game */
	public static String unknownCardsString(char gametype) {
		// return string constant instead of making string
		switch (gametype) {
			case Game.FCD_TYPE:
			case Game.DSTD_TYPE:
				return "[ ][ ][ ][ ][ ]";
			case Game.HE_TYPE:
				return "[ ][ ]";
			case Game.OM_TYPE:
			case Game.OMHL_TYPE:
				return "[ ][ ][ ][ ]";
			default:
				throw new RuntimeException("unknown game type " + gametype);
		}
	}
	
	/** get street names for game type */
	private static String[] getStreetNames (char gametype) {
		switch (gametype) {
			case Game.FCD_TYPE:
				return drawstreetnames;
			case Game.HE_TYPE:
			case Game.OM_TYPE:
			case Game.OMHL_TYPE:
				return hestreetnames;
			case Game.DSTD_TYPE:
				return tripdrawstreetnames;
			default:
				throw new RuntimeException("no such game type " + gametype);
		}
	}
	
	/** return true if this street is the showdown street for the given game type */
	public static boolean isShowdown (char gametype, int street) {
		return street == getStreetNames(gametype).length - 1;
	}
	
	/** return the maximum number of streets in this game type */
	public static int getMaxStreets (char gametype) {
		return getStreetNames(gametype).length;
	}
	
	/** get the name of the street for this game type (starting at 0) */
	public static String getStreetName (char gametype, int street) {
		return getStreetNames(gametype)[street];
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
				return tripleDrawPoker;
			default:
				throw new RuntimeException("no poker for game " + game);
		}
	}
	
}
