package pet.hp.impl;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import pet.hp.*;

public class FTParser extends Parser2 {
	
	/**
	 * @param args
	 */
	public static void main(final String[] args) throws Exception {
		final Parser parser = new FTParser();
		try (FileInputStream fis = new FileInputStream("ft.txt")) {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"))) {
				String line;
				while ((line = br.readLine()) != null) {
					final boolean hand = parser.parseLine(line);
				}
			}
		}
	}
	

	public FTParser() {
		// XXX shouldnt be in constructor
		super(new History());
	}
	
	@Override
	public boolean isHistoryFile(final String name) {
		return name.toLowerCase().endsWith(".txt");
	}
	
	@Override
	public boolean parseLine(final String line0) {
		final String line = line0.replaceAll("(\\d),(\\d)", "$1$2").replaceAll("  +", " ").trim();
		System.out.println(">>> " + line);
		debug(">>> " + line);
		String name;
		if (line.startsWith("Full Tilt Poker Game")) {
			parseHand(line);
		} else if (line.startsWith("Seat ")) {
			parseSeat(line);
		} else if ((name = ParseUtil.parseName(seatsMap, line, 0)) != null) {
			parseAction(line, name);
		} else {
			throw new RuntimeException("unmatched line: " + line);
		}
		
		return false;
	}
	
	private void parseAction (String line, String name) {
		// Keynell antes 100
		System.out.println("action: name=" + name);
		Seat seat = seatsMap.get(name);
		String a = line.substring(name.length() + 1, line.indexOf(" ", name.length() + 1));
		System.out.println("a=" + a);
		byte action = ParseUtil.getAction(a);
		switch (action) {
			case Action.ANTES_TYPE: {
//				int amountStart = ParseUtil.nextToken(line, blindStart);
//				int amount = ParseUtil.parseMoney(line, amountStart);
//				if (amount >= hand.sb) {
//					throw new RuntimeException("invalid ante");
//				}
//				hand.antes += amount;
//				pot += amount;
//				amount = 0;
				break;
			}
			default:
				throw new RuntimeException("unknown action " + action);
		}
	}


	private void parseSeat(final String line) {
		if (!summaryPhase) {
			// Seat 3: Keynell (90000)
			int seatno = ParseUtil.parseInt(line, 5);
			int col = line.indexOf(": ");
			int braStart = line.lastIndexOf("(");
			
			Seat seat = new Seat();
			seat.num = (byte) seatno;
			seat.name = history.getString(line.substring(col + 2, braStart - 1));
			seat.chips = ParseUtil.parseMoney(line, braStart + 1);
			seatsMap.put(seat.name, seat);
		}
	}
	
	private void parseHand(final String line) {
		if (hand != null) {
			throw new RuntimeException("did not finish last hand");
		}
		
		Matcher m = FTHandRe.pattern.matcher(line);
		if (!m.matches()) {
			throw new RuntimeException("does not match: " + line);
		}
		
		System.out.println("hid=" + m.group(FTHandRe.hid));
		System.out.println("tname=" + m.group(FTHandRe.tname));
		System.out.println("tid=" + m.group(FTHandRe.tid));
		System.out.println("table=" + m.group(FTHandRe.table));
		System.out.println("tabletype=" + m.group(FTHandRe.tabletype));
		System.out.println("sb=" + m.group(FTHandRe.sb));
		System.out.println("bb=" + m.group(FTHandRe.bb));
		System.out.println("ante=" + m.group(FTHandRe.ante));
		System.out.println("lim=" + m.group(FTHandRe.lim));
		System.out.println("game=" + m.group(FTHandRe.game));
		System.out.println("date1=" + m.group(FTHandRe.date1));
		System.out.println("date2=" + m.group(FTHandRe.date2));
		
		hand = new Hand();
		hand.id = Long.parseLong(m.group(FTHandRe.hid));
		hand.tablename = m.group(FTHandRe.table);
		hand.sb = ParseUtil.parseMoney(m.group(FTHandRe.sb), 0);
		hand.bb = ParseUtil.parseMoney(m.group(FTHandRe.bb), 0);
		hand.antes = ParseUtil.parseMoney(m.group(FTHandRe.ante), 0);
		
		game = new Game();
		game.currency = ParseUtil.parseCurrency(m.group(FTHandRe.sb), 0);
		game.sb = hand.sb;
		game.bb = hand.bb;
		game.limit = ParseUtil.getLimitType(m.group(FTHandRe.lim));
		game.type = ParseUtil.getGameType(m.group(FTHandRe.game));
		
	}
	
}
















