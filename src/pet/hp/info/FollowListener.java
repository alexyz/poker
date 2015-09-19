package pet.hp.info;

/**
 * interface for receiving file information from the follow thread
 */
public interface FollowListener {
	
	/** a whole file has just been parsed (not on AWT thread!) */
	public void doneFile(int done, int total);
	
}
