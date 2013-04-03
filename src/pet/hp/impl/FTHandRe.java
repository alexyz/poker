package pet.hp.impl;

import java.util.regex.Pattern;

final class FTHandRe {

	static final int hid = 1;
	static final int tname = 2, tid = 3;
	static final int table = 4, tabletype = 5;
	static final int sb = 6, bb = 7, ante = 8;
	static final int lim = 9;
	static final int game = 10;
	static final int date1 = 11;
	static final int date2 = 12;
	static final int partial = 13;
	static final String re = 
			// Full Tilt Poker Game #31364220549:
			"Full Tilt Poker Game #(\\d+):"
			// 2,000 Play Money Sit & Go (243729666),
			+ "(?: (.+?) \\((\\d+)\\),)?"
			// Table .COM Play 2    (6 max, ante, deep)
			+ " Table (.+?)(?: \\((.+?)\\))?"
			// - 300/600 Ante 100
			+ " - (\\S+)/(\\S+)(?: Ante (\\S+))?"
			// No Limit  
			+ " - (Limit|Pot Limit|No Limit)"
			// 2-7 Triple Draw
			+ " (.+)"
			// 20:01:13 UTC - 2012/11/05
			+ " - (\\d+:\\d+:\\d+ \\w+ - \\d+/\\d+/\\d+)"
			// [02:07:17 ET - 2012/11/10]
			+ "(?: \\[(\\d+:\\d+:\\d+ \\w+ - \\d+/\\d+/\\d+)\\])?"
			//  (partial)
			+ "( \\(partial\\))?";
	static final Pattern pattern = Pattern.compile(re);
}