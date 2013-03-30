package pet.hp.impl;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pet.hp.*;

public class FTParser extends Parser {
	
	/**
	 * @param args
	 */
	public static void main(final String[] args) throws Exception {
		final Parser parser = new FTParser();
		try (FileInputStream fis = new FileInputStream("ftgames.txt")) {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"))) {
				String line;
				while ((line = br.readLine()) != null) {
					final boolean hand = parser.parseLine(line);
				}
			}
		}
	}
	
	private Hand hand;
	private Game game;
	
	public FTParser() {
		super(new History());
	}
	
	@Override
	public boolean isHistoryFile(final String name) {
		return name.toLowerCase().endsWith(".txt");
	}
	
	@Override
	public boolean parseLine(final String line0) {
		final Pattern p = Pattern.compile("(\\d),(\\d)");
		System.out.println(line0);
		final String line = line0.replaceAll("(\\d),(\\d)", "$1$2").replaceAll("  +", " ").trim();
		System.out.println(line);
		debug(">>> " + line);
		
		if (line.startsWith("Full Tilt Poker Game")) {
			parseGame(line);
		}
		
		return false;
	}
	
	private void parseGame(final String line) {
		if (hand != null) {
			throw new RuntimeException("did not finish last hand");
		}
		if (!line.startsWith("Full Tilt Poker Game #")) {
			throw new RuntimeException();
		}
		
		String re = "";
		// Full Tilt Poker Game #31364220549:
		re += "Full Tilt Poker Game #(\\d+):";
		int hid = 1;
		// 2,000 Play Money Sit & Go (243729666),
		re += "(?: (.+?) \\((\\d+)\\),)?";
		int tname = 2, tid = 3;
		// Table .COM Play 2    (6 max, ante, deep)
		re += " Table (.+?)(?: \\((.+?)\\))?";
		int table = 4, tabletype = 5;
		// - 300/600 Ante 100
		re += " - (\\S+)/(\\S+)( Ante \\S+)?";
		int sb = 6, bb = 7, ante = 8;
		// No Limit  
		re += " - (Limit|Pot Limit|No Limit)";
		int lim = 9;
		// 2-7 Triple Draw
		re += " (.+)";
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
			throw new RuntimeException("does not match: " + line);
		}
		
		System.out.println("hid=" + m.group(hid));
		System.out.println("tname=" + m.group(tname));
		System.out.println("tid=" + m.group(tid));
		System.out.println("table=" + m.group(table));
		System.out.println("tabletype=" + m.group(tabletype));
		System.out.println("sb=" + m.group(sb));
		System.out.println("bb=" + m.group(bb));
		System.out.println("ante=" + m.group(ante));
		System.out.println("lim=" + m.group(lim));
		System.out.println("game=" + m.group(game));
		System.out.println("date1=" + m.group(date1));
		System.out.println("date2=" + m.group(date2));
		
	}
	
}
















