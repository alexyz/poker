package pet.hp.impl;

import java.text.*;
import java.util.*;
import java.util.regex.*;

import pet.eq.ArrayUtil;
import pet.hp.*;

/**
 * PokerStars hand parser - primarily for Omaha/Hold'em/5 Card Draw PL/NL games but
 * also tournaments and FL games.
 */
public class PSParser extends Parser {

	private static final TimeZone ET = TimeZone.getTimeZone("US/Eastern");
	private static final Map<String,Byte> actionMap = new HashMap<String,Byte>();

	private static class H {
		/** hand start pattern - no punctuation */
		// PokerStars Hand #84322807903:  Triple Draw 2-7 Lowball No Limit (5/10) - 2012/08/05 14:07:58 ET
		// PokerStars Hand #84393778794:  7 Card Stud Limit (20/40) - 2012/08/07 4:39:16 ET
		// PokerStars Hand #79306750218:  Triple Stud (7 Card Stud Limit, 4/8) - 2012/04/23 5:55:37 UTC [2012/04/23 1:55:37 ET]
		// PokerStars Hand #83338296941:  8-Game (Razz Limit, 100/200) - 2012/07/15 1:45:25 ET
		static final Pattern pat = Pattern.compile("PokerStars (?:(Zoom) )?(?:Hand|Game) (\\d+) "
				+ "(?:Tournament (\\d+) (?:(Freeroll)|(\\S+?)\\+(\\S+?)(?: (USD))?) )?" 
				+ "(?:(Mixed \\S+|Triple Stud|8Game) )?"
				+ "(.+?) "
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
		actionMap.put("brings", Action.BRINGSIN_TYPE);
	}
	
	// instance fields

	/** where to send parsed data */
	private final History history;
	/** instance field for thread safety */
	private final DateFormat shortDateFormat = new SimpleDateFormat("yyyy/MM/dd");
	/** instance field for thread safety */
	private final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss zzz");
	/** print everything to System.out */
	public boolean debug;
	
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
	/** hand game instance */
	private Game game;

	public PSParser(History history) {
		this.history = history;
	}

	private void println(String s) {
		debuglines.add(s);
		if (debug) {
			System.out.println(s);
		}
	}

	@Override
	public boolean isHistoryFile(String name) {
		// TS - tournament summaries
		return name.startsWith("HH") && name.endsWith(".txt") && !name.contains("Badugi");
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
		game = null;
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
	public boolean parseLine(String line) {
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
				history.addHand(hand);
				if (hand.tourn != null) {
					history.addTournPlayers(hand.tourn.id, seatsMap.keySet());
				}
				println("end of hand " + hand);
				
				clear();
				return true;				
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
			int cardsStart = ParseUtil.nextToken(line, 0);
			hand.board = checkCards(hand.board, parseCards(line, cardsStart));
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
			
		} else if (line.equals("The deck is reshuffled")) {
			// XXX HandStateUtil better not think discarded cards are blockers anymore...
			println("deck reshuffled");
			hand.reshuffleStreetIndex = (byte) (streets.size() - 1);
			
		} else if (line.equals("Pair on board - a double bet is allowed")) {
			println("double bet allowed");


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
		
		return false;
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
		
		// deals to others in stud
		// Dealt to tawvx [9d 5d Th]
		// Dealt to puddin pig [2d]
		// later...
		// Dealt to tawvx [9d 5d Th 2s 6d] [8s]
		// Dealt to gregory9876 [5c 6c 7d] [Qh]

		// get seat
		int cardsStart = line.indexOf("[");
		String name = line.substring(9, cardsStart - 1);
		Seat theseat = seatsMap.get(name);
		if (theseat == null) {
			throw new RuntimeException("could not find seat " + name);
		}
		
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
				case Game.FCD_TYPE:
				case Game.DSSD_TYPE:
				case Game.DSTD_TYPE:
					// hole cards can be changed in draw so store them all on hand
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

			// gregory9876 mucked [9s Ad 5c 6c 7d Qh Kd]
			int muckedStart = line.indexOf("mucked");
			if (muckedStart > 0) {
				// get opponent hand
				String[] cards = parseCards(line, muckedStart + 7);
				for (Seat seat : seatsMap.values()) {
					if (seat.num == seatno) {
						// get the hole cards and up cards
						seat.finalHoleCards = checkCards(seat.finalHoleCards, getHoleCards(hand.game.type, cards));
						seat.finalUpCards = checkCards(seat.finalUpCards, getUpCards(hand.game.type, cards));
					}
				}
				println("seat " + seatno + " mucked " + Arrays.asList(cards));

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
			seat.name = history.getString(line.substring(col + 2, chStart - 1));
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
		
		Matcher m = H.pat.matcher(handline);
		if (!m.matches()) {
			throw new RuntimeException("could not match first line");
		}
		
		game = new Game();

		// sub type - currently just zoom
		// in future maybe turbo, matrix etc
		String zoom = m.group(H.zoom);
		if (zoom != null && zoom.equals("Zoom")) {
			game.subtype |= Game.ZOOM_SUBTYPE;
		}
		
		long hid = Long.parseLong(m.group(H.handid));
		Hand hand = new Hand(hid);
		
		// get all the tournament stuff if there is tourn id
		String tournids = m.group(H.tournid);
		if (tournids != null) {
			game.currency = Game.TOURN_CURRENCY;
			// get the tournament id and instance
			long tournid = Long.parseLong(tournids);
			
			String tournbuyins = m.group(H.tbuyin);
			String tourncosts = m.group(H.tcost);
			
			char tourncurrency = 0;
			int tournbuyin = 0, tourncost = 0;
			
			if (tournbuyins != null) {
				tourncurrency = parseCurrency(tournbuyins, 0);
				tournbuyin = ParseUtil.parseMoney(tournbuyins, 0);
				tourncost = ParseUtil.parseMoney(tourncosts, 0);
			}
			
			Tourn t = history.getTourn(tournid, tourncurrency, tournbuyin, tourncost);
			
			println("tourn " + t);
			hand.tourn = t;
		}
		
		// mixed game type, if any
		String mixs = m.group(H.mix);
		if (mixs != null) {
			if (mixs.equals("Mixed NLH/PLO") || mixs.equals("Mixed PLH/PLO")) {
				game.mix = Game.HE_OM_MIX;
			} else if (mixs.equals("Triple Stud")) {
				game.mix = Game.TRIPSTUD_MIX;
			} else if (mixs.equals("8Game")) {
				game.mix = Game.EIGHT_MIX;
			} else {
				throw new RuntimeException("unknown mix type " + mixs);
			}
		}
		
		String gameStr = m.group(H.game);
		if (gameStr.equals("Hold'em")) {
			game.type = Game.HE_TYPE;
		} else if (gameStr.equals("Omaha Hi/Lo")) {
			game.type = Game.OMHL_TYPE;
		} else if (gameStr.equals("Omaha")) {
			game.type = Game.OM_TYPE;
		} else if (gameStr.equals("5 Card Draw")) {
			game.type = Game.FCD_TYPE;
		} else if (gameStr.equals("Triple Draw 27 Lowball")) {
			game.type = Game.DSTD_TYPE;
		} else if (gameStr.equals("Razz")) {
			game.type = Game.RAZZ_TYPE;
		} else if (gameStr.equals("7 Card Stud")) {
			game.type = Game.STUD_TYPE;
		} else if (gameStr.equals("7 Card Stud Hi/Lo")) {
			game.type = Game.STUDHL_TYPE;
		} else if (gameStr.equals("Single Draw 27 Lowball")) {
			game.type = Game.DSSD_TYPE;
		} else {
			throw new RuntimeException("unknown game " + gameStr);
		}

		String limits = m.group(H.limit);
		if (limits.equals("Pot Limit")) {
			game.limit = Game.POT_LIMIT;
		} else if (limits.equals("No Limit")) {
			game.limit = Game.NO_LIMIT;
		} else if (limits.equals("Limit")) {
			game.limit = Game.FIXED_LIMIT;
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
		if (game.currency == 0) {
			// if hand isn't tournament, set cash game currency
			game.currency = parseCurrency(sbs, 0);
		}
		game.sb = ParseUtil.parseMoney(sbs, 0);
		
		String bbs = m.group(H.bb);
		game.bb = ParseUtil.parseMoney(bbs, 0);
		if (game.sb == 0 || game.bb == 0 || game.sb >= game.bb) {
			throw new RuntimeException("invalid blinds " + game.sb + "/" + game.bb);
		}
		
		if (game.limit == Game.FIXED_LIMIT) {
			// fixed limit has big bet and small bet not blinds
			game.bb = game.sb;
			game.sb = game.sb / 2;
		}
		
		hand.sb = game.sb;
		hand.bb = game.bb;
		
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
				// XXX the first hand will always have earliest date (unless a TS was parsed first)
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
		hand.tablename = history.getString(line.substring(tableStart + 1, tableEnd));
		println("table " + hand.tablename);

		// fix limit real money holdem games can be 10 player
		int maxStart = ParseUtil.nextToken(line, tableEnd + 1);
		game.max = ParseUtil.parseInt(line, maxStart);
		if (game.max == 0 || game.max > 10) {
			throw new RuntimeException("invalid max " + line);
		}
		println("max " + game.max);
		
		// get the definitive game instance
		game = history.getGame(game);
		hand.game = game;
		println("game " + game);
		
		int d = line.indexOf("Seat");
		if (d > 0) {
			hand.button = (byte) Integer.parseInt(line.substring(d + 6, d + 7));

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
			case Game.DSSD_TYPE:
				if (name.equals("DEALING HANDS")) {
					ignoreStreet = true;
				}
				// have to manually make new street in first draw/stand pat action
				break;
				
			case Game.DSTD_TYPE:
				//*** DEALING HANDS ***
				//*** FIRST DRAW ***
				//*** SECOND DRAW ***
				//*** THIRD DRAW ***
				if (name.equals("DEALING HANDS")) {
					ignoreStreet = true;
				} else if (name.equals("FIRST DRAW") || name.equals("SECOND DRAW") || name.equals("THIRD DRAW")) {
					newStreet = true;
				}
				break;
				
			case Game.STUD_TYPE:
			case Game.STUDHL_TYPE:
			case Game.RAZZ_TYPE:
				// *** 3rd STREET ***
				// *** 4th STREET ***
				// *** 5th STREET ***
				// *** 6th STREET ***
				// *** RIVER ***
				// FIXME river could be followed by community card, like holdem
				if (name.equals("3rd STREET")) {
					ignoreStreet = true;
				} else if (name.equals("4th STREET") || name.equals("5th STREET") || name.equals("6th STREET") || name.equals("RIVER")) {
					newStreet = true;
				}
				break;
				
			default: 
				throw new RuntimeException("unknown game type " + hand.game.type);
		}

		if (newStreet) {
			pip();
			streets.add(new ArrayList<Action>());
			println("new street index " + (streets.size() - 1));

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

	private void parseAction(final String line, final int i) {
		// Bumerang16: posts small blind $0.01
		String name = line.substring(0, i);
		Seat seat = seatsMap.get(name);
		if (seat == null) {
			throw new RuntimeException("unknown player: " + line);
		}

		int actStart = ParseUtil.nextToken(line, i);
		int actEnd = endToken(line, actStart);
		Action action = new Action(seat);
		String actString = line.substring(actStart, actEnd);
		Byte actByte = actionMap.get(actString);
		if (actByte == null) {
			throw new RuntimeException("unknown action " + actString);
		}
		action.type = actByte;
		boolean drawAct = false;

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
				// tawvx: folds [2h Tc 7s 4h Js 2c]
				int handStart = line.indexOf("[");
				if (handStart > 0) {
					String[] cards = parseCards(line, handStart);
					seat.finalHoleCards = checkCards(seat.finalHoleCards, getHoleCards(hand.game.type, cards));
					seat.finalUpCards = checkCards(seat.finalUpCards, getUpCards(hand.game.type, cards));
				}
				break;
			}
			
			case Action.BRINGSIN_TYPE: {
				// trinitycubed: brings in for 3
				// i assume this is the same as calling
				int amountStart = line.indexOf("for", actEnd) + 4;
				int amount = ParseUtil.parseMoney(line, amountStart);
				action.amount = amount;
				seatPip[seat.num] += amount;
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
				// tudy31: shows [7d Ad 4d Kd 8h Jh 3d] (Lo: 8,7,4,3,A)
				int handStart = ParseUtil.nextToken(line, actEnd);
				String[] cards = parseCards(line, handStart);
				seat.finalHoleCards = checkCards(seat.finalHoleCards, getHoleCards(hand.game.type, cards));
				seat.finalUpCards = checkCards(seat.finalUpCards, getUpCards(hand.game.type, cards));
				break;
			}

			case Action.DRAW_TYPE: {
				drawAct = true;
				// tawvx: discards 1 card [Ah]
				// joven2010: discards 3 cards
				
				// five card draw: always draw 0
				// street 0 (size 1): no draw
				// street 1 (size 2): draw 0, etc
				int draw = hand.game.type == Game.DSTD_TYPE ? streets.size() - 2 : 0;
				if (seat.drawn(draw) > 0) {
					throw new RuntimeException("already discarded " + seat.drawn(draw));
				}
				int discardsStart = ParseUtil.nextToken(line, actEnd);
				seat.setDrawn(draw, (byte) ParseUtil.parseInt(line, discardsStart));
				// the actual cards will be set in parseDeal
				break;
			}

			case Action.STANDPAT_TYPE: {
				// stands pat
				if (hand.myseat == seat) {
					// there is no deal so push previous hole cards here
					hand.addMyDrawCards(seat.finalHoleCards);
				}
				drawAct = true;
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

		if (drawAct && streets.size() == 1) {
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
	 * get the cards
	 */
	private String[] parseCards(String line, int off) {
		// [Jc 8h Js Ad]
		if (line.charAt(off) == '[') {
			int end = line.indexOf("]", off);
			int num = (end - off) / 3;
			String[] cards = new String[num];
			for (int n = 0; n < num; n++) {
				int a = off + 1 + (n * 3);
				// could validate this, but pretty unlikely to be invalid
				cards[n] = history.getString(line.substring(a, a+2));
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

	/** get the up cards from the array depending on the game type */
	private static String[] getUpCards(final int gametype, final String[] cards) {
		switch (gametype) {
			case Game.STUD_TYPE:
			case Game.STUDHL_TYPE:
			case Game.RAZZ_TYPE:
				// first two cards and last are hole, others are pub
				// cards length is 3,4,5,6,7
				return Arrays.copyOfRange(cards, 2, Math.min(cards.length, 6));
			default:
				// none are up cards
				return null;
		}
	}
	
	/** check cards haven't got shorter */
	private static String[] checkCards(String[] oldCards, String[] cards) {
		if (oldCards != null && (cards == null || oldCards.length > cards.length)) {
			throw new RuntimeException("old: " + Arrays.toString(oldCards) + " new: " + Arrays.toString(cards));
		}
		return cards;
	}
	
	/** get the hole cards from the array depending on game type */
	private static String[] getHoleCards(final int gametype, final String[] cards) {
		switch (gametype) {
			case Game.STUD_TYPE:
			case Game.STUDHL_TYPE:
			case Game.RAZZ_TYPE:
				// first two cards and last are hole, others are pub
				// cards length is 3,4,5,6,7
				if (cards.length < 7) {
					return new String[] { cards[0], cards[1] };
				} else {
					return new String[] { cards[0], cards[1], cards[6] }; 
				}
			default:
				// all cards are hole cards
				return cards;
		}
	}

}
