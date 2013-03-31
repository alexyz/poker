package pet.hp.impl;

import java.util.Map;

import pet.hp.Action;
import pet.hp.Game;

/**
 * utility methods for strings
 */
public class ParseUtil {
	
	private static final String romanDigits = "IVXLCDM";
	private static final int[] romanValues = new int[] { 1, 5, 10, 50, 100, 500, 1000 };

	/**
	 * remove the extraneous characters from the string, including duplicate
	 * spaces, and remove space from start and end
	 */
	static String strip(String s, String chars) {
		StringBuilder sb = new StringBuilder();
		boolean sp = true;
		for (int n = 0; n < s.length(); n++) {
			char c = s.charAt(n);
			if (chars.indexOf(c) == -1) {
				if (c == ' ') {
					if (sp) {
						continue;
					} else {
						sp = true;
					}
				} else {
					sp = false;
				}
				sb.append(c);
			}
		}
		if (sb.charAt(sb.length() - 1) == ' ') {
			sb.delete(sb.length() - 1, sb.length());
		}
		return sb.toString();
	}

	/**
	 * poker stars actually uses roman numerals for tournament levels...
	 */
	static int parseRoman(String r, int n) {
		int t = 0, v = 0, p = 1;
		char c;
		while (n < r.length() && (c = r.charAt(n++)) != ' ') {
			int x = romanValues[romanDigits.indexOf(c)];
			if (x > p) {
				// subtract previous value from this
				v = x - v;
			} else if (x < p) {
				// add previous to total
				t += v;
				v = x;
			} else {
				// just add
				v += x;
			}
			p = x;
		}
		return t + v;
	}
	
	/**
	 * Get the money amount at offset
	 */
	static int parseMoney(String line, int off) {
		// $0
		// $2
		// $1.05
		boolean dec = false;
		if ("$€".indexOf(line.charAt(off)) >= 0) {
			off++;
			dec = true;
		}
		int v = 0;
		int n = off;
		boolean dp = false;
		while (n < line.length()) {
			int c = line.charAt(n);
			if (c >= '0' && c <= '9') {
				v = (v * 10) + (c - '0');
			} else if (!dp && c == '.') {
				dp = true;
			} else {
				break;
			}
			n++;
		}
		if (n == off) {
			throw new RuntimeException("no money at " + off + ": " + line.substring(off));
		}
		if (dec && !dp) {
			v *= 100;
		}
		return v;
	}

	/**
	 * get integer at offset
	 */
	static int parseInt(String line, int off) {
		int end = off;
		while ("0123456789".indexOf(line.charAt(end)) >= 0) {
			end++;
		}
		String s = line.substring(off, end);
		return Integer.parseInt(s);
	}

	/**
	 * skip non spaces then skip spaces
	 */
	static int nextToken(String line, int off) {
		while (line.charAt(off) != ' ') {
			off++;
		}
		while (line.charAt(off) == ' ') {
			off++;
		}
		return off;
	}

	/**
	 * get the currency symbol or play currency symbol if there is no symbol
	 */
	static char parseCurrency(String line, int off) {
		char c = line.charAt(off);
		if ("$€".indexOf(c) >= 0) {
			return c;
		} else if (c >= '0' && c <= '9') {
			// could be tourn chips...
			return Game.PLAY_CURRENCY;
		} else {
			throw new RuntimeException("unknown currency " + c);
		}
	}

	static Game.Limit getLimitType (String limits) {
		switch (limits) {
			case "Pot Limit":
				return Game.Limit.PL;
			case "No Limit":
				return Game.Limit.NL;
			case "Limit":
				return Game.Limit.FL;
			default:
				throw new RuntimeException("unknown limit");
		}
	}

	static Game.Type getGameType (String gameStr) {
		switch (gameStr) {
			case "Hold'em":
				return Game.Type.HE;
			case "Omaha Hi/Lo":
				return Game.Type.OMHL;
			case "Omaha":
			case "Omaha Hi":
				return Game.Type.OM;
			case "5 Card Draw":
				return Game.Type.FCD;
			case "Triple Draw 27 Lowball":
				return Game.Type.DSTD;
			case "Razz":
				return Game.Type.RAZZ;
			case "7 Card Stud":
				return Game.Type.STUD;
			case "7 Card Stud Hi/Lo":
				return Game.Type.STUDHL;
			case "Single Draw 27 Lowball":
				return Game.Type.DSSD;
			case "Courchevel":
				return Game.Type.OM51;
			case "5 Card Omaha":
				return Game.Type.OM5;
			case "5 Card Omaha Hi/Lo":
				return Game.Type.OM5HL;
			case "Courchevel Hi/Lo":
				return Game.Type.OM51HL;
			default:
				throw new RuntimeException("unknown game " + gameStr);
		}
	}

	static Action.Type getAction(String act) {
		switch (act) {
			// map stars terms to action constants
			case "checks": return Action.Type.CHECK;
			case "folds": return Action.Type.FOLD;
			case "mucks": return Action.Type.MUCK;
			case "doesn't": return Action.Type.DOESNTSHOW;
			case "bets": return Action.Type.BET;
			case "calls": return Action.Type.CALL;
			case "raises": return Action.Type.RAISE;
			case "antes": return Action.Type.ANTE;
			case "posts": return Action.Type.POST;
			case "shows": return Action.Type.SHOW;
			case "discards": return Action.Type.DRAW;
			case "stands": return Action.Type.STANDPAT;
			case "brings": return Action.Type.BRINGSIN;
			default: throw new RuntimeException("unknown action " + act);
		}
	}
	
	/**
	 * return player name at given offset.
	 * return null if no name found
	 */
	static String parseName(Map<String,?> seatsMap, String line, int off) {
		String name = "";
		// find longest matching name
		for (String n : seatsMap.keySet()) {
			if (n.length() > name.length() && line.startsWith(n, off)) {
				name = n;
			}
		}
		return name.length() == 0 ? null : name;
	}
}
