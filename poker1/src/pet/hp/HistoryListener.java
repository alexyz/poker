package pet.hp;

/**
 * interface for picking up new hands, games and tournaments
 */
public interface HistoryListener {
	
	/**
	 * a hand has just been parsed
	 */
	public void handAdded(Hand hand);

	/**
	 * a new game has just been added
	 */
	public void gameAdded(Game game);
	
	/**
	 * a new tournament has just been added
	 */
	public void tournAdded(Tourn tourn);

}
