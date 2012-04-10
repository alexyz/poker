package pet.hp;

/**
 * represents a type of poker game
 */
public class Game implements Comparable<Game> {
	
	// TODO blinds, bet type, remove name from id
	// num hole cards for replayer
	// tidy up game stuff in hand utils
	
	// Play 5 Card Draw NL 6max Zoom 0.01/0.02
	// (cur) (name) (bet) (max) (zoom) (blinds)
	
	/** description string (unique for all games) */
	public final String id;
	/** hand currency, $, € or P */
	public final char currency;
	/** type of game for street and hand analysis purposes */
	public final char type;
	/** max number of players at table */
	public final int max;
	/** game name including blinds */
	public final String name;
	
	public Game(String name, int max) {
		this.name = name;
		this.max = max;
		
		if (name.contains("Hold'em")) {
			type = HandUtil.HE_TYPE;
		} else if (name.contains("Omaha")) {
			type = HandUtil.OM_TYPE;
		} else if (name.contains("5 Card Draw")) {
			type = HandUtil.FCD_TYPE;
		} else {
			throw new RuntimeException("unknown game " + name);
		}
		
		if (name.indexOf("$") >= 0) {
			currency = '$';
		} else if (name.indexOf("€") >= 0) {
			currency = '€';
		} else {
			// assume play money
			currency = 'P';
		}
		
		id = String.format("%c-%c-%d-%s", currency, type, max, name);
	}
	
	/** is holdem or omaha */
	public boolean isHoldemType() {
		return "HO".indexOf(type) >= 0;
	}
	
	/** is five card draw */
	public boolean isDrawType() {
		return type == HandUtil.FCD_TYPE;
	}
	
	@Override
	public int compareTo(Game o) {
		return id.compareTo(o.id);
	}
	
	@Override
	public String toString() {
		return "Game[" + id + "]";
	}
}
