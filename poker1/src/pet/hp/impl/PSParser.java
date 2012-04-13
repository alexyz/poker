package pet.hp.impl;

import java.io.*;
import java.text.*;
import java.util.*;

import pet.eq.ArrayUtil;
import pet.hp.*;

/**
 * PokerStars hand parser - primarily for Omaha/Holdem/Draw cash games.
 * TODO file/line/offset link
 * TODO tournaments and oh/l
 */
public class PSParser extends Parser {

	private static final TimeZone ET = TimeZone.getTimeZone("US/Eastern");
	private static final DateFormat shortDateFormat = new SimpleDateFormat("yyyy/MM/dd");
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss zzz");
	private static final Map<String,Byte> actionMap = new HashMap<String,Byte>();

	static {
		// map stars terms to action constants
		actionMap.put("checks", Action.CHECK_TYPE);
		actionMap.put("folds", Action.FOLD_TYPE);
		actionMap.put("mucks", Action.MUCK_TYPE);
		actionMap.put("doesn't", Action.DOESNTSHOW_TYPE);
		actionMap.put("calls", Action.CALL_TYPE);
		actionMap.put("bets", Action.BET_TYPE);
		actionMap.put("calls", Action.CALL_TYPE);
		actionMap.put("raises", Action.RAISE_TYPE);
		actionMap.put("posts", Action.POST_TYPE);
		actionMap.put("shows", Action.SHOW_TYPE);
		actionMap.put("discards", Action.DRAW_TYPE);
		actionMap.put("stands", Action.STANDPAT_TYPE);
	}

	/** string cache to avoid multiple instances of same string */
	private final Map<String,String> cache = new HashMap<String,String>();
	/** list of completed hands */
	private final List<Hand> hands = new ArrayList<Hand>();
	//private final Map<String,Game> games = new TreeMap<String,Game>();
	private final List<Game> games = new ArrayList<Game>();

	public boolean debug;

	/** map of player name to seat for current hand */
	// TODO see if hashmap or array is faster
	private final Map<String,Seat> seatsMap = new TreeMap<String,Seat>();
	/** array of seat num to seat pip for this street. seat numbers are 1-10 */
	private final int[] seatPip = new int[11];
	private int pot;
	/** streets of action for current hand */
	private transient final List<List<Action>> streets = new ArrayList<List<Action>>();
	/** current hand */
	private Hand hand;
	/** hand reached showdown */
	private boolean showdown = false;
	/** is in summary phase */
	private boolean summaryPhase = false;
	private final List<String> debuglines = new ArrayList<String>();
	private String handline;

	public PSParser() {
		//
	}

	private void println(String s) {
		debuglines.add(s);
		if (debug) {
			System.out.println(s);
		}
	}

	@Override
	public boolean isHistoryFile(String name) {
		if (!name.contains("H-L") && name.startsWith("HH") && name.endsWith(".txt")) {
			int a = name.indexOf(" ");
			int b = name.indexOf(" ", a + 1);
			String tname = name.substring(a + 1, b);
			return !tname.matches("T\\d+");
		} else {
			return false;
		}
	}

	@Override
	public void clear() {
		showdown = false;
		summaryPhase = false;
		//for (Seat seat : seatsMap.values()) {
		//println("seat " + seat + " pip " + seat.pip + " show=" + seat.showdown + " hole " + Arrays.asList(seat.hole));
		//}
		seatsMap.clear();
		//seatsList.clear();
		streets.clear();
		// XXX should already be clear
		Arrays.fill(seatPip, 0);
		pot = 0;
		hand = null;
		debuglines.clear();
		handline = null;
	}

	@Override
	public List<String> getDebug() {
		return debuglines;
	}

	/**
	 * parse next line from file
	 * if the line completes a hand, it is returned
	 */
	@Override
	public Hand parseLine(String line) {
		Hand ret = null;

		if (line.length() > 0 && line.charAt(0) == 0xfeff) {
			line = line.substring(1);
			println("skip bom");
		}

		line = line.trim();

		println(">  " + line);

		if (line.length() == 0) {
			if (summaryPhase && hand != null) {
				// finalise hand and return
				hand.seats = seatsMap.values().toArray(new Seat[seatsMap.size()]);
				// prob already sorted
				Arrays.sort(hand.seats, HandUtil.seatCmp);
				hand.streets = new Action[streets.size()][];
				for (int n = 0; n < streets.size(); n++) {
					List<Action> street = streets.get(n);
					hand.streets[n] = street.toArray(new Action[street.size()]);
				}
				hand.showdown = showdown;
				println("end of hand " + hand);
				hands.add(hand);
				ret = hand;
				clear();
			}

		} else if (line.startsWith("PokerStars ")) {
			parseHand(line);

		} else if (line.startsWith("Betting is capped")) {
			// TODO
			println("capped");

		} else if (line.startsWith("Table")) {
			parseTable(line);

		} else if (line.startsWith("Seat")) {
			parseSeat(line);

		} else if (line.startsWith("***")) {
			parsePhase(line);

		} else if (line.startsWith("Board ")) {
			// Board [6d 3s Qc 8s 5d]
			int a = nextToken(line, 0);
			hand.board = parseHand(line, a);
			println("board " + Arrays.asList(hand.board));

		} else if (line.startsWith("Dealt to")) {
			parseDeal(line);

		} else if (line.startsWith("Uncalled")) {
			parseUncall(line);

		} else if (line.startsWith("Total pot")) {
			parseTotal(line);


			/// ---------- ends with ----------


		} else if (line.endsWith("sits out")) {
			// h_fa: sits out 
			println("sit out");

		} else if (line.endsWith("is sitting out")) {
			// scotty912: is sitting out 
			println("sitting out");

		} else if (line.endsWith("has timed out")) {
			// Festo5811 has timed out
			println("timed out");

		} else if (line.endsWith("leaves the table")) {
			// kuca444 leaves the table
			println("leaves");

		} else if (line.endsWith("is connected")) {
			println("connected");

		} else if (line.endsWith("is disconnected")) {
			println("connected");

		} else if (line.endsWith("has timed out while disconnected")) {
			println("timed out");

		} else if (line.endsWith("has timed out while being disconnected")) {
			println("timed out");

		} else if (line.endsWith("was removed from the table for failing to post")) {
			println("kicked");

		} else if (line.endsWith("will be allowed to play after the button")) {
			println("play after");


			// -------- contains ---------


		} else if (line.contains("said,")) {
			// tawvx said, "it's not a race"
			println("talk");

		} else if (line.contains("joins the table at seat")) {
			// scotty912 joins the table at seat #6 
			println("joins");

		} else if (line.contains("collected")) {
			parseCollect(line);

		} else if (line.contains(": ")) {
			parseAction(line);

		} else {
			System.out.println("unknown line");
			System.out.println("> " + line);
			throw new RuntimeException("unknown line " + line);
		}

		return ret;
	}

	private void parseCollect(String line) {
		// olasz53 collected $1.42 from main pot
		// NightPred8or collected $1.41 from main pot
		// olasz53 collected $0.56 from side pot
		// NightPred8or collected $0.56 from side pot
		int a = line.indexOf("collected");
		String name = line.substring(0, a - 1);
		Seat seat = seatsMap.get(name);
		if (seat == null) {
			throw new RuntimeException("could not find seat " + name);
		}
		int amount = parseMoney(line, a + 10);
		seat.won += amount;
		
		// add the collect as a fake action so the action amounts sum to pot size
		Action act = new Action(seat);
		act.amount = -amount;
		act.type = Action.COLLECT_TYPE;
		streets.get(streets.size() - 1).add(act);
		
		println("collected " + name + " " + amount);
	}

	private void parseTotal(final String line) {
		// Total pot $0.30 | Rake $0.01 
		// Total pot $4.15 Main pot $2.83. Side pot $1.12. | Rake $0.20 
		hand.pot = parseMoney(line, 10);
		if (hand.pot != pot) {
			throw new RuntimeException("total pot " + hand.pot + " not equal to running pot " + pot);
		}
		int a = line.indexOf("Rake");
		hand.rake = parseMoney(line, a + 5);

		// validate pot size
		// FIXME remove these
		int won = 0;
		int lost = 0;
		for (Seat seat : seatsMap.values()) {
			won += seat.won;
			lost += seat.pip;
		}
		if (won != (hand.pot - hand.rake)) {
			throw new RuntimeException("pot " + pot + " not equal to total won " + won + " - rake " + hand.rake);
		}
		if (won != (lost - hand.rake + hand.db)) {
			throw new RuntimeException("won " + won + " not equal to lost " + lost);
		}
		int asum = hand.db - hand.rake;
		for (List<Action> str : streets) {
			for (Action ac : str) {
				asum += ac.amount;
			}
		}
		if (asum != 0) {
			throw new RuntimeException("actsum " + asum + " not zero");
		}

		println("total " + hand.pot + " rake " + hand.rake);
	}

	private void parseUncall(final String line) {
		// Uncalled bet ($0.19) returned to Hokage_91
		int amountStart = line.indexOf("(") + 1;
		int amount = parseMoney(line, amountStart);
		int nameStart = line.indexOf("to") + 3;
		String name = line.substring(nameStart);
		Seat seat = seatsMap.get(name);
		if (seat == null) {
			throw new RuntimeException("could not get seat " + name);
		}
		//seat.uncalled = amount;
		seatPip[seat.num] -= amount;
		
		// add the uncall as a fake action so the action amounts sum to pot size
		Action act = new Action(seat);
		act.amount = -amount;
		act.type = Action.UNCALL_TYPE;
		streets.get(streets.size() - 1).add(act);
		
		println("uncalled " + name + " " + amount);
	}

	private void parseDeal(final String line) {
		// Dealt to tawvx [4c 6h 7d 5h]
		// after draw...
		// Dealt to tawvx [Ts Th] [Kc 5d Qh]
		// if discard all
		// Dealt to tawvx [5c 7h 3d 4d Jh]

		int handStart = line.indexOf("[");
		String name = line.substring(9, handStart - 1);
		Seat myseat = seatsMap.get(name);
		if (myseat == null) {
			throw new RuntimeException("could not find seat " + name);
		}
		if (hand.myseat != null && hand.myseat != myseat) {
			throw new RuntimeException("two seats");
		}
		hand.myseat = myseat;

		String[] h = parseHand(line, handStart);
		int b = line.indexOf("[", handStart + 1);
		if (b > 0) {
			h = ArrayUtil.join(h, parseHand(line, b));
		}

		if (hand.myhole == null) {
			// first hand
			println("dealt " + Arrays.asList(h));
			hand.myhole = h;
		}

		// last hand
		if (myseat.hole != null) {
			println("dealt new hand " + Arrays.asList(h));
		}
		myseat.hole = h;
	}

	private void parseSeat(final String line) {
		int a = line.indexOf(":");
		int seatno = parseInt(line, a - 1);

		if (summaryPhase) {
			// Seat 1: 777KTO777 folded before Flop (didn't bet)
			// Seat 2: tawvx showed [4c 6h 7d 5h] and won ($0.44) with a straight, Four to Eight
			// Seat 4: fearvanilla folded before Flop (didn't bet)
			// Seat 5: $AbRaO$ TT folded on the Flop
			// Seat 6: Sama�ito mucked [2h 6c Qh Jh]
			// Seat 7: azacel77 (button) folded before Flop (didn't bet)
			// Seat 8: Bumerang16 (small blind) folded on the Flop
			// Seat 9: NSavov (big blind) folded on the Flop

			int b = line.indexOf("mucked");
			if (b > 0) {
				// get opponent hand
				String[] hand = parseHand(line, b + 7);
				// ehhh...
				for (Seat seat : seatsMap.values()) {
					if (seat.num == seatno) {
						// could be mucking more than they showed
						checkMuckedHand(seat.hole, hand);
						seat.hole = hand;
					}
				}
				println("seat summary " + seatno + " hand " + Arrays.asList(hand));

			} else {
				println("seat summary");
			}

		} else {
			// Seat 2: tawvx ($2.96 in chips) 
			// Seat 6: abs(EV) ($2.40 in chips) 

			int b = line.lastIndexOf("(");

			Seat seat = new Seat();
			seat.num = (byte) seatno;
			seat.name = cache(line.substring(a + 2, b - 1));
			seat.chips = parseMoney(line, b + 1);
			seatsMap.put(seat.name, seat);
			//seatsList.add(seat);
			println("seat " + seat);
		}
	}

	private void parseHand(final String line) {
		// PokerStars Game #73347266323:  Omaha Pot Limit ($0.01/$0.02 USD) - 2012/01/05 16:12:04 ET
		// PokerStars Game #73076810536:  5 Card Draw No Limit (100/200) - 2011/12/31 14:45:08 ET
		// PokerStars Game #73112640557: Tournament #493078525, 2000+110 Omaha Pot Limit - Level I (10/20) - 2012/01/01 13:43:02 ET
		// PokerStars Game #73111358128:  Hold'em Pot Limit (100/200) - 2012/01/01 13:19:41 ET
		// PokerStars Zoom Hand #77405734487:  Omaha Pot Limit ($0.01/$0.02) - 2012/03/18 14:38:20 ET
		if (hand != null) {
			throw new RuntimeException("did not not finish parsing last hand");
		}

		if (line.contains("Tournament")) {
			throw new RuntimeException("no tournaments");
		}
		
		// get game later
		handline = line;

		Hand hand = new Hand();
		int i1 = line.indexOf("#");
		int i2 = line.indexOf(":", i1);
		int ns = nextToken(line, i2);
		int i4 = line.indexOf("-", i2);
		int ds = nextToken(line, i4);

		// TODO check if game already exists
		hand.id = parseLong(line, i1 + 1);

		String datestr = line.substring(ds);
		try {
			// 2011/12/31 14:45:08 ET
			// 2012/04/11 10:41:17 ET
			// ET can mean either EDT (summer dst) or EST (winter)
			// FIXME this is probably parsed as local time zone so could be
			// wrong by a few hours each year
			Date d1 = shortDateFormat.parse(datestr.substring(0, datestr.indexOf(" ")));
			boolean dst = ET.inDaylightTime(d1);
			Date date = dateFormat.parse(datestr.replace("ET", dst ? "EDT" : "EST"));
			hand.date = date;
		} catch (Exception e) {
			throw new RuntimeException("could not parse date " + datestr, e);
		}

		this.hand = hand;

		// create first street
		streets.add(new ArrayList<Action>());

		println("hand " + hand.id);
	}

	private void parseTable(final String line) {
		// Table 'Roehla IX' 9-max Seat #7 is the button
		// Table 'Sabauda VI' 6-max (Play Money) Seat #5 is the button
		// Table 'Honoria V' 6-max Seat #6 is the button
		// Table 'Mekbuda VIII' 2-max (Play Money) Seat #2 is the button
		// Table '493078525 1' 9-max Seat #1 is the button
		// Table 'bltable.1225797637.1225917089' 6-max
		// seat 1 is button if unspec
		int tableStart = line.indexOf("'");
		int tableEnd = line.indexOf("'", tableStart + 1);
		hand.tablename = cache(line.substring(tableStart + 1, tableEnd));
		println("table " + hand.tablename);

		// fix limit real money holdem games can be 10 player
		int maxStart = nextToken(line, tableEnd + 1);
		int max = parseInt(line, maxStart);
		if (max == 0 || max > 10) {
			throw new RuntimeException("invalid max " + line);
		}

		// FIXME zoom doesn't include play in text, only in file name
		//boolean play = line.contains("Play Money");
		hand.game = getGame(handline, max);

		int d = line.indexOf("Seat");
		if (d > 0) {
			hand.button = Integer.parseInt(line.substring(d + 6, d + 7));

		} else {
			// assume button in seat one for zoom
			hand.button = 1;
		}
	}

	private void parsePhase(final String line) {
		// posts
		// *** HOLE CARDS ***
		// *** FLOP *** [6d 3s Qc]
		// *** TURN *** [6d 3s Qc] [8s]
		// *** RIVER *** [6d 3s Qc 8s] [5d]
		// *** SHOW DOWN ***
		// *** SUMMARY ***

		// posts
		// *** DEALING HANDS ***
		// (discards)
		// (bets)
		// *** SHOW DOWN ***
		// *** SUMMARY ***

		int a = line.indexOf("***");
		int b = line.indexOf("***", a + 3);
		String name = line.substring(a + 4, b - 1);
		boolean newstr = false;
		boolean ignstr = false;

		if (hand.game.type == Game.HE_TYPE || hand.game.type == Game.OM_TYPE) {
			if (name.equals("FLOP") || name.equals("TURN") || name.equals("RIVER")) {
				newstr = true;
			} else if (name.equals("HOLE CARDS")) {
				ignstr = true;
			}


		} else if (hand.game.type == Game.FCD_TYPE) {
			if (name.equals("DEALING HANDS")) {
				ignstr = true;
			}
		}

		if (newstr) {
			pip();
			streets.add(new ArrayList<Action>());
			println("new street " + streets.size());

		} else if (name.equals("SHOW DOWN")) {
			println("showdown");
			showdown = true;

		} else if (name.equals("SUMMARY")) {
			println("summary");
			// pip in case there is only one street
			pip();
			summaryPhase = true;

		} else if (!ignstr) {
			throw new RuntimeException("unknown phase " + name);
		}
	}

	private void parseAction(final String line) {
		// Bumerang16: posts small blind $0.01
		int nameEnd = line.indexOf(": ");
		String name = line.substring(0, nameEnd);
		Seat seat = seatsMap.get(name);
		if (seat == null) {
			throw new RuntimeException("unknown player: " + line);
		}

		int actStart = nextToken(line, nameEnd);
		int actEnd = endToken(line, actStart);
		Action action = new Action(seat);
		action.type = actionMap.get(line.substring(actStart, actEnd));
		boolean draw = false;

		switch (action.type) {
			case Action.CHECK_TYPE:
			case Action.MUCK_TYPE:
			case Action.DOESNTSHOW_TYPE:
				// NSavov: checks 
				// scotty912: doesn't show hand
				break;

			case Action.FOLD_TYPE: {
				// azacel77: folds
				// Ninjajundiai: folds [5d 5s]
				int handStart = line.indexOf("[");
				if (handStart > 0) {
					String[] hand = parseHand(line, handStart);
					checkNewHand(seat.hole, hand);
					seat.hole = hand;
				}
				break;
			}

			case Action.CALL_TYPE:
			case Action.BET_TYPE: {
				// Bumerang16: calls $0.01
				int amountStart = nextToken(line, actEnd);
				int amount = parseMoney(line, amountStart);
				action.amount = amount;
				seatPip[seat.num] += amount;
				break;
			}

			case Action.RAISE_TYPE: {
				// bluff.tb: raises $0.05 to $0.07
				int amountStart = line.indexOf("to ", actEnd) + 3;
				// subtract what seat has already put in this round
				int amount = parseMoney(line, amountStart) - seatPip[seat.num];
				action.amount = amount;
				seatPip[seat.num] += amount;
				break;
			}

			case Action.POST_TYPE: {
				// Bumerang16: posts small blind $0.01
				// pisti361: posts small & big blinds $0.03
				// Yury.Nik: posts big blind 50 and is all-in
				// small and big blinds always posted first due to position
				// though very occasionally the big blind may not be posted (?)

				int blindStart = line.indexOf("blind", actEnd);
				int amountStart = nextToken(line, blindStart);
				int amount = parseMoney(line, amountStart);
				if (line.indexOf("small blind", actEnd) > 0) {
					if (hand.sb == 0) {
						println("small blind " + amount);
						if (amount != hand.game.sb) {
							throw new RuntimeException("invalid small blind");
						}
						hand.sb = amount;

					} else {
						// dead blind
						println("dead small blind " + amount);
						hand.db += amount;
						pot += amount;
						amount = 0;
					}

					seat.smallblind = true;

				} else if (line.indexOf("small & big blinds", actEnd) > 0) {
					// dead small blind doesn't count towards pip (but does count towards pot)
					if (hand.sb == 0) {
						throw new RuntimeException("post sb+bb without sb");
					}
					println("dead small and big blind " + amount);
					hand.db += hand.sb;
					pot += hand.sb;
					amount -= hand.sb;
					seat.bigblind = true;
					seat.smallblind = true;

				} else if (line.indexOf("big blind", actEnd) > 0) {
					println("big blind " + amount);
					if (amount != hand.game.bb) {
						throw new RuntimeException("invalid big blind");
					}
					hand.bb = amount;
					seat.bigblind = true;

				} else {
					throw new RuntimeException("unknown post");
				}

				seatPip[seat.num] += amount;
				action.amount = amount;
				break;
			}

			case Action.SHOW_TYPE: {
				// bluff.tb: shows [Jc 8h Js Ad] (two pair, Aces and Kings)
				//showdown = true;
				int handStart = nextToken(line, actEnd);
				String[] hand = parseHand(line, handStart);
				checkNewHand(seat.hole, hand);
				seat.hole = hand;
				break;
			}

			case Action.DRAW_TYPE: {
				draw = true;
				// tawvx: discards 1 card [Ah]
				// joven2010: discards 3 cards
				if (seat.discards > 0) {
					throw new RuntimeException("already discarded " + seat.discards);
				}
				int discardsStart = nextToken(line, actEnd);
				seat.discards = (byte) parseInt(line, discardsStart);
				break;
			}

			case Action.STANDPAT_TYPE: {
				// stands pat
				draw = true;
				println("stands");
				break;
			}

			default:
				throw new RuntimeException("unknown action: " + action.type);
		}

		if (showdown) {
			// action after show down phase
			seat.showdown = true;
		}

		// any betting action can cause this
		if (line.endsWith("and is all-in")) {
			action.allin = true;
		}

		println("action " + action);

		if (draw && streets.size() == 1) {
			// there is no draw phase, so pip and fake a new street
			println("new street for draw");
			pip();
			streets.add(new ArrayList<Action>());
		}

		List<Action> street = streets.get(streets.size() - 1);
		street.add(action);
	}

	/**
	 * update seat pip and running pot
	 */
	private void pip() {
		for (Seat seat : seatsMap.values()) {
			int pip = seatPip[seat.num];
			if (pip > 0) {
				println("seat " + seat + " pip " + pip); 
				pot += pip;
				seat.pip += pip;
				seatPip[seat.num] = 0;
			}
		}
		println("pot now " + pot);
	}

	private String cache(String s) {
		if (s != null) {
			String s2 = cache.get(s);
			if (s2 != null) {
				return s2;
			}
			s = new String(s);
			cache.put(s, s);
		}
		return s;
	}

	private String[] parseHand(String line, int off) {
		// [Jc 8h Js Ad]
		if (line.charAt(off) == '[') {
			int end = line.indexOf("]", off);
			int num = (end - off) / 3;
			String[] cards = new String[num];
			for (int n = 0; n < num; n++) {
				int a = off + 1 + (n * 3);
				cards[n] = cache(line.substring(a, a+2));
			}
			return cards;
		} else {
			throw new RuntimeException("no hand at " + off);
		}
	}

	/**
	 * return index of first character after token
	 */
	private static int endToken(String line, int off) {
		while (off < line.length() && line.charAt(off) != ' ') {
			off++;
		}
		return off;
	}

	/**
	 * skip non spaces then skip spaces
	 */
	private static int nextToken(String line, int off) {
		while (line.charAt(off) != ' ') {
			off++;
		}
		while (line.charAt(off) == ' ') {
			off++;
		}
		return off;
	}

	private static long parseLong(String line, int off) {
		int end = off;
		while (".0123456789".indexOf(line.charAt(end)) >= 0) {
			end++;
		}
		String s = line.substring(off, end);
		return Long.parseLong(s);
	}

	private static int parseInt(String line, int off) {
		int end = off;
		while (".0123456789".indexOf(line.charAt(end)) >= 0) {
			end++;
		}
		String s = line.substring(off, end);
		return Integer.parseInt(s);
	}

	/**
	 * Get the money amount at offset
	 */
	private static int parseMoney(String line, int off) {
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
	 * check new hand is not differet to old hand if the old hand was already set
	 */
	static void checkNewHand(String[] oldhand, String[] newhand) {
		if (oldhand != null) {
			// could be out of order if drawn
			String[] oh = oldhand.clone();
			Arrays.sort(oh);
			String[] nh = newhand.clone();
			Arrays.sort(nh);
			if (!Arrays.equals(oh, nh)) {
				throw new RuntimeException("hand changed from " + Arrays.asList(oldhand) + " to " + Arrays.asList(newhand)); 
			}
		}
	}

	/**
	 * check new hand is not much different to old hand if the old hand was already set
	 */
	static void checkMuckedHand(String[] oldhand, String[] newhand) {
		if (oldhand != null) {
			if (newhand.length < oldhand.length) {
				throw new RuntimeException("new hand shorter than old hand");
			}
			// just check old cards are in new hand
			for (int n = 0; n < oldhand.length; n++) {
				boolean found = false;
				for (int m = 0; m < newhand.length; m++) {
					if (oldhand[n].equals(newhand[m])) {
						found = true;
						break;
					}
				}
				if (!found) {
					throw new RuntimeException("partial hand changed from " + Arrays.asList(oldhand) + " to " + Arrays.asList(newhand));
				}
			}
		}
	}

	/**
	 * get the game for the hand line and table details
	 */
	private Game getGame(String handline, int max) {
		// PokerStars Game #73347266323:  Omaha Pot Limit ($0.01/$0.02 USD) - 2012/01/05 16:12:04 ET
		// PokerStars Game #73076810536:  5 Card Draw No Limit (100/200) - 2011/12/31 14:45:08 ET
		// PokerStars Game #73112640557: Tournament #493078525, 2000+110 Omaha Pot Limit - Level I (10/20) - 2012/01/01 13:43:02 ET
		// PokerStars Game #73111358128:  Hold'em Pot Limit (100/200) - 2012/01/01 13:19:41 ET
		// PokerStars Zoom Hand #77405734487:  Omaha Pot Limit ($0.01/$0.02) - 2012/03/18 14:38:20 ET
		// PokerStars Hand #75934682486:  Mixed NLH/PLO (Hold'em No Limit, 100/200) - 2012/02/20 16:16:13 ET
		// PokerStars Game #64393043049:  5 Card Draw Pot Limit (5/10) - 2011/07/10 16:33:36 ET

		char mix = 0;
		if (handline.contains("Mixed")) {
			if (handline.contains("Mixed NLH/PLO")) {
				mix = Game.NLHE_PLO_MIX;
			} else {
				throw new RuntimeException("unknown mix type: " + handline);
			}
		}

		char type;
		if (handline.contains("Hold'em")) {
			type = Game.HE_TYPE;
		} else if (handline.contains("Omaha")) {
			type = Game.OM_TYPE;
		} else if (handline.contains("5 Card Draw")) {
			type = Game.FCD_TYPE;
		} else {
			throw new RuntimeException("unknown game " + handline);
		}

		char currency;
		if (handline.indexOf("$") >= 0) {
			currency = '$';
		} else if (handline.indexOf("€") >= 0) {
			currency = '€';
		} else {
			// assume play
			currency = Game.PLAY_CURRENCY;
			//throw new RuntimeException("unknown currency " + handline);
		}

		char limit;
		if (handline.contains("Pot Limit")) {
			limit = Game.POT_LIMIT;
		} else if (handline.contains("No Limit")) {
			limit = Game.NO_LIMIT;
		} else if (handline.contains("Limit")) {
			limit = Game.FIXED_LIMIT;
		} else {
			throw new RuntimeException("unknown limit " + handline);
		}

		char subtype = 0;
		if (handline.contains("Zoom")) {
			subtype = Game.ZOOM_SUBTYPE;
		}

		int sbStart = handline.indexOf("(");
		if (sbStart == -1) {
			throw new RuntimeException("could not get small blind from " + handline);
		}
		sbStart += 1;

		// if there is a comma, move after it
		// (Hold'em No Limit, 100/200)
		int c = handline.indexOf(",", sbStart);
		if (c > 0) {
			sbStart = c + 2;
		}

		int bbStart = handline.indexOf("/", sbStart);
		if (bbStart == -1) {
			throw new RuntimeException("could not get big blind from " + handline);
		}
		bbStart += 1;

		int sb = parseMoney(handline, sbStart);
		int bb = parseMoney(handline, bbStart);
		if (sb == 0 || bb == 0 || sb > bb) {
			throw new RuntimeException("could not get blinds from " + handline);
		}

		if (limit == Game.FIXED_LIMIT) {
			// fixed limit has big bet and small bet not blinds
			bb = sb;
			sb = sb / 2;
		}

		// find game, otherwise create it
		for (Game game : games) {
			if (game.currency == currency && game.type == type && game.limit == limit && game.max == max
					&& game.subtype == subtype && game.sb == sb && game.bb == bb && game.mix == mix) {
				return game;
			}
		}

		Game game = new Game();
		game.currency = currency;
		game.type = type;
		game.limit = limit;
		game.max = max;
		game.subtype = subtype;
		game.sb = sb;
		game.bb = bb;
		game.mix = mix;
		game.id = GameUtil.getGameId(game);
		games.add(game);

		System.out.println("created game " + game);
		return game;
	}

}
