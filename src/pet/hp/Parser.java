package pet.hp;

import java.util.*;

/**
 * abstract parser interface
 */
public abstract class Parser {
	
	/** print everything to System.out */
	public boolean debug;
	/** debug output in case of parse error */
	private final List<String> debuglines = new ArrayList<>();
	/** where to send parsed data */
	protected final History history;
	
	public Parser(History history) {
		this.history = history;
	}
	
	/**
	 * add a line to the debug log for this hand
	 */
	protected void println(String s) {
		debuglines.add(s);
		if (debug) {
			System.out.println(s);
		}
	}
	
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
	public void clear() {
		debuglines.clear();
	}
	
	/**
	 * Get debug output of parser for last hand
	 */
	public List<String> getDebug() {
		return debuglines;
	}
	
}
