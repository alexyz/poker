package pet.hp;

import java.io.Serializable;

/**
 * Represents a type of poker game.
 * There should only be once instance of this class for each game.
 * This object should be considered immutable.
 */
public class Game implements Serializable {
	
	/* game type constants */
	public static final int FCD_TYPE = 1;
	public static final int HE_TYPE = 2;
	public static final int OM_TYPE = 3;
	public static final int OMHL_TYPE = 4;
	public static final int DSTD_TYPE = 5;
	public static final int DSSD_TYPE = 6;
	public static final int RAZZ_TYPE = 7;
	public static final int STUD_TYPE = 8;
	public static final int STUDHL_TYPE = 9;
	/** limit type constants */
	public static final int NO_LIMIT = 100, POT_LIMIT = 101, FIXED_LIMIT = 102;
	/** mix type constants */
	public static final int HE_OM_MIX = 1001, TRIPSTUD_MIX = 1002, EIGHT_MIX = 1003, HORSE_MIX = 1004;
	/** currency type constants (excluding $ and €) */
	public static final char PLAY_CURRENCY = 'p', TOURN_CURRENCY = 't';
	/** subtype constants */
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
	
	@Override
	public String toString() {
		return "Game[" + id + "]";
	}
}
