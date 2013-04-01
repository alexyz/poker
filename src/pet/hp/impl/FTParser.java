
package pet.hp.impl;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.*;

import pet.eq.ArrayUtil;
import pet.hp.*;

import static pet.hp.impl.ParseUtil.*;

/**
 * a tilt parser
 */
public class FTParser extends Parser2 {
	
	/**
	 * @param args
	 */
	public static void main (final String[] args) throws Exception {
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
	
	private final DateFormat df = new SimpleDateFormat("HH:mm:ss zzz - yyyy/MM/dd");
	
	/** current line */
	private String line;
	/** is in summary phase */
	protected boolean summaryPhase;
	private int sitdown = 0;
	
	public FTParser() {
		// XXX shouldnt be in constructor
		super(new History());
		debug = true;
	}
	
	@Override
	public void clear () {
		super.clear();
		summaryPhase = false;
		line = null;
		sitdown = 0;
	}
	
	@Override
	public boolean isHistoryFile (final String name) {
		return name.startsWith("FT") && name.endsWith(".txt");
	}
	
	private void parseAction (String name) {
		// Keynell antes 100
		println("action: name=" + name);
		Seat seat = seatsMap.get(name);
		
		int actIndex = name.length() + 1;
		int actEndIndex = line.indexOf(" ", actIndex);
		if (actEndIndex == -1) {
			actEndIndex = line.length();
		}
		String actStr = line.substring(actIndex, actEndIndex);
		println("actStr=" + actStr);
		
		Action action = new Action(seat);
		action.type = getAction(actStr);
		
		switch (action.type) {
			case ANTE: {
				action.amount = parseMoney(line, actEndIndex + 1);
				assert_(action.amount < hand.sb, "ante < sb");
				hand.db += action.amount;
				anonPip(action.amount);
				// doesn't count toward pip
				break;
			}
			
			case POST: {
				// Keynell posts a dead small blind of 5
				
				// blinds always count toward player pip
				// TODO except dead blinds...
				String sbExp = "posts the small blind of ";
				String bbExp = "posts the big blind of ";
				String dsbExp = "posts a dead small blind of ";
				
				if (line.startsWith(sbExp, actIndex)) {
					action.amount = parseMoney(line, actIndex + sbExp.length());
					assert_(action.amount == hand.sb, "post sb = hand sb");
					seat.smallblind = true;
					seatPip(seat, action.amount);
					println("post sb " + action.amount);
					
				} else if (line.startsWith(bbExp, actIndex)) {
					action.amount = parseMoney(line, actIndex + bbExp.length());
					assert_(action.amount == hand.bb, "action bb = hand bb");
					seat.bigblind = true;
					seatPip(seat, action.amount);
					println("post bb " + action.amount);
					
				} else if (line.startsWith(dsbExp, actIndex)) {
					action.amount = parseMoney(line, actIndex + dsbExp.length());
					assert_(action.amount == hand.sb, "action dsb = hand sb");
					anonPip(action.amount);
					println("post dead sb " + action.amount);
					
				} else if (line.indexOf(" ", actEndIndex + 1) == -1) {
					// Keynell posts 10
					action.amount = parseMoney(line, actEndIndex + 1);
					assert_ (action.amount == hand.bb, "unknown post = bb");
					seat.bigblind = true;
					seatPip(seat, action.amount);
					println("inspecific post " + action.amount);
				
				} else {
					fail("unknown post");
				}
				
				break;
			}
			
			case CALL:
			case BET: {
				// Keynell calls 300
				assert_(line.indexOf(" ", actEndIndex + 1) == -1, "end of line");
				action.amount = parseMoney(line, actEndIndex + 1);
				seatPip(seat, action.amount);
				break;
			}
			
			case RAISE: {
				// x-G-MONEY raises to 2000
				String raiseExp = "raises to ";
				assert_(line.startsWith(raiseExp, actIndex), "raise exp");
				int amountStart = actIndex + raiseExp.length();
				// subtract what seat has already put in this round
				// otherwise would double count
				// have to do inverse when replaying..
				action.amount = parseMoney(line, amountStart) - seatPip(seat);
				seatPip(seat, action.amount);
				break;
			}
			
			case FOLD: {
				// Keynell folds
				assert_(line.indexOf(" ", actEndIndex) == -1, "line complete");
				// int handStart = line.indexOf("[", actEnd);
				// if (handStart > 0) {
				// String[] cards = parseCards(line, handStart);
				// seat.finalHoleCards =
				// checkCards(seat.finalHoleCards,
				// getHoleCards(hand.game.type, cards));
				// seat.finalUpCards = checkCards(seat.finalUpCards,
				// getUpCards(hand.game.type, cards));
				// }
				break;
			}
			
			case SHOW: {
				// bombermango shows [Ah Ad]
				// bombermango shows two pair, Aces and Sevens
				if (line.indexOf("[", actEndIndex + 1) > 0) {
					String[] cards = parseCards(line, actEndIndex + 1);
					seat.finalHoleCards = checkCards(seat.finalHoleCards, getHoleCards(hand.game.type, cards));
					seat.finalUpCards = checkCards(seat.finalUpCards, getUpCards(hand.game.type, cards));
				}
				break;
			}
			
			case COLLECT: {
				// stoliarenko1 wins the pot (2535) with a full house, Twos full
				// of Sevens
				int braIndex = line.indexOf("(", actEndIndex + 1);
				int amount = parseMoney(line, braIndex + 1);
				seat.won += amount;
				
				// add the collect as a fake action so the action amounts sum to
				// pot size
				action.amount = -amount;
				
				if (line.indexOf("with", braIndex) > 0) {
					// assume this means it was a showdown and not just a flash
					hand.showdown = true;
				}
				
				break;
			}
			
			case CHECK:
			case MUCK:
			case DOESNTSHOW:
				// x-G-MONEY mucks
				break;
			
			default:
				fail("action " + action.type);
		}
		
		// any betting action can cause this
		if (line.endsWith("and is all in")) {
			action.allin = true;
		}
		
		if (hand.showdown) {
			seat.showdown = true;
		}
		
		println("action " + action.toString());
		currentStreet().add(action);
	}
	
	private void parseBoard () {
		// Board: [2d 7s Th 8h 7c]
		int braIndex = line.indexOf("[");
		hand.board = checkCards(hand.board, parseCards(line, braIndex));
		println("board " + Arrays.asList(hand.board));
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
		
		// get seat
		// have to skip over name which could be anything
		String prefix = "Dealt to ";
		String name = parseName(seatsMap, line, prefix.length());
		int cardsStart = line.indexOf("[", prefix.length() + name.length());
		Seat theseat = seatsMap.get(name);
		
		// get cards and cards 2
		String[] cards = parseCards(line, cardsStart);
		int cardsStart2 = line.indexOf("[", cardsStart + 1);
		if (cardsStart2 > 0) {
			cards = ArrayUtil.join(cards, parseCards(line, cardsStart2));
		}
		println(name + " dealt " + Arrays.asList(cards));
		
		// get current player seat - always has more than 1 initial hole card
		if (hand.myseat == null && cards.length > 1) {
			println("this is my seat");
			hand.myseat = theseat;
		}
		
		if (theseat == hand.myseat) {
			switch (hand.game.type) {
				case FCD:
				case DSSD:
				case DSTD:
					// hole cards can be changed in draw so store them all on
					// hand
					hand.addMyDrawCards(cards);
				default:
			}
			theseat.finalHoleCards = checkCards(theseat.finalHoleCards, getHoleCards(hand.game.type, cards));
			theseat.finalUpCards = checkCards(theseat.finalUpCards, getUpCards(hand.game.type, cards));
			
		} else {
			// not us, all cards are up cards
			theseat.finalUpCards = checkCards(theseat.finalUpCards, cards);
		}
		
	}
	
	private void parseHand () {
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
		hand.sb = parseMoney(m.group(FTHandRe.sb), 0);
		hand.bb = parseMoney(m.group(FTHandRe.bb), 0);
		String ante = m.group(FTHandRe.ante);
		if (ante != null) {
			hand.ante = parseMoney(ante, 0);
		}

		// 02:46:23 ET - 2012/11/10
		// date2 is always et but may be null, if so date1 is et
		hand.date = parseDates(df, m.group(FTHandRe.date1), m.group(FTHandRe.date2)).getTime();
		
		Game game = new Game();
		game.currency = parseCurrency(m.group(FTHandRe.sb), 0);
		game.sb = hand.sb;
		game.bb = hand.bb;
		game.ante = hand.ante;
		game.limit = getLimitType(m.group(FTHandRe.lim));
		game.type = getGameType(m.group(FTHandRe.game));
		String tabletype = m.group(FTHandRe.tabletype);
		if (tabletype != null && tabletype.contains("heads up")) {
			game.max = 2;
		} else if (tabletype != null && tabletype.matches("\\d max")) {
			game.max = Integer.parseInt(tabletype.substring(0, 1));
		} else {
			// guess max number of players
			switch (game.type) {
				case DSSD:
				case FCD:
				case DSTD:
					game.max = 6;
					break;
				case RAZZ:
				case STUD:
				case STUDHL:
					game.max = 8;
					break;
				default:
					game.max = 9;
			}
		}
		hand.game = history.getGame(game);
		
		newStreet();
	}
	
	@Override
	public boolean parseLine (final String line0) {
		line = line0.replaceAll("(\\d),(\\d)", "$1$2").replaceAll("  +", " ").trim();
		println(">>> " + line);
		
		String name;
		String buttonExp = "The button is in seat #";
		
		if (line.length() == 0) {
			if (summaryPhase && hand != null) {
				finish();
				return true;
			}
			
		} else if (line.startsWith("Full Tilt Poker Game")) {
			parseHand();
			
		} else if (line.startsWith("Seat ")) {
			println("seat");
			parseSeat();
			
		} else if (line.startsWith("Total pot ")) {
			parseTotal();
			
		} else if (line.startsWith("Uncalled bet of ")) {
			parseUncall();
			
		} else if (line.startsWith("Board: ")) {
			parseBoard();
			
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
			
		} else if (line.endsWith(" has 15 seconds left to act")) {
			println("15 secs");
			
		} else if ((name = parseName(seatsMap, line, 0)) != null) {
			parseAction(name);
			
		} else if (line.endsWith(" sits down")) {
			// unrecognised name sits down
			println("sit down");
			sitdown++;
			assert_(seatsMap.size() + sitdown <= hand.game.max, "sit down seats < max");
			
		} else {
			fail("unmatched line: " + seatsMap);
		}
		
		return false;
	}
	
	private void parsePhase () {
		// *** HOLE CARDS *** (not a street)
		// *** FLOP *** [4d 7c 2c] (Total Pot: 120, 4 Players)
		// *** TURN *** [4d 7c 2c] [Kd] (Total Pot: 240, 4 Players)
		// *** RIVER *** [4d 7c 2c Kd] [Ah] (Total Pot: 300, 2 Players)
		// *** SHOW DOWN *** (not a street)
		// *** SUMMARY *** (not a street)
		
		String t = "*** ";
		int a = line.indexOf(t);
		int b = line.indexOf(" ***", a + t.length());
		String name = line.substring(a + t.length(), b);
		boolean newStreet = false;
		boolean ignoreStreet = false;
		
		switch (hand.game.type) {
			case HE:
			case OM:
				if (name.equals("FLOP") || name.equals("TURN") || name.equals("RIVER")) {
					newStreet = true;
				} else if (name.equals("HOLE CARDS") || name.equals("PRE-FLOP")) {
					ignoreStreet = true;
				}
				break;
				
			case FCD:
				// *** HOLE CARDS ***
				if (name.equals("HOLE CARDS")) {
					ignoreStreet = true;
				} else if (name.equals("DRAW")) {
					newStreet = true;
				}
				break;
				
			default:
				fail("unknown game " + hand.game.type);
		}
		
		if (newStreet) {
			pip();
			newStreet();
			println("new street index " + currentStreetIndex());
			
		} else if (name.equals("SHOW DOWN")) {
			println("showdown");
			
		} else if (name.equals("SUMMARY")) {
			println("summary");
			// pip in case there is only one street
			pip();
			summaryPhase = true;
			
		} else if (!ignoreStreet) {
			throw new RuntimeException("unknown phase " + name);
		}
	}
	
	private void parseSeat () {
		if (!summaryPhase) {
			// Seat 3: Keynell (90000)
			int seatno = parseInt(line, 5);
			int col = line.indexOf(": ");
			int braStart = line.lastIndexOf("(");
			
			Seat seat = new Seat();
			seat.num = (byte) seatno;
			seat.name = StringCache.get(line.substring(col + 2, braStart - 1));
			seat.chips = parseMoney(line, braStart + 1);
			seatsMap.put(seat.name, seat);
		}
		// TODO might get opponent cards here
		assert_(seatsMap.size() <= hand.game.max, "seats < max");
	}
	
	private void parseTotal () {
		// Total pot 2535 | Rake 0
		String potExp = "Total pot ";
		hand.pot = parseMoney(line, potExp.length());
		int a = line.indexOf("Rake", potExp.length());
		hand.rake = parseMoney(line, a + 5);
		println("total " + hand.pot + " rake " + hand.rake);
	}
	
	private void parseUncall () {
		// Uncalled bet of 12600 returned to x-G-MONEY
		String uncallExp = "Uncalled bet of ";
		int amount = parseMoney(line, uncallExp.length());
		
		String retExp = "returned to ";
		int retIndex = line.indexOf(retExp, uncallExp.length());
		String name = parseName(seatsMap, line, retIndex + retExp.length());
		Seat seat = seatsMap.get(name);
		seatPip(seat, -amount);
		
		// add the uncall as a fake action so the action amounts sum to pot size
		Action act = new Action(seat);
		act.amount = -amount;
		act.type = Action.Type.UNCALL;
		currentStreet().add(act);
		
		println("uncalled " + name + " " + amount);
	}
	
}
