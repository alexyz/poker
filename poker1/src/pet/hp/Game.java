package pet.hp;

public class Game {
	// TODO max, limit
	
	/** game name including blinds */
	public String name;
	/** hand currency, $, € or P */
	public char currency;
	/** type of game for street and hand analysis purposes */
	public char type;
	/** is holdem or omaha */
	public boolean isHoldemType() {
		return "HO".indexOf(type) >= 0;
	}
	/** is five card draw */
	public boolean isDrawType() {
		return type == HandUtil.FCD_TYPE;
	}
	@Override
	public String toString() {
		return String.format("%c-%c-%d-%s", currency, type, -1, name);
	}
}
