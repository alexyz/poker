package pet.eq.impl;


/**
 * methods for getting complete hold'em/omaha boards, either
 * randomly or with combinatorial enumeration
 */
abstract class HEBoard {
	/** starting board, never changes */
	final String[] current;
	/** remaining cards in deck, never changes */
	final String[] deck;
	/** next board after call to next() */
	final String[] board = new String[5];
	public HEBoard(String[] deck, String[] current) {
		this.deck = deck;
		this.current = current;
	}
	/** how many boards are there */
	abstract int count();
	/** create the next board */
	abstract void next();
	/** how many cards will be picked */
	abstract int pick();
	/** is this an exact enumeration */
	abstract boolean exact();
}
	
