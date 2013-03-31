
package pet.hp;

import java.io.Serializable;

/**
 * Represents a type of poker game. There should only be once instance of this
 * class for each game. This object should be considered immutable.
 */
public class Game implements Serializable {
	
	/** game type constants */
	public enum Type {
		/** five card draw (high) */
		FCD,
		/** hold'em (high) */
		HE,
		/** omaha (high) */
		OM,
		/** omaha high/low */
		OMHL,
		/** deuce to seven triple draw */
		DSTD,
		/** deuce to seven single draw */
		DSSD,
		/** razz (a-5 low) */
		RAZZ,
		/** stud (high) */
		STUD,
		/** stud high/low */
		STUDHL,
		/** stars Courchevel */
		OM51,
		/** stars 5 card omaha */
		OM5,
		/** stars Courchevel high/lo */
		OM51HL,
		/** stars 5 card omaha high/lo */
		OM5HL,
	}
	
	/** limit type constants */
	public enum Limit {
		/** no limit */
		NL, 
		/** pot limit */
		PL, 
		/** fixed limit */
		FL;
	}
	
	/** mix type constants */
	public enum Mix {
		HE_OM_MIX, TRIPSTUD_MIX, EIGHT_MIX, HORSE_MIX;
	}
	
	/** currency type constants (excluding $ and €) */
	public static final char PLAY_CURRENCY = 'p', TOURN_CURRENCY = 't';
	
	/** sub type constants */
	public static final int ZOOM_SUBTYPE = 1;
	
	/** description string (unique for all games) */
	public String id;
	
	/** mixed game type */
	public Game.Mix mix;
	/** type of game for street and hand analysis purposes */
	public Game.Type type;
	/** max players */
	public int max;
	/** limit type */
	public Game.Limit limit;
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
	public String toString () {
		return "Game[" + id + "]";
	}
	
}
