package pet.hp.info;

/**
 * currently unused
 * intended for updating the tournament and game tabs with newly parsed info
 */
public interface InfoListener {
	
	public void playerGameInfoUpdated(PlayerGameInfo pgi);
	
	public void tournInfoUpdated(TournInfo ti);

}
