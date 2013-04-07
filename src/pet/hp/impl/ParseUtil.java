package pet.hp.impl;

import java.text.DateFormat;
import java.util.*;

import pet.hp.Action;
import pet.hp.Game;

/**
 * utility methods for strings
 */
public class ParseUtil {
	
	private static final String romanDigits = "IVXLCDM";
	private static final int[] romanValues = new int[] { 1, 5, 10, 50, 100, 500, 1000 };
	private static final TimeZone ET = TimeZone.getTimeZone("US/Eastern");
	
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
	 * parse one or both dates as ET or UTC
	 */
	protected static Date parseDates(DateFormat df, String dateStr1, String dateStr2) {
		Date date1 = parseDate(df, dateStr1);
		Date date2 = parseDate(df, dateStr2);
		if (date1 != null && date2 != null && !date1.equals(date2)) {
			throw new RuntimeException("date1=" + date1 + " date2=" + date2);
		}
		return date1 != null ? date1 : date2;
	}
	
	/**
	 * parse date as ET or UTC
	 */
	protected static Date parseDate(DateFormat df, String dateStr) {
		Date date = null;
		if (dateStr != null && dateStr.length() > 0) {
			try {
				if (dateStr.contains("UTC")) {
					date = df.parse(dateStr);
				} else if (dateStr.contains("ET")) {
					// parse as winter time
					date = df.parse(dateStr.replace("ET", "EST"));
					// EDT - summer, EST - winter
					// daylight savings is summer time
					if (ET.inDaylightTime(date)) {
						// reparse as summer time
						date = df.parse(dateStr.replace("ET", "EDT"));
					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			//System.out.println("date " + dateStr + " -> " + date);
		}
		return date;
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
	
	static Game.Limit parseLimit (String limits) {
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
	
	static Game.Type parseGame (String gameStr) {
		switch (gameStr) {
			case "Hold'em":
				return Game.Type.HE;
			case "Omaha H/L":
			case "Omaha Hi/Lo":
				return Game.Type.OMHL;
			case "Omaha":
			case "Omaha Hi":
				return Game.Type.OM;
			case "5 Card Draw":
				return Game.Type.FCD;
			case "Triple Draw 27 Lowball":
			case "2-7 Triple Draw":
				return Game.Type.DSTD;
			case "Razz":
				return Game.Type.RAZZ;
			case "7 Card Stud":
			case "Stud Hi":
				return Game.Type.STUD;
			case "7 Card Stud Hi/Lo":
			case "Stud H/L":
				return Game.Type.STUDHL;
			case "Single Draw 27 Lowball":
			case "2-7 Single Draw":
				return Game.Type.DSSD;
			case "Courchevel":
				return Game.Type.OM51;
			case "5 Card Omaha":
				return Game.Type.OM5;
			case "5 Card Omaha Hi/Lo":
				return Game.Type.OM5HL;
			case "Courchevel Hi/Lo":
				return Game.Type.OM51HL;
			case "Badugi":
				return Game.Type.BG;
			case "A-5 Triple Draw":
				return Game.Type.AFTD;
			case "5 Card Stud":
				return Game.Type.FSTUD;
			default:
				throw new RuntimeException("unknown game " + gameStr);
		}
	}
	
	/** map stars terms to action constants */
	static Action.Type parseAction(String act) {
		switch (act) {
			case "checks": return Action.Type.CHECK;
			case "ties":
			case "wins": return Action.Type.COLLECT;
			case "folds": return Action.Type.FOLD;
			case "mucks": return Action.Type.MUCK;
			case "doesn't": return Action.Type.DOESNTSHOW;
			case "bets": return Action.Type.BET;
			case "completes":
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
	
	/**
	 * get the cards
	 */
	static String[] parseCards(String line, int off) {
		// [Jc 8h Js Ad]
		if (line.startsWith("[", off)) {
			int end = line.indexOf("]", off);
			int num = (end - off) / 3;
			String[] cards = new String[num];
			for (int n = 0; n < num; n++) {
				int a = off + 1 + (n * 3);
				// could validate this, but pretty unlikely to be invalid
				cards[n] = StringCache.get(line.substring(a, a+2));
			}
			return cards;
			
		} else {
			throw new RuntimeException("no hand at " + off);
		}
	}
	
	/** get the (private) hole cards from the array depending on game type */
	static String[] getDownCards(final Game.Type gametype, final String[] cards) {
		switch (gametype) {
			case FSTUD:
				// only one down card in 5 stud
				return new String[] { cards[0] };
				
			case STUD:
			case STUDHL:
			case RAZZ:
				// first two cards and last are hole, others are pub
				// cards length is 3,4,5,6,7
				if (cards.length < 7) {
					return new String[] { cards[0], cards[1] };
				} else {
					return new String[] { cards[0], cards[1], cards[6] }; 
				}
				
			case AFTD:
			case BG:
			case DSSD:
			case DSTD:
			case FCD:
			case HE:
			case OM:
			case OM5:
			case OM51:
			case OM51HL:
			case OM5HL:
			case OMHL:
				// all cards are hole cards
				return cards;
				
			default:
				throw new RuntimeException();
		}
	}
	
	/**
	 * get the up cards from the array depending on the game type. return null
	 * if there are no up cards
	 */
	static String[] getUpCards(final Game.Type gametype, final String[] cards) {
		switch (gametype) {
			case FSTUD:
				return Arrays.copyOfRange(cards, 1, cards.length);
				
			case STUD:
			case STUDHL:
			case RAZZ:
				// first two cards and last are hole, others are pub
				// cards length is 3,4,5,6,7
				return Arrays.copyOfRange(cards, 2, Math.min(cards.length, 6));
				
			case AFTD:
			case BG:
			case DSSD:
			case DSTD:
			case FCD:
			case HE:
			case OM:
			case OM5:
			case OM51:
			case OM51HL:
			case OM5HL:
			case OMHL:
				// no up cards in these games
				return null;
				
			default:
				throw new RuntimeException();
		}
	}
	
	/** check cards haven't got shorter */
	static String[] checkCards(String[] oldCards, String[] cards) {
		if (oldCards != null && (cards == null || oldCards.length > cards.length)) {
			throw new RuntimeException("old: " + Arrays.toString(oldCards) + " new: " + Arrays.toString(cards));
		}
		return cards;
	}
	
	/**
	 * return true if line ends with something in the array
	 */
	static boolean endsWith(String[] endsWith, String line) {
		for (String s : endsWith) {
			if (line.endsWith(s)) {
				return true;
			}
		}
		return false;
	}
	
	private ParseUtil() {
		//
	}
}
