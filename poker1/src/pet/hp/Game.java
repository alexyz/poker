package pet.hp;

/**
 * Represents a type of poker game.
 * There should only be once instance of this class for each game.
 */
public class Game implements Comparable<Game> {
	
	/** game type constants */
	public static final char FCD_TYPE = 'F', HE_TYPE = 'H', OM_TYPE = 'O', OMHL_TYPE = '8';
	/** limit type constants */
	public static final char NO_LIMIT = 'N', POT_LIMIT = 'P', FIXED_LIMIT = 'F';
	/** mix type constants */
	public static final char HE_OM_MIX = 'M';
	/** currency type constants (excluding $ and €) */
	public static final char PLAY_CURRENCY = 'p', TOURN_CURRENCY = 't';
	/** subtype constants */
	public static final char ZOOM_SUBTYPE = 'Z';
	
	/** description string (unique for all games) */
	public String id;
	/** hand currency */
	public char currency;
	/** mixed game type */
	public char mix;
	/** type of game for street and hand analysis purposes */
	public char type;
	/** subtype of game (e.g. zoom) */
	public char subtype;
	/** limit type */
	public char limit;
	/** blinds XXX cash game only */
	public int sb, bb;
	
	@Override
	public int compareTo(Game o) {
		return id.compareTo(o.id);
	}
	
	@Override
	public String toString() {
		return "Game[" + id + "]";
	}
}
