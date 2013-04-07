package pet.hp;

import java.text.NumberFormat;
import java.util.Comparator;

import pet.eq.*;
import pet.eq.impl.*;

/**
 * Utility methods for game objects, most methods are just interested in game
 * type and return various fixed properties of that game
 */
public class GameUtil {
	
	/** poker equity functions */
	private static final DrawPoker drawPoker = new DrawPoker(Value.hiValue);
	private static final DrawPoker dsLowDrawPoker = new DrawPoker(Value.dsLowValue);
	private static final DrawPoker afLowDrawPoker = new DrawPoker(Value.afLowValue);
	private static final DrawPoker badugiPoker = new DrawPoker(Value.badugiValue);
	private static final HEPoker holdemPoker = new HEPoker(false, false);
	private static final HEPoker omahaPoker = new HEPoker(true, false);
	private static final HEPoker omahaHLPoker = new HEPoker(true, true);
	private static final StudPoker studPoker = new StudPoker(Value.hiValue, false);
	private static final StudPoker studHLPoker = new StudPoker(Value.hiValue, true);
	private static final StudPoker razzPoker = new StudPoker(Value.afLowValue, false);
	private static final FiveStudPoker fiveCardStudPoker = new FiveStudPoker();
	
	private static final String[] hestreetnames = { "Pre-flop", "Flop", "Turn", "River" };
	private static final String[] drawstreetnames = { "Pre-draw", "Post-draw" };
	private static final String[] tripdrawstreetnames = { "Pre-draw", "Post-draw 1", "Post-draw 2", "Post-draw 3" };
	private static final String[] studstreetnames = { "3rd Street", "4th Street", "5th Street", "6th Street", "River" };
	private static final String[] fcstudstreetnames = { "2nd Street", "3rd Street", "4th Street", "River" };
	
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
			sb.append(game.mix.desc).append(": ");
		}
		sb.append(game.type.desc).append(" ");
		sb.append(game.limit).append(" ");
		sb.append(game.max).append("-max ");
		if (game.subtype != 0) {
			sb.append(getSubTypeName(game.subtype)).append(" ");
		}
		// XXX 0 for tourn...
		sb.append(formatMoney(game.currency, game.sb)).append("/");
		sb.append(formatMoney(game.currency, game.bb));
		if (game.ante != 0) {
			sb.append("/");
			sb.append(formatMoney(game.currency, game.ante));
		}
		return sb.toString();
	}
	
	/** return the number of hole cards for this game (including up cards) */
	public static int getHoleCards(Game.Type gametype) {
		switch (gametype) {
			case HE:
				return 2;
			case OM:
			case OMHL:
			case BG:
				return 4;
			case FCD:
			case DSTD:
			case DSSD:
			case OM5:
			case OM51:
			case OM51HL:
			case OM5HL:
			case FSTUD:
			case AFTD:
				return 5;
			case STUD:
			case RAZZ:
			case STUDHL:
				return 7;
			default: 
				throw new RuntimeException("unknown game type " + gametype);
		}
	}
	
	/**
	 * get the max number of up cards for the game
	 */
	public static int getUpCards(Game.Type gametype) {
		switch (gametype) {
			case HE:
			case OM:
			case OMHL:
			case BG:
			case FCD:
			case DSTD:
			case DSSD:
			case OM5:
			case OM51:
			case OM51HL:
			case OM5HL:
			case AFTD:
				return 0;
				
			case FSTUD:
			case STUD:
			case RAZZ:
			case STUDHL:
				return 4;
				
			default: 
				throw new RuntimeException();
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
			case FSTUD:
				return fcstudstreetnames;
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
			case BG:
			case DSTD:
			case AFTD:
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
		return streetIndex == getStreets(gametype) - 1;
	}
	
	/** return the maximum number of streets in this game type */
	public static int getStreets (Game.Type gametype) {
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
				return dsLowDrawPoker;
			case AFTD:
				return afLowDrawPoker;
			case STUD:
				return studPoker;
			case RAZZ:
				return razzPoker;
			case STUDHL:
				return studHLPoker;
			case BG:
				return badugiPoker;
			case FSTUD:
				return fiveCardStudPoker;
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
				return Poker.dsLowShortRankNames;
			case RAZZ:
			case AFTD:
				return Poker.afLowShortRankNames;
			case FCD:
			case HE:
			case OM:
			case STUDHL:
			case OM51:
			case OM51HL:
			case OMHL:
			case STUD:
			case OM5HL:
			case OM5:
			case FSTUD:
				return Poker.shortRankNames;
			case BG:
				return Badugi.shortRankNames;
			default:
				throw new RuntimeException();
		}
	}
	
	/**
	 * get the number of draws remaining for the game type and street index
	 */
	public static int getDraws(Game.Type gameType, int streetIndex) {
		switch (gameType) {
			case FSTUD:
			case HE:
			case OM:
			case OM5:
			case OM51:
			case OM51HL:
			case OM5HL:
			case OMHL:
			case RAZZ:
			case STUD:
			case STUDHL: 
				return 0;
				
			case FCD:
			case DSSD:
				// 1, 0 draws
				return 1 - streetIndex;
				
			case BG:
			case DSTD:
			case AFTD:
				// 3, 2, 1, 0 draws
				return 3 - streetIndex;
				
			default:
				throw new RuntimeException();
		}
	}
	
	/**
	 * get the number of up cards that are displayed on the street (index from
	 * 0) for the game type
	 */
	public static int getUpCards(Game.Type gameType, int streetIndex) {
		switch (gameType) {
			case AFTD:
			case BG:
			case DSSD:
			case DSTD:
			case FCD:
			case HE:
			case OM:
			case OM5:
			case OM51:
			case OM51HL:
			case OM5HL:
			case OMHL:
				return 0;
				
			case FSTUD:
				// 0:1, 1:2, 2:3, 3:4
				return streetIndex + 1;
				
			case RAZZ:
			case STUD:
			case STUDHL:
				// 0:1, 1:2, 2:3, 3:4, 4:4
				return Math.min(streetIndex + 1, 4);
				
			default:
				throw new RuntimeException();
		}
	}
	
	/**
	 * return true if a draw like game
	 */
	public static boolean isDraw(Game.Type gameType) {
		return getDraws(gameType, 0) > 0;
	}
	
	/**
	 * return true if stud like game - i.e. has up cards
	 */
	public static boolean isStud(Game.Type gameType) {
		switch (gameType) {
			case AFTD:
			case BG:
			case DSSD:
			case DSTD:
			case FCD:
			case HE:
			case OM:
			case OM5:
			case OM51:
			case OM51HL:
			case OM5HL:
			case OMHL:
				return false;
				
			case FSTUD:
			case RAZZ:
			case STUD:
			case STUDHL:
				return true;
				
			default:
				throw new RuntimeException();
		}
	}
	
	/**
	 * return true if game is high/low split
	 */
	public static boolean isHilo(Game.Type gameType) {
		switch (gameType) {
			case AFTD:
			case BG:
			case DSSD:
			case DSTD:
			case FCD:
			case FSTUD:
			case HE:
			case OM:
			case OM5:
			case OM51:
			case RAZZ:
			case STUD:
				return false;
				
			case OM51HL:
			case OM5HL:
			case OMHL:
			case STUDHL:
				return true;
				
			default:
				throw new RuntimeException();
		}
	}
	
	/**
	 * get the max board size for this game
	 */
	public static int getBoard(Game.Type gameType) {
		switch (gameType) {
			case AFTD:
			case BG:
			case DSSD:
			case DSTD:
			case FCD:
			case FSTUD:
				return 0;
				
			case RAZZ:
			case STUD:
			case STUDHL:
				return 1;
				
			case HE:
			case OM:
			case OM5:
			case OM51:
			case OM51HL:
			case OM5HL:
			case OMHL:
				return 5;
				
			default:
				throw new RuntimeException();
		}
	}
}
