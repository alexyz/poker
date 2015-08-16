
package pet.hp.impl;

import java.text.*;
import java.util.*;
import java.util.regex.*;

import pet.eq.*;
import pet.hp.*;

import static pet.hp.impl.ParseUtil.*;

/**
 * PokerStars hand parser - primarily for Omaha/Hold'em/5 Card Draw PL/NL games
 * but also tournaments and FL games.
 */
public class PSParser extends Parser2 {
	
	private static final TimeZone ET = TimeZone.getTimeZone("US/Eastern");
	
	// instance fields
	
	/** instance field for thread safety */
	private final DateFormat shortDateFormat = new SimpleDateFormat("yyyy/MM/dd");
	/** instance field for thread safety */
	private final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss zzz");
	/** hand game instance - can only determine game after two lines */
	protected Game game;
	/** is in summary phase */
	protected boolean summaryPhase;
	
	public PSParser() {
		//
	}
	
	@Override
	public boolean isHistoryFile (String name) {
		// TS - tournament summaries
		return name.startsWith("HH") && name.endsWith(".txt");
	}
	
	/**
	 * reset state for new hand
	 */
	@Override
	public void clear () {
		super.clear();
		summaryPhase = false;
		game = null;
	}
	
	/**
	 * parse next line from file if the line completes a hand, it is returned
	 */
	@Override
	public boolean parseLine (String line) {
		if (line.length() > 0 && line.charAt(0) == 0xfeff) {
			line = line.substring(1);
			println("skip bom");
		}
		
		int i = 0;
		line = line.trim();
		println(">>> " + line);
		
		if (line.length() == 0) {
			if (summaryPhase && hand != null) {
				finish();
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
			int cardsStart = nextToken(line, 0);
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
			// need to check game.hilo to see if this can be used
			hand.showdownNoLow = true;
			
		} else if (line.equals("The deck is reshuffled")) {
			println("deck reshuffled");
			// don't think of discarded cards as blockers...
			hand.reshuffleStreetIndex = (byte) currentStreetIndex();
			
		} else if (line.equals("Pair on board - a double bet is allowed")) {
			println("double bet allowed");
			
			// / ---------- ends with ----------
			
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
			assertObj(seat, "seat");
			
			if (hand.myseat == seat) {
				// get finish position and win amount
				int p = parseInt(line, i + 28);
				hand.tourn.pos = p;
				println("player finished " + p);
				
				int m = line.indexOf("received", i);
				if (m > 0) {
					int won = parseMoney(line, m + 9);
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
			assertObj(seat, "seat");
			
			hand.tourn.winner = name;
			if (hand.myseat == seat) {
				// get win amount
				hand.tourn.pos = 1;
				int m = line.indexOf("receives", i);
				int won = parseMoney(line, m + 9);
				hand.tourn.won = won;
				println("player won " + won);
				
			} else {
				println("won");
			}
			
		} else {
			fail("unknown line " + line);
		}
		
		return false;
	}
	
	private void parseCollect (String line, int a) {
		// olasz53 collected $1.42 from main pot
		// NightPred8or collected $1.41 from main pot
		// olasz53 collected $0.56 from side pot
		// NightPred8or collected $0.56 from side pot
		String name = line.substring(0, a);
		Seat seat = seatsMap.get(name);
		assertObj(seat, "seat");
		int amount = parseMoney(line, a + 11);
		seat.won += amount;
		
		// add the collect as a fake action so the action amounts sum to pot
		// size
		Action act = new Action(seat);
		act.amount = -amount;
		act.type = Action.Type.COLLECT;
		currentStreet().add(act);
		
		println("collected " + name + " " + amount);
	}
	
	private void parseTotal (final String line) {
		// Total pot $0.30 | Rake $0.01
		// Total pot $4.15 Main pot $2.83. Side pot $1.12. | Rake $0.20
		hand.pot = parseMoney(line, 10);
		int a = line.indexOf("Rake");
		hand.rake = parseMoney(line, a + 5);
		println("total " + hand.pot + " rake " + hand.rake);
	}
	
	private void parseUncall (final String line) {
		// Uncalled bet ($0.19) returned to Hokage_91
		int amountStart = line.indexOf("(") + 1;
		int amount = parseMoney(line, amountStart);
		int nameStart = line.indexOf("to") + 3;
		String name = line.substring(nameStart);
		Seat seat = seatsMap.get(name);
		assertObj(seat, "seat");
		// seat.uncalled = amount;
		seatPip(seat, -amount);
		
		// add the uncall as a fake action so the action amounts sum to pot size
		Action act = new Action(seat);
		act.amount = -amount;
		act.type = Action.Type.UNCALL;
		currentStreet().add(act);
		
		println("uncalled " + name + " " + amount);
	}
	
	private void parseDeal (final String line) {
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
		
		// Dealt to [AUT] BigPot [Qc]
		
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
			if (GameUtil.isDraw(hand.game.type)) {
				// hole cards can be changed in draw so store them all on
				// hand
				hand.addMyDrawCards(cards);
			}
			theseat.downCards = checkCards(theseat.downCards, getDownCards(hand.game.type, cards));
			theseat.upCards = checkCards(theseat.upCards, getUpCards(hand.game.type, cards));
			
		} else {
			// not us, all cards are up cards
			theseat.upCards = checkCards(theseat.upCards, cards);
		}
		
	}
	
	private void parseSeat (final String line) {
		int seatno = parseInt(line, 5);
		
		if (summaryPhase) {
			// Seat 1: 777KTO777 folded before Flop (didn't bet)
			// Seat 2: tawvx showed [4c 6h 7d 5h] and won ($0.44) with a
			// straight, Four to Eight
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
						seat.downCards = checkCards(seat.downCards, getDownCards(hand.game.type, cards));
						seat.upCards = checkCards(seat.upCards, getUpCards(hand.game.type, cards));
					}
				}
				println("seat " + seatno + " mucked " + Arrays.asList(cards));
				
			} else {
				println("seat summary");
			}
			
		} else {
			// Seat 2: tawvx ($2.96 in chips)
			// Seat 6: abs(EV) ($2.40 in chips)
			// Seat 5: OCTAVIAN 61 (2000 in chips) out of hand (moved from
			// another table into small blind)
			
			int col = line.indexOf(": ");
			int chEnd = line.indexOf(" in chips)");
			int chStart = line.lastIndexOf("(", chEnd);
			
			Seat seat = new Seat();
			seat.num = (byte) seatno;
			seat.name = StringCache.get(line.substring(col + 2, chStart - 1));
			seat.chips = parseMoney(line, chStart + 1);
			seatsMap.put(seat.name, seat);
			println("seat " + seat);
		}
	}
	
	/**
	 * Parse the hand line starting with PokerStars
	 */
	private void parseHand (final String line) {
		assert_ (hand == null, "finished last hand");
		
		// the hardest line to parse...
		// cut the date out, and remove all the punctuation
		int dateIndex = line.lastIndexOf("- ");
		assert_ (dateIndex > 0, "has date");
		
		String handline = strip(line.substring(0, dateIndex), "()#:,-");
		println("hand line: " + handline);
		String dateline = line.substring(dateIndex + 2);
		println("date line: " + dateline);
		
		Matcher m = PSHandRE.pat.matcher(handline);
		assert_ (m.matches(), "match hand exp");
		
		game = new Game();
		
		// sub type - currently just zoom
		// in future maybe turbo, matrix etc
		String zoom = m.group(PSHandRE.zoom);
		if (zoom != null && zoom.equals("Zoom")) {
			game.subtype |= Game.ZOOM_SUBTYPE;
		}
		
		Hand hand = new Hand();
		hand.id = Long.parseLong(m.group(PSHandRE.handid)) | Hand.PS_ROOM;
		
		// get all the tournament stuff if there is tourn id
		String tournids = m.group(PSHandRE.tournid);
		if (tournids != null) {
			game.currency = Game.TOURN_CURRENCY;
			// get the tournament id and instance
			long tournid = Long.parseLong(tournids) | Hand.PS_ROOM;
			
			String tournbuyins = m.group(PSHandRE.tbuyin);
			String tourncosts = m.group(PSHandRE.tcost);
			
			char tourncurrency = 0;
			int tournbuyin = 0, tourncost = 0;
			
			if (tournbuyins != null) {
				tourncurrency = parseCurrency(tournbuyins, 0);
				tournbuyin = parseMoney(tournbuyins, 0);
				tourncost = parseMoney(tourncosts, 0);
			}
			
			Tourn t = getHistory().getTourn(tournid, tourncurrency, tournbuyin, tourncost);
			
			println("tourn " + t);
			hand.tourn = t;
		}
		
		// mixed game type, if any
		String mixs = m.group(PSHandRE.mix);
		if (mixs != null) {
			game.mix = getMixType(mixs);
		}
		
		String gameStr = m.group(PSHandRE.game);
		game.type = parseGame(gameStr);
		
		String limits = m.group(PSHandRE.limit);
		game.limit = parseLimit(limits);
		
		String round = m.group(PSHandRE.tround);
		if (round != null) {
			int r = parseRoman(round, 0);
			hand.round = r;
		}
		
		String level = m.group(PSHandRE.tlevel);
		if (level != null) {
			int l = parseRoman(level, 0);
			hand.level = l;
		}
		
		String sbs = m.group(PSHandRE.sb);
		if (game.currency == 0) {
			// if hand isn't tournament, set cash game currency
			game.currency = parseCurrency(sbs, 0);
		}
		game.sb = parseMoney(sbs, 0);
		
		String bbs = m.group(PSHandRE.bb);
		game.bb = parseMoney(bbs, 0);
		if (game.sb == 0 || game.bb == 0 || game.sb >= game.bb) {
			throw new RuntimeException("invalid blinds " + game.sb + "/" + game.bb);
		}
		
		if (game.limit == Game.Limit.FL) {
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
			hand.date = hdatetime.getTime();
			if (hand.tourn != null && (hand.tourn.date == null || hand.tourn.date.after(hdatetime))) {
				// estimate tournament start date if it is not present
				// XXX the first hand will always have earliest date (unless a
				// TS was parsed first)
				hand.tourn.date = hdatetime;
			}
		} catch (Exception e) {
			throw new RuntimeException("could not parse date " + dateline, e);
		}
		
		// create first street
		newStreet();
		
		this.hand = hand;
		println("hand " + hand);
	}
	
	private void parseTable (final String line) {
		// Table 'Roehla IX' 9-max Seat #7 is the button
		// Table 'Sabauda VI' 6-max (Play Money) Seat #5 is the button
		// Table 'Honoria V' 6-max Seat #6 is the button
		// Table 'Mekbuda VIII' 2-max (Play Money) Seat #2 is the button
		// Table '493078525 1' 9-max Seat #1 is the button
		// Table 'bltable.1225797637.1225917089' 6-max
		// seat 1 is button if unspec
		int tableStart = line.indexOf("'");
		int tableEnd = line.indexOf("'", tableStart + 1);
		hand.tablename = StringCache.get(line.substring(tableStart + 1, tableEnd));
		println("table " + hand.tablename);
		
		// fix limit real money holdem games can be 10 player
		int maxStart = nextToken(line, tableEnd + 1);
		game.max = parseInt(line, maxStart);
		assert_(game.max > 0 && game.max <= 10, "max");
		println("max " + game.max);
		
		// get the definitive game instance
		game = getHistory().getGame(game);
		hand.game = game;
		println("game " + game);
		
		int d = line.indexOf("Seat");
		if (d > 0) {
			hand.button = (byte) Integer.parseInt(line.substring(d + 6, d + 7));
			
		} else if ((game.subtype & Game.ZOOM_SUBTYPE) != 0) {
			// assume button in seat one for zoom
			// probably wrong for zoom stud, but who plays that
			println("assume button in seat 1");
			hand.button = 1;
		}
	}
	
	private void parsePhase (final String line) {
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
		
		// don't need to parse cards as stars always summarises board at end
		
		int a = line.indexOf("***");
		int b = line.indexOf("***", a + 3);
		String name = line.substring(a + 4, b - 1);
		println("phase " + name);
		
		switch (name) {
			case "FLOP":
			case "TURN":
			case "RIVER":
			case "FIRST DRAW":
			case "SECOND DRAW":
			case "THIRD DRAW":
			case "4th STREET":
			case "5th STREET":
			case "6th STREET":
				pip();
				newStreet();
				println("new street index " + currentStreetIndex());
				break;
				
			case "SHOW DOWN":
				println("showdown");
				hand.showdown = true;
				break;
				
			case "DEALING HANDS":
			case "3rd STREET":
			case "HOLE CARDS":
			case "PRE-FLOP":
				println("ignore phase");
				break;
				
			case "SUMMARY":
				println("summary");
				// pip in case there is only one street
				pip();
				summaryPhase = true;
				break;
				
			default:
				fail("unknown phase: " + name);
		}
		
	}
	
	private void parseAction (final String line, final int i) {
		// Bumerang16: posts small blind $0.01
		String name = line.substring(0, i);
		Seat seat = seatsMap.get(name);
		assertObj(seat, "seat");
		
		int actStart = nextToken(line, i);
		int actEnd = skipToken(line, actStart);
		Action action = new Action(seat);
		String actString = line.substring(actStart, actEnd);
		Action.Type actByte = ParseUtil.parseAction(actString);
		action.type = actByte;
		boolean drawAct = false;
		
		switch (action.type) {
			case CHECK:
			case MUCK:
			case DOESNTSHOW:
				// NSavov: checks
				// scotty912: doesn't show hand
				// not sure what difference is between muck and doesn't show
				break;
			
			case FOLD: {
				// azacel77: folds
				// Ninjajundiai: folds [5d 5s]
				// tawvx: folds [2h Tc 7s 4h Js 2c]
				int handStart = line.indexOf("[", actEnd);
				if (handStart > 0) {
					String[] cards = parseCards(line, handStart);
					seat.downCards = checkCards(seat.downCards, getDownCards(hand.game.type, cards));
					seat.upCards = checkCards(seat.upCards, getUpCards(hand.game.type, cards));
				}
				break;
			}
			
			case BRINGSIN: {
				// trinitycubed: brings in for 3
				// i assume this is the same as calling
				int amountStart = line.indexOf("for", actEnd) + 4;
				int amount = parseMoney(line, amountStart);
				action.amount = amount;
				seatPip(seat, amount);
				break;
			}
			
			case CALL:
			case BET: {
				// Bumerang16: calls $0.01
				int amountStart = nextToken(line, actEnd);
				int amount = parseMoney(line, amountStart);
				action.amount = amount;
				seatPip(seat, amount);
				break;
			}
			
			case RAISE: {
				// bluff.tb: raises $0.05 to $0.07
				int amountStart = line.indexOf("to ", actEnd) + 3;
				// subtract what seat has already put in this round
				int amount = parseMoney(line, amountStart) - seatPip(seat);
				action.amount = amount;
				seatPip(seat, amount);
				break;
			}
			
			case POST: {
				// Bumerang16: posts small blind $0.01
				// pisti361: posts small & big blinds $0.03
				// Yury.Nik: posts big blind 50 and is all-in
				// Festo5811: posts the ante 5
				
				// small and big blinds always posted first due to position
				// though very occasionally the big blind may not be posted due
				// to button rule
				
				int blindStart = line.indexOf("blind", actEnd);
				if (blindStart == -1) {
					blindStart = line.indexOf("ante", actEnd);
				}
				int amountStart = nextToken(line, blindStart);
				action.amount = parseMoney(line, amountStart);
				
				if (line.indexOf(" small blind ", actEnd) > 0) {
					// posted sb can be smaller in tournaments
					assert_(action.amount <= hand.sb, "sm act <= sb");
					seat.smallblind = true;
					
					if (!sbposted) {
						println("small blind " + action.amount);
						seatPip(seat, action.amount);
						sbposted = true;
						
					} else {
						// dead small blind
						println("dead small blind " + action.amount);
						// doesn't count toward player pip
						anonPip(action.amount);
					}
					
				} else if (line.indexOf(" small & big blinds ", actEnd) > 0) {
					println("dead small and big blind " + action.amount);
					assert_(action.amount == hand.bb + hand.sb, "post bb + dead sb = hand bb + sb");
					seat.bigblind = true;
					seat.smallblind = true;
					
					// dead small blind doesn't count towards pip (but does
					// count towards pot)
					anonPip(hand.sb);
					seatPip(seat, hand.bb);
					
				} else if (line.indexOf(" big blind ", actEnd) > 0) {
					println("big blind " + action.amount);
					seat.bigblind = true;
					assert_(action.amount <= hand.bb, "post bb <= hand bb");
					seatPip(seat, action.amount);
					
				} else if (line.indexOf(" the ante ", actEnd) > 0) {
					// consider ante different to post
					action.type = Action.Type.ANTE;
					println("ante " + action.amount);
					assert_(action.amount < hand.sb, "ante < sb");
					
					anonPip(action.amount);
					
				} else {
					throw new RuntimeException("unknown post");
				}
				
				break;
			}
			
			case SHOW: {
				// bluff.tb: shows [Jc 8h Js Ad] (two pair, Aces and Kings)
				// tudy31: shows [7d Ad 4d Kd 8h Jh 3d] (Lo: 8,7,4,3,A)
				int handStart = nextToken(line, actEnd);
				String[] cards = parseCards(line, handStart);
				seat.downCards = checkCards(seat.downCards, getDownCards(hand.game.type, cards));
				seat.upCards = ParseUtil.checkCards(seat.upCards, getUpCards(hand.game.type, cards));
				break;
			}
			
			case DRAW: {
				// create new street before adding action (5cd only)
				drawAct = true;
				// tawvx: discards 1 card [Ah]
				// joven2010: discards 3 cards
				
				// st=>draw
				// 5cd: 0=>0
				// td/bg: 1=>0, 2=>1, 3=>2
				int gamedraw = GameUtil.getDraws(hand.game.type, 0);
				int draw = gamedraw == 1 ? 0 : currentStreetIndex() - 1;
				assert_(seat.drawn(draw) == 0, "seat drawn=zero: " + seat.drawn(draw));
				int discardsStart = nextToken(line, actEnd);
				seat.setDrawn(draw, (byte) parseInt(line, discardsStart));
				// the actual cards will be set in parseDeal
				break;
			}
			
			case STANDPAT: {
				// stands pat
				if (hand.myseat == seat) {
					// there is no deal so push previous hole cards here
					hand.addMyDrawCards(seat.downCards);
				}
				drawAct = true;
				println("stands");
				break;
			}
			
			case UNCALL:
			case ANTE:
			case COLLECT:
				// handled elsewhere
			default:
				throw new RuntimeException("unknown action: " + action.type);
		}
		
		if (hand.showdown) {
			// action after show down phase
			seat.showdown = true;
		}
		
		// any betting action can cause this
		if (line.endsWith("and is all-in")) {
			action.allin = true;
		}
		
		println("action " + action);
		
		if (drawAct && currentStreetIndex() == 0) {
			// there is no draw phase for 5 card draw, so pip and fake a new street
			println("new street for draw");
			pip();
			newStreet();
		}
		
		currentStreet().add(action);
	}
	
	/**
	 * return index of first character after token
	 */
	private static int skipToken (String line, int off) {
		while (off < line.length() && line.charAt(off) != ' ') {
			off++;
		}
		return off;
	}
	
	private static Game.Mix getMixType (String mixs) {
		switch (mixs) {
			case "Mixed NLH/PLO":
			case "Mixed PLH/PLO":
				return Game.Mix.HO;
			case "Triple Stud":
				return Game.Mix.TS;
				// dashes are removed
			case "8Game":
				return Game.Mix.EG;
			case "HORSE":
				return Game.Mix.HORSE;
			default:
				throw new RuntimeException("unknown mix type " + mixs);
		}
	}
	
}
