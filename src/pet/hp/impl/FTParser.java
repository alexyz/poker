package pet.hp.impl;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pet.hp.*;

public class FTParser extends Parser {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Parser parser = new FTParser();
		FileInputStream fis = new FileInputStream("ftgames.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
		String line;
		while ((line = br.readLine()) != null) {
			boolean hand = parser.parseLine(line);
		}
		br.close();
	}
	
	private Hand hand;
	
	public FTParser() {
		super(new History());
	}
	
	@Override
	public boolean isHistoryFile(String name) {
		return name.toLowerCase().endsWith(".txt");
	}
	
	@Override
	public boolean parseLine(String line0) {
		Pattern p = Pattern.compile("(\\d),(\\d)");
		String line = line0.replaceAll("(\\d),(\\d)", "$1$2").replaceAll("  +", " ").trim();
		println(">>> " + line);
		
		if (line.startsWith("Full Tilt Poker Game")) {
			parseGame(line);
		}
		
		return false;
	}
	
	private void parseGame(String line) {
		if (hand != null) {
			throw new RuntimeException("did not finish last hand");
		}
		String re = "";
		// Full Tilt Poker Game #31364220549:
		re += "Full Tilt Poker Game #(\\d+):";
		int hid = 1;
		// 2,000 Play Money Sit & Go (243729666),
		re += "(?: (.+?) \\((\\d+)\\),)?";
		int tourn = 2, tid = 3;
		// Table .COM Play 2    (6 max, ante, deep)
		re += " Table (.+?)( \\(.+?\\))?";
		int table = 4, tabletype = 5;
		// - 300/600 Ante 100
		re += " - (\\d+)/(\\d+)( Ante \\d+)?";
		int sb = 6, bb = 7, ante = 8;
		// No Limit  
		re += " - (Limit|Pot Limit|No Limit)";
		int lim = 9;
		// 2-7 Triple Draw
		re += " (Hold'em|Omaha Hi|Omaha H/L|2-7 Triple Draw|5 Card Draw|Razz|Stud H/L)";
		int game = 10;
		// 20:01:13 UTC - 2012/11/05
		re += " - (\\d+:\\d+:\\d+ \\w+ - \\d+/\\d+/\\d+)";
		int date1 = 11;
		// [02:07:17 ET - 2012/11/10]
		re += "(?: (\\[\\d+:\\d+:\\d+ \\w+ - \\d+/\\d+/\\d+\\]))?";
		int date2 = 12;
		
		Pattern p = Pattern.compile(re);
		Matcher m = p.matcher(line);
		if (!m.matches()) {
			throw new RuntimeException("does not match");
		}
		
		
	}
	
}
















