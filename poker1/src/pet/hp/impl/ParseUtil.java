package pet.hp.impl;

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
			throw new RuntimeException("no money at " + off);
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

}
