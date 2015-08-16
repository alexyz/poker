package pet.hp;

/**
 * interface for picking up new hands, games and tournaments
 */
public interface HistoryListener {
	
	/**
	 * a hand has just been parsed (not on AWT thread!)
	 */
	public void handAdded(Hand hand);

	/**
	 * a new game has just been added (not on AWT thread!)
	 */
	public void gameAdded(Game game);
	
}
