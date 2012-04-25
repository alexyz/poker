package pet.hp.impl;

import java.text.*;
import java.util.*;
import java.util.regex.*;

import pet.eq.ArrayUtil;
import pet.eq.Poker;
import pet.hp.*;

/**
 * PokerStars hand parser - primarily for Omaha/Hold'em/5 Card Draw PL/NL games but
 * also tournaments and FL games. 
 */
public class PSParser extends Parser {

	private static final TimeZone ET = TimeZone.getTimeZone("US/Eastern");
	private static final DateFormat shortDateFormat = new SimpleDateFormat("yyyy/MM/dd");
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss zzz");
	private static final Map<String,Byte> actionMap = new HashMap<String,Byte>();

	private static class H {
		/** hand start pattern */
		static final Pattern p = Pattern.compile("PokerStars (?:(Zoom) )?(?:Hand|Game) (\\d+) "
				+ "(?:Tournament (\\d+) (?:(Freeroll)|(\\S+?)\\+(\\S+?)(?: (USD))?) )?" 
				+ "(?:(Mixed \\S+) )?"
				+ "(Hold'em|Omaha|Omaha Hi/Lo|5 Card Draw) " 
				+ "(No Limit|Pot Limit|Limit) "
				+ "(?:(?:Match Round (\\w+) )?(?:Level (\\w+)) )?" 
				+ "(\\S+?)/(\\S+?)(?: (USD))?");
		/** hand pattern capturing group constants */
		static final int zoom = 1, handid = 2, tournid = 3, freeroll = 4, tbuyin = 5, tcost = 6, tcur = 7, mix = 8, game = 9,
				limit = 10, tround = 11, tlevel = 12, sb = 13, bb = 14, blindcur = 15;
	}
	
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
	
	// instance fields

	/** print everything to System.out */
	public boolean debug;
	
	// would these be better in separate object for e.g. multithreaded parsing?
	/** string cache to avoid multiple instances of same string */
	private final Map<String,String> cache = new HashMap<String,String>();
	/** game instances */
	private final List<Game> games = new ArrayList<Game>();
	/** tournament instances */
	private final Map<Long,Tourn> tourns = new TreeMap<Long,Tourn>();

	// stuff for current hand, cleared on clear()

	/** map of player name to seat for current hand */
	private final Map<String,Seat> seatsMap = new TreeMap<String,Seat>();
	/** array of seat num to seat pip for this street. seat numbers are 1-10 */
	private final int[] seatPip = new int[11];
	/** running pot - updated when pip() is called */
	private int pot;
	/** streets of action for current hand */
	private final List<List<Action>> streets = new ArrayList<List<Action>>();
	/** current hand */
	private Hand hand;
	/** hand reached showdown */
	private boolean showdown;
	/** is in summary phase */
	private boolean summaryPhase;
	/** debug output in case of parse error */
	private final List<String> debuglines = new ArrayList<String>();
	/** has live sb been posted (others are dead) */
	private boolean sbposted;

	public PSParser() {
		for (String c : Poker.FULL_DECK) {
			cache(c);
		}
	}

	private void println(String s) {
		debuglines.add(s);
		if (debug) {
			System.out.println(s);
		}
	}

	@Override
	public boolean isHistoryFile(String name) {
		// TODO TS - tournament summaries
		return name.startsWith("HH") && name.endsWith(".txt");
	}

	/**
	 * reset state for new hand
	 */
	@Override
	public void clear() {
		showdown = false;
		summaryPhase = false;
		seatsMap.clear();
		streets.clear();
		Arrays.fill(seatPip, 0);
		pot = 0;
		hand = null;
		debuglines.clear();
		sbposted = false;
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

		int i = 0;
		line = line.trim();
		println(">>> " + line);

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
				ret = hand;
				clear();
			}

		} else if (line.startsWith("PokerStars ")) {
			parseHand(line);

		} else if (line.startsWith("Betting is capped")) {
			println("capped");

		} else if (line.startsWith("Table ")) {
			parseTable(line);

		} else if (line.startsWith("Seat ")) {
			parseSeat(line);

		} else if (line.startsWith("*** ")) {
			parsePhase(line);

		} else if (line.startsWith("Board ")) {
			// Board [6d 3s Qc 8s 5d]
			int a = ParseUtil.nextToken(line, 0);
			hand.board = parseHand(line, a);
			println("board " + Arrays.asList(hand.board));

		} else if (line.startsWith("Dealt to ")) {
			parseDeal(line);

		} else if (line.startsWith("Uncalled ")) {
			parseUncall(line);

		} else if (line.startsWith("Total pot ")) {
			parseTotal(line);


			// ------ equals ------------


		} else if (line.equals("No low hand qualified")) {
			println("no low");


			/// ---------- ends with ----------


		} else if (line.endsWith(" sits out")) {
			// h_fa: sits out 
			println("sit out");

		} else if (line.endsWith(" is sitting out")) {
			// scotty912: is sitting out 
			println("sitting out");

		} else if (line.endsWith(" has timed out")) {
			// Festo5811 has timed out
			println("timed out");

		} else if (line.endsWith(" leaves the table")) {
			// kuca444 leaves the table
			println("leaves");

		} else if (line.endsWith(" is connected")) {
			println("connected");

		} else if (line.endsWith(" is disconnected")) {
			println("connected");

		} else if (line.endsWith(" has timed out while disconnected")) {
			println("timed out");

		} else if (line.endsWith(" has timed out while being disconnected")) {
			println("timed out");

		} else if (line.endsWith(" was removed from the table for failing to post")) {
			println("kicked");

		} else if (line.endsWith(" will be allowed to play after the button")) {
			println("play after");
			
		} else if (line.endsWith(" has returned")) {
			// Flawless Gem has returned
			println("he's back");


			// -------- contains ---------


		} else if (line.contains(" said, ")) {
			// tawvx said, "it's not a race"
			println("talk");

		} else if (line.contains(" joins the table at seat ")) {
			// scotty912 joins the table at seat #6 
			println("joins");

		} else if ((i = line.indexOf(" collected ")) > 0) {
			parseCollect(line, i);

		} else if ((i = line.indexOf(": ")) > 0) {
			parseAction(line, i);
			
		} else if ((i = line.indexOf(" finished the tournament in ")) > 0) {
			// jr_uemura finished the tournament in 2nd place
			// tawvx finished the tournament in 2nd place and received $2.77.
			String name = line.substring(0, i);
			Seat seat = seatsMap.get(name);
			if (seat == null) {
				throw new RuntimeException("could not get seat " + name);
			}
			
			if (hand.myseat == seat) {
				// get finish position and win amount
				int p = ParseUtil.parseInt(line, i + 28);
				hand.tourn.pos = p;
				println("player finished " + p);
				
				int m = line.indexOf("received", i);
				if (m > 0) {
					int won = ParseUtil.parseMoney(line, m + 9);
					hand.tourn.won = won;
					println("player won " + won);
				}
				
			} else {
				println("finished");
			}
			
		} else if ((i = line.indexOf(" wins the tournament and receives ")) > 0) {
			// tawvx wins the tournament and receives $2.76 - congratulations!
			String name = line.substring(0, i);
			Seat seat = seatsMap.get(name);
			if (seat == null) {
				throw new RuntimeException("could not get seat " + name);
			}
			
			hand.tourn.winner = name;
			if (hand.myseat == seat) {
				// get win amount
				hand.tourn.pos = 1;
				int m = line.indexOf("receives", i);
				int won = ParseUtil.parseMoney(line, m + 9);
				hand.tourn.won = won;
				println("player won " + won);
				
			} else {
				println("won");
			}

		} else {
			println("unknown line: " + line);
			throw new RuntimeException("unknown line " + line);
		}

		return ret;
	}

	private void parseCollect(String line, int a) {
		// olasz53 collected $1.42 from main pot
		// NightPred8or collected $1.41 from main pot
		// olasz53 collected $0.56 from side pot
		// NightPred8or collected $0.56 from side pot
		String name = line.substring(0, a);
		Seat seat = seatsMap.get(name);
		if (seat == null) {
			throw new RuntimeException("could not find seat " + name);
		}
		int amount = ParseUtil.parseMoney(line, a + 11);
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
		hand.pot = ParseUtil.parseMoney(line, 10);
		if (hand.pot != pot) {
			throw new RuntimeException("total pot " + hand.pot + " not equal to running pot " + pot);
		}
		int a = line.indexOf("Rake");
		hand.rake = ParseUtil.parseMoney(line, a + 5);

		// validate pot size
		// TODO remove these
		int won = 0;
		int lost = 0;
		for (Seat seat : seatsMap.values()) {
			won += seat.won;
			lost += seat.pip;
		}
		if (won != (hand.pot - hand.rake)) {
			throw new RuntimeException("pot " + pot + " not equal to total won " + won + " - rake " + hand.rake);
		}
		if (won != (lost - hand.rake + hand.antes)) {
			throw new RuntimeException("won " + won + " not equal to lost " + lost);
		}
		int asum = hand.antes - hand.rake;
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
		int amount = ParseUtil.parseMoney(line, amountStart);
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
		int seatno = ParseUtil.parseInt(line, 5);

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
			// Seat 5: OCTAVIAN 61 (2000 in chips) out of hand (moved from another table into small blind)
			
			int col = line.indexOf(": ");
			int chEnd = line.indexOf(" in chips)");
			int chStart = line.lastIndexOf("(", chEnd);

			Seat seat = new Seat();
			seat.num = (byte) seatno;
			seat.name = cache(line.substring(col + 2, chStart - 1));
			seat.chips = ParseUtil.parseMoney(line, chStart + 1);
			seatsMap.put(seat.name, seat);
			//seatsList.add(seat);
			println("seat " + seat);
		}
	}

	/**
	 * Parse the hand line starting with PokerStars
	 */
	private void parseHand(final String line) {
		if (hand != null) {
			throw new RuntimeException("did not not finish parsing last hand");
		}
		
		// the hardest line to parse...
		// cut the date out, and remove all the punctuation
		int dateIndex = line.lastIndexOf("- ");
		if (dateIndex == -1) {
			throw new RuntimeException("no date");
		}
		
		String handline = ParseUtil.strip(line.substring(0, dateIndex), "()#:,-");
		println("hand line: " + handline);
		String dateline = line.substring(dateIndex + 2);
		println("date line: " + dateline);
		
		Matcher m = H.p.matcher(handline);
		if (!m.matches()) {
			throw new RuntimeException("could not match first line");
		}

		// sub type - zoom or 0
		char subtype = 0;
		String zoom = m.group(H.zoom);
		if (zoom != null && zoom.equals("Zoom")) {
			subtype = Game.ZOOM_SUBTYPE;
		}
		
		long hid = Long.parseLong(m.group(H.handid));
		Hand hand = new Hand(hid);
		
		// hand currency (possibly tournament chips)
		char currency = 0;
		
		// get all the tournament stuff if there is tourn id
		String tids = m.group(H.tournid);
		if (tids != null) {
			currency = Game.TOURN_CURRENCY;
			// get the tournament id and instance
			long tid = Long.parseLong(tids);
			Tourn t = getTourn(tid);
			
			String tbuyins = m.group(H.tbuyin);
			if (tbuyins != null) {
				// not a freeroll
				char tcur = parseCurrency(tbuyins, 0);
				if (t.currency != 0 && t.currency != tcur) {
					throw new RuntimeException("invalid cur");
				}
				t.currency = tcur;
				
				int tbuyin = ParseUtil.parseMoney(tbuyins, 0);
				if (t.buyin > 0 && t.buyin != tbuyin) {
					throw new RuntimeException("invalid buy in");
				}
				t.buyin = tbuyin;
				
				String tcosts = m.group(H.tcost);
				int tcost = ParseUtil.parseMoney(tcosts, 0);
				if (t.cost > 0 && t.cost != tcost) {
					throw new RuntimeException("invalid cost");
				}
				t.cost = tcost;
			}
			
			println("tourn " + t);
			hand.tourn = t;
		}
		
		// mixed game type, if any
		char mix = 0;
		String mixs = m.group(H.mix);
		if (mixs != null) {
			if (mixs.equals("Mixed NLH/PLO") || mixs.equals("Mixed PLH/PLO")) {
				mix = Game.HE_OM_MIX;
			} else {
				throw new RuntimeException("unknown mix type " + mixs);
			}
		}
		
		String games = m.group(H.game);
		char type;
		if (games.equals("Hold'em")) {
			type = Game.HE_TYPE;
		} else if (games.equals("Omaha Hi/Lo")) {
			type = Game.OMHL_TYPE;
		} else if (games.equals("Omaha")) {
			type = Game.OM_TYPE;
		} else if (games.equals("5 Card Draw")) {
			type = Game.FCD_TYPE;
		} else {
			throw new RuntimeException("unknown game");
		}

		String limits = m.group(H.limit);
		char limit;
		if (limits.equals("Pot Limit")) {
			limit = Game.POT_LIMIT;
		} else if (limits.equals("No Limit")) {
			limit = Game.NO_LIMIT;
		} else if (limits.equals("Limit")) {
			limit = Game.FIXED_LIMIT;
		} else {
			throw new RuntimeException("unknown limit");
		}

		String round = m.group(H.tround);
		if (round != null) {
			int r = ParseUtil.parseRoman(round, 0);
			hand.round = r;
		}

		String level = m.group(H.tlevel);
		if (level != null) {
			int l = ParseUtil.parseRoman(level, 0);
			hand.level = l;
		}
		
		String sbs = m.group(H.sb);
		if (currency == 0) {
			// if hand isn't tournament, set cash game currency
			currency = parseCurrency(sbs, 0);
		}
		int sb = ParseUtil.parseMoney(sbs, 0);
		
		String bbs = m.group(H.bb);
		int bb = ParseUtil.parseMoney(bbs, 0);
		if (sb == 0 || bb == 0 || sb >= bb) {
			throw new RuntimeException("invalid blinds " + sb + "/" + bb);
		}
		
		if (limit == Game.FIXED_LIMIT) {
			// fixed limit has big bet and small bet not blinds
			bb = sb;
			sb = sb / 2;
		}
		
		hand.sb = sb;
		hand.bb = bb;
		
		// get the game instance
		Game game = getGame(currency, mix, type, subtype, limit, sb, bb);
		hand.game = game;
		println("game " + game);
		
		// hand date
		if (dateline.contains("[")) {
			// 2012/04/24 18:21:16 UTC [2012/04/24 14:21:16 ET]
			dateline = dateline.substring(dateline.indexOf("[") + 1, dateline.indexOf("]"));
		}
		
		try {
			// 2011/12/31 14:45:08 ET
			// 2012/04/11 10:41:17 ET
			// ET can mean either EDT (summer dst) or EST (winter)
			// FIXME this is probably parsed as local time zone so could be
			// wrong by a few hours each year
			Date hdate = shortDateFormat.parse(dateline.substring(0, dateline.indexOf(" ")));
			boolean dst = ET.inDaylightTime(hdate);
			Date hdatetime = dateFormat.parse(dateline.replace("ET", dst ? "EDT" : "EST"));
			hand.date = hdatetime;
			if (hand.tourn != null && (hand.tourn.date == null || hand.tourn.date.after(hdatetime))) {
				// estimate tournament start date if it is not present
				hand.tourn.date = hdatetime;
			}
		} catch (Exception e) {
			throw new RuntimeException("could not parse date " + dateline, e);
		}

		// create first street
		streets.add(new ArrayList<Action>());
		
		this.hand = hand;
		println("hand " + hand);
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
		int maxStart = ParseUtil.nextToken(line, tableEnd + 1);
		int max = ParseUtil.parseInt(line, maxStart);
		if (max == 0 || max > 10) {
			throw new RuntimeException("invalid max " + line);
		}
		hand.max = max;
		println("max " + max);
		
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
		boolean newStreet = false;
		boolean ignoreStreet = false;

		switch (hand.game.type) {
			case Game.HE_TYPE:
			case Game.OM_TYPE:
			case Game.OMHL_TYPE:
				if (name.equals("FLOP") || name.equals("TURN") || name.equals("RIVER")) {
					newStreet = true;
				} else if (name.equals("HOLE CARDS")) {
					ignoreStreet = true;
				}
				break;
			case Game.FCD_TYPE:
				if (name.equals("DEALING HANDS")) {
					ignoreStreet = true;
				}
				break;
			default: 
				throw new RuntimeException("unknown game type " + hand.game.type);
		}

		if (newStreet) {
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

		} else if (!ignoreStreet) {
			throw new RuntimeException("unknown phase " + name);
		}
	}

	private void parseAction(final String line, int i) {
		// Bumerang16: posts small blind $0.01
		String name = line.substring(0, i);
		Seat seat = seatsMap.get(name);
		if (seat == null) {
			throw new RuntimeException("unknown player: " + line);
		}

		int actStart = ParseUtil.nextToken(line, i);
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
				// not sure what difference is between muck and doesn't show
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
				int amountStart = ParseUtil.nextToken(line, actEnd);
				int amount = ParseUtil.parseMoney(line, amountStart);
				action.amount = amount;
				seatPip[seat.num] += amount;
				break;
			}

			case Action.RAISE_TYPE: {
				// bluff.tb: raises $0.05 to $0.07
				int amountStart = line.indexOf("to ", actEnd) + 3;
				// subtract what seat has already put in this round
				int amount = ParseUtil.parseMoney(line, amountStart) - seatPip[seat.num];
				action.amount = amount;
				seatPip[seat.num] += amount;
				break;
			}

			case Action.POST_TYPE: {
				// Bumerang16: posts small blind $0.01
				// pisti361: posts small & big blinds $0.03
				// Yury.Nik: posts big blind 50 and is all-in
				// Festo5811: posts the ante 5
				
				// small and big blinds always posted first due to position
				// though very occasionally the big blind may not be posted due to button rule

				int blindStart = line.indexOf("blind", actEnd);
				if (blindStart == -1) {
					blindStart = line.indexOf("ante");
				}
				int amountStart = ParseUtil.nextToken(line, blindStart);
				int amount = ParseUtil.parseMoney(line, amountStart);

				if (line.indexOf(" small blind ", actEnd) > 0) {
					if (amount > hand.sb) {
						// posted sb can be smaller in tournaments
						throw new RuntimeException("invalid small blind");
					}
					if (!sbposted) {
						println("small blind " + amount);
						sbposted = true;

					} else {
						// dead small blind
						println("dead small blind " + amount);
						hand.antes += amount;
						// doesn't count toward player pip
						pot += amount;
						amount = 0;
					}
					seat.smallblind = true;

				} else if (line.indexOf(" small & big blinds ", actEnd) > 0) {
					println("dead small and big blind " + amount);
					if (amount != hand.bb + hand.sb) {
						throw new RuntimeException("invalid small and big blind");
					}
					// dead small blind doesn't count towards pip (but does count towards pot)
					hand.antes += hand.sb;
					pot += hand.sb;
					amount -= hand.sb;
					seat.bigblind = true;
					seat.smallblind = true;

				} else if (line.indexOf(" big blind ", actEnd) > 0) {
					println("big blind " + amount);
					if (amount > hand.bb) {
						throw new RuntimeException("invalid big blind");
					}
					seat.bigblind = true;
					
				} else if (line.indexOf(" the ante ") > 0) {
					println("ante " + amount);
					if (amount >= hand.sb) {
						throw new RuntimeException("invalid ante");
					}
					hand.antes += amount;
					pot += amount;
					amount = 0;
					
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
				int handStart = ParseUtil.nextToken(line, actEnd);
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
				int discardsStart = ParseUtil.nextToken(line, actEnd);
				seat.discards = (byte) ParseUtil.parseInt(line, discardsStart);
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
	 * put in pot - update running pot with seat pips
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

	/**
	 * get cached string instance
	 */
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

	/**
	 * get the cards
	 */
	private String[] parseHand(String line, int off) {
		// [Jc 8h Js Ad]
		if (line.charAt(off) == '[') {
			int end = line.indexOf("]", off);
			int num = (end - off) / 3;
			String[] cards = new String[num];
			for (int n = 0; n < num; n++) {
				int a = off + 1 + (n * 3);
				// could validate this, but pretty unlikely to be invalid
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
	 * get the currency symbol or play currency symbol if there is no symbol
	 */
	private static char parseCurrency(String line, int off) {
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
	private Game getGame(char currency, char mix, char type, char subtype, char limit, int sb, int bb) { 

		if (currency == Game.TOURN_CURRENCY) {
			// don't store blinds for tournament hands as they are variable
			sb = 0;
			bb = 0;
		}
		
		// find game, otherwise create it
		for (Game game : games) {
			if (game.currency == currency && game.type == type && game.limit == limit
					&& game.subtype == subtype && game.sb == sb && game.bb == bb && game.mix == mix) {
				return game;
			}
		}

		Game game = new Game();
		game.currency = currency;
		game.type = type;
		game.limit = limit;
		game.subtype = subtype;
		game.sb = sb;
		game.bb = bb;
		game.mix = mix;
		game.id = GameUtil.getGameId(game);
		games.add(game);

		System.out.println("created game " + game);
		return game;
	}

	/**
	 * get tournament instance, possibly creating it
	 */
	private Tourn getTourn(long id) {
		Tourn t = tourns.get(id);
		if (t == null) {
			tourns.put(id, t = new Tourn(id));
		}
		return t;
	}

}
