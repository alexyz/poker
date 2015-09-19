package pet.hp.impl;

import java.io.File;

import pet.hp.Parser;


public class HistoryUtil {
	
	public static File getTiltPath(FTParser parser) {
		// "/Users/alex/Documents/HandHistory/Keynell"
		String home = System.getProperty("user.home");
		return getDir(home + "/Documents/HandHistory", parser);
	}
	
	/** get the pokerstars hand history directory */
	public static File getStarsPath(PSParser parser) {
		// C:\Users\Alex\AppData\Local\PokerStars\HandHistory\
		// /Users/alex/Library/Application Support/PokerStars/HandHistory/tawvx
		String home = System.getProperty("user.home");
		String os = System.getProperty("os.name");
		String path;
		if (os.equals("Mac OS X")) {
			path = home + "/Library/Application Support/PokerStars/HandHistory";
		} else if (os.contains("Windows")) {
			// could be something like PokerStars.FR instead
			path = home + "\\AppData\\Local\\PokerStars\\HandHistory";
		} else {
			path = home;
		}
		return getDir(path, parser);
	}
	
	/**
	 * get dir within dir with files matching regex.
	 * return home dir if none found.
	 */
	private static File getDir(String path, Parser parser) {
		File f = new File(path);
		if (f.exists() && f.isDirectory()) {
			// get the first directory containing files matching the regex
			for (File f2 : f.listFiles()) {
				if (f2.isDirectory()) {
					final String[] list = f2.list();
					if (list != null) {
						for (String f3 : list) {
							if (parser.isHistoryFile(f3)) {
								return f2;
							}
						}
					}
				}
			}
		}
		return f;
	}
}
