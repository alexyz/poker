package pet.hp;

import java.util.List;

/**
 * abstract parser interface
 */
public abstract class Parser {
	/**
	 * Parse next line from file.
	 */
	public abstract boolean parseLine(String line);
	/**
	 * Return true if this file can be parsed
	 */
	public abstract boolean isHistoryFile(String name);
	
	/**
	 * reset state of parser for new file
	 */
	public abstract void clear();
	
	/**
	 * Get debug output of parser for last hand
	 */
	public abstract List<String> getDebug();
}
