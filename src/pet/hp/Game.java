package pet.hp;

import java.io.Serializable;

/**
 * Represents a type of poker game.
 * There should only be once instance of this class for each game.
 * This object should be considered immutable.
 */
public class Game implements Serializable {
	
	/** game type constants */
	public static final char FCD_TYPE = 'F', HE_TYPE = 'H', OM_TYPE = 'O', OMHL_TYPE = '8';
	/** limit type constants */
	public static final char NO_LIMIT = 'N', POT_LIMIT = 'P', FIXED_LIMIT = 'F';
	/** mix type constants */
	public static final char HE_OM_MIX = 'M';
	/** currency type constants (excluding $ and €) */
	public static final char PLAY_CURRENCY = 'p', TOURN_CURRENCY = 't';
	/** subtype constants */
	public static final int ZOOM_SUBTYPE = 1;
	
	/** description string (unique for all games) */
	public String id;
	
	/** mixed game type */
	public char mix;
	/** type of game for street and hand analysis purposes */
	public char type;
	/** max players */
	public int max;
	/** limit type */
	public char limit;
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
