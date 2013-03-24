package pet.hp;

import java.io.Serializable;

/**
 * Represents a type of poker game.
 * There should only be once instance of this class for each game.
 * This object should be considered immutable.
 */
public class Game implements Serializable {
	
	/* game type constants */
	/** five card draw (high) */
	public static final int FCD_TYPE = 1;
	/** hold'em (high) */
	public static final int HE_TYPE = 2;
	/** omaha (high) */
	public static final int OM_TYPE = 3;
	/** omaha high/low */
	public static final int OMHL_TYPE = 4;
	/** deuce to seven triple draw */
	public static final int DSTD_TYPE = 5;
	/** deuce to seven single draw */
	public static final int DSSD_TYPE = 6;
	/** razz (a-5 low) */
	public static final int RAZZ_TYPE = 7;
	/** stud (high) */
	public static final int STUD_TYPE = 8;
	/** stud high/low */
	public static final int STUDHL_TYPE = 9;
	/** stars Courchevel */
	public static final int OM51_TYPE = 10;
	/** stars 5 card omaha */
	public static final int OM5_TYPE = 11;
	/** stars Courchevel high/lo */
	public static final int OM51HL_TYPE = 12;
	/** stars 5 card omaha high/lo */
	public static final int OM5HL_TYPE = 13;
	/** limit type constants */
	public static final int NO_LIMIT = 100, POT_LIMIT = 101, FIXED_LIMIT = 102;
	/** mix type constants */
	public static final int HE_OM_MIX = 1001, TRIPSTUD_MIX = 1002, EIGHT_MIX = 1003, HORSE_MIX = 1004;
	/** currency type constants (excluding $ and €) */
	public static final char PLAY_CURRENCY = 'p', TOURN_CURRENCY = 't';
	/** sub type constants */
	public static final int ZOOM_SUBTYPE = 1;
	
	/** description string (unique for all games) */
	public String id;
	
	/** mixed game type */
	public int mix;
	/** type of game for street and hand analysis purposes */
	public int type;
	/** max players */
	public int max;
	/** limit type */
	public int limit;
	/** hand currency */
	public char currency;
	/** sub type, e.g. zoom */
	public int subtype;
	/** blinds XXX cash game only */
	public int sb, bb;
	/** is a hi/lo game */
	public boolean hilo;
	
	public Game() {
		//
	}
	
	@Override
	public String toString() {
		return "Game[" + id + "]";
	}
	
}
