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
	
	private void fail(String desc) {
		throw new RuntimeException("Failure: " + desc + " Line: " + line);
	}
	
	private void assert_(boolean cond, String desc) {
		if (!cond) {
			throw new RuntimeException("Assertion failed: " + desc + " Line: " + line);
		}
	}
	
	/** current line */
	private String line;

	public FTParser() {
		// XXX shouldnt be in constructor
		super(new History());
		debug = true;
	}
	
	@Override
	public boolean isHistoryFile(final String name) {
		return name.toLowerCase().endsWith(".txt");
	}
	
	@Override
	public boolean parseLine(final String line0) {
		line = line0.replaceAll("(\\d),(\\d)", "$1$2").replaceAll("  +", " ").trim();
		println(">>> " + line);
		
		String name;
		String buttonExp = "The button is in seat #";
		if (line.startsWith("Full Tilt Poker Game")) {
			parseHand();
			
		} else if (line.startsWith("Seat ")) {
			println("seat");
			parseSeat();
			
		} else if (line.startsWith("*** ")) {
			println("phase");
			parsePhase();
			
		} else if (line.startsWith("Dealt to ")) {
			println("dealt");
			parseDeal();
			
		} else if (line.startsWith(buttonExp)) {
			println("button");
			int but = Integer.parseInt(line.substring(buttonExp.length()));
			hand.button = (byte) but;
			
		} else if ((name = ParseUtil.parseName(seatsMap, line, 0)) != null) {
			parseAction(name);
			
		} else {
			fail("unmatched line");
		}
		
		return false;
	}
	
	private void parseDeal () {
		// omaha:
		// Dealt to Keynell [Tc As Qd 3s]
		// stud:
		// Dealt to mamie2k [4d]
		// Dealt to doubleupnow [3h]
		// Dealt to bcs75 [5d]
		// Dealt to mymommy [Jh]
		// Dealt to Keynell [Qs 3s] [5s]
		// after draw: [kept] [received]
		// Dealt to Keynell [2h 4c] [Qs Kd Kh]
		
	}

	private void parsePhase () {
		// *** HOLE CARDS ***
	}

	private void parseAction (String name) {
		// Keynell antes 100
		println("action: name=" + name);
		Seat seat = seatsMap.get(name);
		
		int actIndex = name.length() + 1;
		int spaceIndex = line.indexOf(" ", actIndex);
		String actStr = line.substring(actIndex, spaceIndex);
		println("actStr=" + actStr);
		
		Action action = new Action(seat);
		action.type = ParseUtil.getAction(actStr);
		
		switch (action.type) {
			case ANTE: {
				int amount = ParseUtil.parseMoney(line, spaceIndex + 1);
				assert_(amount < hand.sb, "ante < sb");
				hand.antes += amount;
				pot += amount;
				amount = 0;
				break;
			}
			case POST: {
				String sbExp = "posts the small blind of ";
				String bbExp = "posts the big blind of ";
				if (line.startsWith(sbExp, actIndex)) {
					action.amount = ParseUtil.parseMoney(line, actIndex + sbExp.length());
					seat.smallblind = true;
					assert_(action.amount == hand.sb, "post sb = hand sb");
					assert_(line.indexOf(" ", actIndex + sbExp.length()) == -1, "line complete");
					
				} else if (line.startsWith(bbExp, actIndex)) {
					action.amount = ParseUtil.parseMoney(line, actIndex + bbExp.length());
					seat.bigblind = true;
					assert_(action.amount == hand.bb, "action bb = hand bb");
					assert_(line.indexOf(" ", actIndex + bbExp.length()) == -1, "line complete");
					
				} else {
					fail("unknown post");
				}
				
				break;
			}
			default:
				fail("action " + action.type);
		}
		
		currentStreet().add(action);
	}


	private void parseSeat() {
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
	
	private void parseHand() {
		assert_(hand == null, "finished last hand");
		
		Matcher m = FTHandRe.pattern.matcher(line);
		if (!m.matches()) {
			throw new RuntimeException("does not match: " + line);
		}
		
		println("hid=" + m.group(FTHandRe.hid));
		println("tname=" + m.group(FTHandRe.tname));
		println("tid=" + m.group(FTHandRe.tid));
		println("table=" + m.group(FTHandRe.table));
		println("tabletype=" + m.group(FTHandRe.tabletype));
		println("sb=" + m.group(FTHandRe.sb));
		println("bb=" + m.group(FTHandRe.bb));
		println("ante=" + m.group(FTHandRe.ante));
		println("lim=" + m.group(FTHandRe.lim));
		println("game=" + m.group(FTHandRe.game));
		println("date1=" + m.group(FTHandRe.date1));
		println("date2=" + m.group(FTHandRe.date2));
		
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
		
		newStreet();
	}
	
}
















