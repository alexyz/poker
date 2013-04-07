
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
		FCD("5 Card Draw"),
		/** hold'em (high) */
		HE("Hold'em"),
		/** omaha (high) */
		OM("Omaha"),
		/** omaha high/low */
		OMHL("Omaha H/L"),
		/** deuce to seven triple draw */
		DSTD("2-7 Triple Draw"),
		/** ace to five triple draw */
		AFTD("A-5 Triple Draw"),
		/** deuce to seven single draw */
		DSSD("2-7 Single Draw"),
		/** razz (a-5 low) */
		RAZZ("Razz"),
		/** stud (high) */
		STUD("7 Card Stud"),
		/** stud high/low */
		STUDHL("7 Card Stud H/L"),
		/** 5 card stud */
		FSTUD("5 Card Stud"),
		/** stars Courchevel */
		OM51("5+1 Card Omaha"),
		/** stars 5 card omaha */
		OM5("5 Card Omaha"),
		/** stars Courchevel high/lo */
		OM51HL("5+1 Card Omaha H/L"),
		/** stars 5 card omaha high/lo */
		OM5HL("5 Card Omaha H/L"),
		/** badugi (ugh) */
		BG("Badugi");
		public final String desc;
		private Type(String desc) {
			this.desc = desc;
		}
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
		/** holdem/omaha */
		HO("Mixed HE/OM"), 
		/** triple stud */
		TS("Triple Stud"),
		/** eight game */
		EG("8-Game"), 
		/** horse */
		HORSE("HORSE");
		public final String desc;
		private Mix(String desc) {
			this.desc = desc;
		}
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
	/** blinds - cash game only. also on each hand. */
	public int sb, bb, ante;
	
	public Game() {
		//
	}
	
	@Override
	public String toString () {
		return "Game[" + id + "]";
	}
	
}
