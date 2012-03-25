package pet.hp;

import java.util.List;

public abstract class Parser {
	/**
	 * Get hands that have been parsed so far
	 */
	public abstract List<Hand> getHands();
	/**
	 * Parse next line from file.
	 * Return hand if this line completes one, otherwise null
	 */
	public abstract Hand parseLine(String line);
	/**
	 * Return true if this file can be parsed
	 */
	public abstract boolean isHistoryFile(String name);
}
