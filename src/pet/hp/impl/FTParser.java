
package pet.hp.impl;

import java.text.DateFormat;
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
	
	/** line endings to ignore */
	private static String[] endsWithIgnore = {
		" seconds left to act",
		" has returned",
		" has reconnected",
		" is feeling happy",
		" is feeling confused",
		" is feeling normal",
		" is feeling angry",
		" has timed out",
		" is sitting out",
		" has been disconnected",
		" has requested TIME",
		" is dealt 1 card",
		" is dealt 2 cards",
		" is dealt 3 cards",
		" is dealt 4 cards",
		" is dealt 5 cards",
		" stands up",
		" sits down",
		" has registered late for the tournament and will be dealt in on their big blind",
		" seconds to reconnect"
	};
	
	private final DateFormat df = new SimpleDateFormat("HH:mm:ss zzz - yyyy/MM/dd");
	
	/** current line */
	private String line;
	/** is in summary phase */
	protected boolean summaryPhase;
	/** has there been a collect action yet */
	private boolean collected;
	/** is this hand partial, i.e not worth parsing */
	private boolean partial;
	
	public FTParser() {
		//
	}
	
	@Override
	public void clear () {
		super.clear();
		summaryPhase = false;
		line = null;
		collected = false;
		partial = false;
	}
	
	@Override
	public boolean isHistoryFile (final String name) {
		return name.startsWith("FT") && name.endsWith(".txt") && !name.endsWith(" Irish.txt")
				 && !name.endsWith(" - Summary.txt");
	}
	
	private void parseAction (final String name) {
		// Keynell antes 100
		println("action player " + name);
		final Seat seat = seatsMap.get(name);
		
		final int actIndex = name.length() + 1;
		
		int actEndIndex = line.indexOf(" ", actIndex);
		if (actEndIndex == -1) {
			actEndIndex = line.length();
		}
		final String actStr = line.substring(actIndex, actEndIndex);
		println("action str " + actStr);
		
		if (actStr.length() == 0) {
			println("no action");
			return;
		}
		
		final Action action = new Action(seat);
		action.type = ParseUtil.parseAction(actStr);
		
		switch (action.type) {
			case ANTE: {
				// Keynell antes 100
				action.amount = parseMoney(line, actEndIndex + 1);
				assert_(action.amount < hand.sb, "ante < sb");
				// doesn't count toward pip
				anonPip(action.amount);
				break;
			}
			
			case POST: {
				// Keynell posts a dead small blind of 5
				// blinds always count toward player pip
				// TODO except dead blinds...
				final String sbExp = "posts the small blind of ";
				final String bbExp = "posts the big blind of ";
				final String dsbExp = "posts a dead small blind of ";
				
				if (line.startsWith(sbExp, actIndex)) {
					action.amount = parseMoney(line, actIndex + sbExp.length());
					seat.smallblind = true;
					seatPip(seat, action.amount);
					println("post sb");
					
				} else if (line.startsWith(bbExp, actIndex)) {
					action.amount = parseMoney(line, actIndex + bbExp.length());
					seat.bigblind = true;
					seatPip(seat, action.amount);
					println("post bb");
					
				} else if (line.startsWith(dsbExp, actIndex)) {
					action.amount = parseMoney(line, actIndex + dsbExp.length());
					anonPip(action.amount);
					println("post dead sb");
					
				} else if (line.indexOf(" ", actEndIndex + 1) == -1) {
					// Keynell posts 10
					// doesn't say what it's for, so guess bb
					action.amount = parseMoney(line, actEndIndex + 1);
					seat.bigblind = true;
					seatPip(seat, action.amount);
					println("post bb2");
					
				} else {
					fail("unknown post");
				}
				
				break;
			}
			
			case CALL:
			case BET: {
				// Keynell calls 300
				// doubleupnow completes it to 10
				String compExp = "completes it to ";
				if (line.startsWith(compExp, actIndex)) {
					action.amount = parseMoney(line, actIndex + compExp.length());
				} else {
					action.amount = parseMoney(line, actEndIndex + 1);
				}
				seatPip(seat, action.amount);
				break;
			}
			
			case RAISE: {
				// x-G-MONEY raises to 2000
				final String raiseExp = "raises to ";
				assert_(line.startsWith(raiseExp, actIndex), "raise exp");
				// subtract what seat has already put in this round
				// otherwise would double count
				// have to do inverse when replaying..
				action.amount = parseMoney(line, actIndex + raiseExp.length()) - seatPip(seat);
				seatPip(seat, action.amount);
				break;
			}
			
			case FOLD: {
				// Keynell folds
				assert_(line.indexOf(" ", actEndIndex) == -1, "fold eol");
				break;
			}
			
			case SHOW: {
				// bombermango shows [Ah Ad]
				// bombermango shows two pair, Aces and Sevens
				// Allante_93 shows [5c 8c 9s 7s Ah Ks 8h] 98,75,A
				if (line.indexOf("[", actEndIndex + 1) > 0) {
					final String[] cards = parseCards(line, actEndIndex + 1);
					seat.downCards = checkCards(seat.downCards, getDownCards(hand.game.type, cards));
					seat.upCards = checkCards(seat.upCards, getUpCards(hand.game.type, cards));
					println("show upcards: " + Arrays.toString(seat.upCards));
					println("show " + Arrays.toString(cards));
				}
				break;
			}
			
			case COLLECT: {
				// stoliarenko1 wins the pot (2535) with a full house, Twos full of Sevens
				// vestax4 ties for the pot ($0.76) with 7,6,4,3,2
				// laiktoerees wins the side pot (45,000) with a straight, Queen high
				final int braIndex = line.indexOf("(", actEndIndex + 1);
				final int amount = parseMoney(line, braIndex + 1);
				seat.won += amount;
				// sometimes there is no collect, have to fake it in summary phase
				collected = true;
				
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
				assert_(line.indexOf(" ", actEndIndex) == -1, "check/muck eol");
				break;
				
			case DRAW: {
				// Rudapple discards 1 card
				// the actual cards will be set in parseDeal
				assert_(currentStreetIndex() > 0, "draw on street > 0");
				final int draw = currentStreetIndex() - 1;
				assert_(seat.drawn(draw) == 0, "first draw on street");
				final int drawn = parseInt(line, actEndIndex + 1);
				seat.setDrawn(draw, drawn);
				println("drawn " + drawn);
				break;
			}
			
			case STANDPAT: {
				// safrans stands pat
				if (hand.myseat == seat) {
					// there is no deal so push previous hole cards here
					hand.addMyDrawCards(seat.downCards);
				}
				break;
			}
			
			case BRINGSIN: {
				// obvid0nkkk brings in for 3
				Pattern p = Pattern.compile(".+ brings in for (\\d+)");
				Matcher m = p.matcher(line);
				assert_(m.matches(), "brings in");
				action.amount = parseMoney(m.group(1), 0);
				seatPip(seat, action.amount);
				break;
			}
			
			case UNCALL:
				// handled elsewhere
			default:
				fail("missing action " + action.type);
		}
		
		// any betting action can cause this
		if (line.endsWith("and is all in")) {
			action.allin = true;
			println("all in");
		}
		
		if (hand.showdown) {
			seat.showdown = true;
			println("seat showdown");
		}
		
		println("action " + action.toString());
		currentStreet().add(action);
	}
	
	private void parseBoard () {
		// Board: [2d 7s Th 8h 7c]
		final int braIndex = line.indexOf("[");
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
		// if someone is all in
		// Dealt to Allante_93 [5c 8c 9s 7s Ah Ks] [8h]
		
		// get seat
		// have to skip over name which could be anything
		final String prefix = "Dealt to ";
		final String name = parseName(seatsMap, line, prefix.length());
		final int cardsStart = line.indexOf("[", prefix.length() + name.length());
		final Seat seat = seatsMap.get(name);
		
		// get cards and cards 2
		String[] cards = parseCards(line, cardsStart);
		final int cardsStart2 = line.indexOf("[", cardsStart + 1);
		if (cardsStart2 > 0) {
			cards = ArrayUtil.join(cards, parseCards(line, cardsStart2));
		}
		println(name + " dealt " + Arrays.asList(cards));
		
		// get current player seat - always has more than 1 initial hole card
		if (hand.myseat == null && cards.length > 1) {
			println("this is my seat");
			hand.myseat = seat;
		}
		
		if (seat == hand.myseat) {
			if (GameUtil.isDraw(hand.game.type)) {
				// hole cards can be changed in draw so store them all on
				// hand
				hand.addMyDrawCards(cards);
			}
			seat.downCards = checkCards(seat.downCards, getDownCards(hand.game.type, cards));
			seat.upCards = checkCards(seat.upCards, getUpCards(hand.game.type, cards));
			println("deal upcards 1: " + Arrays.toString(seat.upCards));
			
		} else {
			// not us, all cards are up cards, unless the player has shown their hand before showdown
			int uc = GameUtil.getUpCards(hand.game.type, currentStreetIndex());
			
			if (cards.length == uc) {
				// only up cards
				seat.upCards = checkCards(seat.upCards, cards);
				println("deal upcards 2: " + Arrays.toString(seat.upCards));
				
			} else if (cards.length > uc) {
				// mixed
				seat.downCards = checkCards(seat.downCards, getDownCards(hand.game.type, cards));
				seat.upCards = checkCards(seat.upCards, getUpCards(hand.game.type, cards));
				println("deal upcards 3: " + Arrays.toString(seat.upCards));
				
			} else {
				fail("not enough cards");
			}
		}
		
	}
	
	private void parseHand () {
		assert_(hand == null, "finished last hand");
		
		final Matcher m = FTHandRe.pattern.matcher(line);
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
		println("partial=" + m.group(FTHandRe.partial));
		
		if (m.group(FTHandRe.partial) != null) {
			// don't bother with partial hands, they won't validate
			println("ignore partial hand");
			partial = true;
			return;
		}
		
		hand = new Hand();
		hand.id = Long.parseLong(m.group(FTHandRe.hid)) | Hand.FT_ROOM;
		hand.tablename = StringCache.get(m.group(FTHandRe.table));
		hand.sb = parseMoney(m.group(FTHandRe.sb), 0);
		hand.bb = parseMoney(m.group(FTHandRe.bb), 0);
		final String ante = m.group(FTHandRe.ante);
		if (ante != null) {
			hand.ante = parseMoney(ante, 0);
		}
		
		// 02:46:23 ET - 2012/11/10
		// date2 is always et but may be null, if so date1 is et
		hand.date = parseDates(df, m.group(FTHandRe.date1), m.group(FTHandRe.date2)).getTime();
		
		final Game game = new Game();
		game.currency = parseCurrency(m.group(FTHandRe.sb), 0);
		game.sb = hand.sb;
		game.bb = hand.bb;
		game.ante = hand.ante;
		game.limit = parseLimit(m.group(FTHandRe.lim));
		game.type = parseGame(m.group(FTHandRe.game));
		final String tabletype = m.group(FTHandRe.tabletype);
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
				case AFTD:
				case BG:
					game.max = 6;
					break;
				case FSTUD:
				case RAZZ:
				case STUD:
				case STUDHL:
					game.max = 8;
					break;
				case HE:
				case OM:
				case OM5:
				case OM51:
				case OM51HL:
				case OM5HL:
				case OMHL:
					game.max = 9;
					break;
				default:
					throw new RuntimeException();
			}
		}
		if (game.limit == Game.Limit.FL) {
			// fixed limit has big bet and small bet not blinds
			println("convert big bet/small bet to blinds");
			game.bb = game.sb;
			game.sb = game.sb / 2;
			hand.bb = hand.sb;
			hand.sb = hand.sb / 2;
		}
		hand.game = getHistory().getGame(game);
		
		newStreet();
	}
	
	@Override
	public boolean parseLine (String line) {
		// TODO could make this faster by operating on stringbuilder
		// remove null bytes, seem to be a lot of these
		line = line.replace("\u0000", "");
		// remove bom thing?
		line = line.replace("\ufffd", "");
		// take the comma separator out of numbers
		line = line.replaceAll("(\\d),(\\d)", "$1$2");
		// coalesce spaces
		line = line.replaceAll("  +", " ");
		line = line.trim();
		
		this.line = line;
		println(">>> " + line);
		
		String name;
		
		if (line.length() == 0) {
			if (partial) {
				clear();
				
			} else if (summaryPhase && hand != null) {
				println("end of hand");
				finish();
				return true;
			}
			
		} else if (partial) {
			println("ignore partial");
			
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
			parsePhase();
			
		} else if (line.startsWith("Dealt to ")) {
			parseDeal();
			
		} else if (line.startsWith("The button is in seat #")) {
			parseButton(line);
			
		} else if (line.startsWith("The blinds are now ")) {
			//
			
		} else if (line.equals("No low hand qualified")) {
			println("no low");
			hand.showdownNoLow = true;
			
		} else if (line.equals("Time has expired")) {
			//
			
		} else if (line.indexOf(": ") > 0) {
			// following checks assume no talk, i.e. use endsWith
			
		} else if (endsWith(endsWithIgnore, line)) {
			println("ignore");
			
		} else if (line.indexOf(" adds ") > 0) {
			println("adds");
			
		} else if (line.indexOf(" is low with ") > 0) {
			println("low");
			
		} else if (line.indexOf(" is high with ") > 0) {
			println("high");
			
		} else if ((name = parseName(seatsMap, line, 0)) != null) {
			parseAction(name);
			
		} else {
			fail("unmatched line");
		}
		
		return false;
	}
	
	private void parseButton (String line) {
		println("button");
		final Matcher m = Pattern.compile("The button is in seat #(\\d)").matcher(line);
		assert_ (m.matches(), "button pattern");
		final int but = Integer.parseInt(m.group(1));
		hand.button = (byte) but;
	}
	
	private void parsePhase () {
		// *** HOLE CARDS *** (not a street)
		// *** FLOP *** [4d 7c 2c] (Total Pot: 120, 4 Players)
		// *** TURN *** [4d 7c 2c] [Kd] (Total Pot: 240, 4 Players)
		// *** RIVER *** [4d 7c 2c Kd] [Ah] (Total Pot: 300, 2 Players)
		// *** SHOW DOWN *** (not a street)
		// *** SUMMARY *** (not a street)
		
		final String t = "*** ";
		final int a = line.indexOf(t);
		final int b = line.indexOf(" ***", a + t.length());
		final String name = line.substring(a + t.length(), b);
		
		switch (name) {
			case "3RD STREET":
				if (hand.game.type != Game.Type.FSTUD) {
					// ignore for 7 stud, new street in 5 stud
					break;
				}
				
			case "FLOP":
			case "TURN":
			case "RIVER":
			case "DRAW":
			case "FIRST DRAW":
			case "SECOND DRAW":
			case "THIRD DRAW":
			case "4TH STREET":
			case "5TH STREET":
			case "6TH STREET":
			case "7TH STREET":
				println("new street " + name);
				pip();
				newStreet();
				println("new street index " + currentStreetIndex());
				break;
				
			case "SHOW DOWN":
				// note there may not be a show down phase even if there is a showdown
				hand.showdown = true;
				break;
			
			case "HOLE CARDS":
			case "PRE-FLOP":
			case "2ND STREET":
				println("ignore street " + name);
				break;
				
			case "SUMMARY":
				println("summary");
				// pip in case there is only one street
				// though the street may not actually be over if there is a missing uncall/collect
				pip();
				summaryPhase = true;
				break;
				
			default:
				fail("unknown phase: " + name);
		}
		
	}
	
	private void parseSeat () {
		if (summaryPhase) {
			parseSeatSummary();
			
		} else {
			// Seat 3: Keynell (90000)
			final int seatno = parseInt(line, 5);
			final int col = line.indexOf(": ");
			final int braStart = line.lastIndexOf("(");
			
			final Seat seat = new Seat();
			seat.num = (byte) seatno;
			seat.name = StringCache.get(line.substring(col + 2, braStart - 1));
			seat.chips = parseMoney(line, braStart + 1);
			seatsMap.put(seat.name, seat);
			assert_(seatsMap.size() <= hand.game.max, "seats < max");
		}
	}
	
	private void parseSeatSummary () {
		println("seat summary: collected=" + collected);
		// Seat 4: CougarMD                showed [7c 6s 4h 3s 2s] and won ($0.57) with 7,6,4,3,2
		// Seat 6: Keynell                 showed [Qh Qc 9d 9h 5d] and won ($0.14) with two pair, Queens and Nines
		// Seat 3: Srta_Arruez (big blind) showed [Ah Tc 9s 6h 4c] and lost with Ace Ten high
		// Seat 3: redcar 55   (big blind) mucked [Ad 9h 4c 3c 2h] - A,9,4,3,2
		// Seat 1: Cherry65    (big blind) mucked [Td 7c 5c 4s 2d] - T,7,5,4,2
		// Seat 6: Keynell     (big blind) mucked [Kh Ks 6c 6d 5c] - two pair, Kings and Sixes
		// Seat 3: Srta_Arruez (big blind) collected ($0.02), mucked
		// Seat 6: yarden311   (button)    collected (29000), mucked
		// Seat 1: Keynell     (big blind) collected (600)
		String nameExp = "(.+?)(?: \\(.+?\\))?";
		int seatGroup = 1, nameGroup = 2, cardsGroup = 3, wonGroup = 4, amountGroupShow = 5;
		String showExp = "Seat (\\d): " + nameExp + " showed (\\[.+?\\]) and (lost|won \\((.+?)\\)) with .+";
		String muckExp = "Seat (\\d): " + nameExp + " mucked (.+?) - .+";
		int amountGroupColl = 3;
		String collExp = "Seat (\\d): " + nameExp + " collected \\((.+)\\)(?:, mucked)?";
		boolean collect = false;
		int amount = 0;
		Seat seat = null;
		
		Matcher m;
		if ((m = Pattern.compile(showExp).matcher(line)).matches()) {
			println("show exp");
			seat = seatsMap.get(m.group(nameGroup));
			assert_ (seat.num == Integer.parseInt(m.group(seatGroup)), "seat num");
			String[] cards = parseCards(m.group(cardsGroup), 0);
			seat.downCards = checkCards(seat.downCards, getDownCards(hand.game.type, cards));
			seat.upCards = checkCards(seat.upCards, getUpCards(hand.game.type, cards));
			boolean won = m.group(wonGroup).startsWith("won");
			seat.showdown = true;
			hand.showdown = true;
			if (!this.collected && won) {
				amount = parseMoney(m.group(amountGroupShow), 0);
				collect = true;
			}
			
		} else if ((m = Pattern.compile(muckExp).matcher(line)).matches()) {
			println("muck exp");
			seat = seatsMap.get(m.group(nameGroup));
			assert_ (seat.num == Integer.parseInt(m.group(seatGroup)), "seat num");
			String[] cards = parseCards(m.group(cardsGroup), 0);
			seat.downCards = checkCards(seat.downCards, getDownCards(hand.game.type, cards));
			seat.upCards = checkCards(seat.upCards, getUpCards(hand.game.type, cards));
			seat.showdown = true;
			
		} else if ((m = Pattern.compile(collExp).matcher(line)).matches()) {
			println("coll exp");
			seat = seatsMap.get(m.group(nameGroup));
			assert_ (seat.num == Integer.parseInt(m.group(seatGroup)), "seat num");
			if (!this.collected) {
				amount = parseMoney(m.group(amountGroupColl), 0);
				collect = true;
			}
			
			// XXX ehh remove this
		} else if (line.contains("mucked") || line.contains("collected") || line.contains("showed")) {
			fail("unmatched summary");
		}
		
		if (collect) {
			// there was no win action, add here
			// note that more than one seat can win
			
			println("hand.pot=" + hand.pot);
			println("pot()=" + pot());
			println("seat.pip=" + seat.pip);
			println("seatPip(seat)=" + seatPip(seat));
			
			if (pot() > hand.pot) {
				// there is a missing uncalled bet action
				// add uncall to balance pot
				int ucamount = pot() - hand.pot;
				// should really check seatPip(seat), but pip() has already been called
				if (ucamount <= seat.pip) {
					// assume it is for the current seat (though it might not be for multiple pots)
					println("add missing uncall " + ucamount);
					anonPop(ucamount);
					seat.pip -= ucamount;
					// add the uncall as a fake action so the action amounts sum to pot size
					final Action action = new Action(seat);
					action.amount = -ucamount;
					action.type = Action.Type.UNCALL;
					println("action " + action);
					currentStreet().add(action);
				}
			}
			
			while (!GameUtil.isShowdown(hand.game.type, currentStreetIndex())) {
				println("new street");
				newStreet();
			}
			
			println("add missing collect");
			seat.won = amount;
			Action action = new Action(seat);
			action.type = Action.Type.COLLECT;
			action.amount = -amount;
			println("action " + action);
			currentStreet().add(action);
		}
	}
	
	private void parseTotal () {
		// Total pot 2535 | Rake 0
		// Total pot 4,410 Main pot 4,140. Side pot 270. | Rake 0
		final String potExp = "Total pot ";
		hand.pot = parseMoney(line, potExp.length());
		final int a = line.indexOf("Rake", potExp.length());
		hand.rake = parseMoney(line, a + 5);
		println("total " + hand.pot + " rake " + hand.rake);
	}
	
	private void parseUncall () {
		// Uncalled bet of 12600 returned to x-G-MONEY
		final String uncallExp = "Uncalled bet of ";
		final int amount = parseMoney(line, uncallExp.length());
		
		final String retExp = "returned to ";
		final int retIndex = line.indexOf(retExp, uncallExp.length());
		final String name = parseName(seatsMap, line, retIndex + retExp.length());
		final Seat seat = seatsMap.get(name);
		seatPip(seat, -amount);
		
		// add the uncall as a fake action so the action amounts sum to pot size
		final Action act = new Action(seat);
		act.amount = -amount;
		act.type = Action.Type.UNCALL;
		currentStreet().add(act);
		
		println("uncalled " + name + " " + amount);
	}
	
}
