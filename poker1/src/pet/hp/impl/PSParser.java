package pet.hp.impl;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import pet.hp.*;

/**
 * PokerStars hand parser
 */
public class PSParser extends Parser implements Serializable {

	private static final long serialVersionUID = 1;
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss zzz");

	/** string cache to avoid multiple instances of same string */
	private final Map<String,String> cache = new HashMap<String,String>();
	/** list of completed hands */
	private final List<Hand> hands = new ArrayList<Hand>();

	/** map of player name to seat for current hand */
	private transient final Map<String,Seat> seats = new TreeMap<String,Seat>();
	/** list of seats for current hand */
	private transient final List<Seat> seatsl = new ArrayList<Seat>();
	/** streets of action for current hand */
	private transient final List<List<Action>> streets = new ArrayList<List<Action>>();
	/** current hand */
	private transient Hand hand;
	/** is in summary phase */
	private transient boolean show = false, sum = false;
	
	public boolean debug;

	public PSParser() {
		//
	}
	
	private void println(String s) {
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

	private void clear() {
		show = false;
		sum = false;
		seats.clear();
		seatsl.clear();
		streets.clear();
		hand = null;
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
		if (debug)
			println(">  " + line);

		if (line.length() == 0) {
			if (sum && hand != null) {
				// finalise hand and return
				hand.seats = seatsl.toArray(new Seat[seatsl.size()]);
				hand.streets = new Action[streets.size()][];
				for (int n = 0; n < streets.size(); n++) {
					List<Action> street = streets.get(n);
					hand.streets[n] = street.toArray(new Action[street.size()]);
				}
				hand.showdown = show;
				println("end of " + hand);
				hands.add(hand);
				ret = hand;
				clear();
			}

		} else if (line.startsWith("PokerStars ")) {
			// PokerStars Game #73347266323:  Omaha Pot Limit ($0.01/$0.02 USD) - 2012/01/05 16:12:04 ET
			// PokerStars Game #73076810536:  5 Card Draw No Limit (100/200) - 2011/12/31 14:45:08 ET
			// PokerStars Game #73112640557: Tournament #493078525, 2000+110 Omaha Pot Limit - Level I (10/20) - 2012/01/01 13:43:02 ET
			// PokerStars Game #73111358128:  Hold'em Pot Limit (100/200) - 2012/01/01 13:19:41 ET
			// PokerStars Zoom Hand #77405734487:  Omaha Pot Limit ($0.01/$0.02) - 2012/03/18 14:38:20 ET
			if (hand != null) {
				throw new RuntimeException("unsaved game");
			}

			if (line.contains("Tournament")) {
				throw new RuntimeException("no tournaments");
			}

			Hand hand = new Hand();
			int i1 = line.indexOf("#");
			int i2 = line.indexOf(":", i1);
			int ns = nextToken(line, i2);
			int i4 = line.indexOf("-", i2);
			int ds = nextToken(line, i4);

			// TODO check if game already exists
			hand.id = parseLong(line, i1 + 1);

			String name = cache(line.substring(ns, i4 - 1));
			hand.gamename = name;
			if (name.contains("Hold'em")) {
				hand.gametype = HandUtil.HE_TYPE;
			} else if (name.contains("Omaha")) {
				hand.gametype = HandUtil.OM_TYPE;
			} else if (name.contains("5 Card Draw")) {
				hand.gametype = HandUtil.FCD_TYPE;
			}

			if (hand.gamename.indexOf("$") >= 0) {
				hand.currency = '$';
			}

			String datestr = line.substring(ds);
			try {
				// 2011/12/31 14:45:08 ET
				Date date = dateFormat.parse(datestr.replace("ET", "EST"));
				hand.date = date;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			this.hand = hand;
			println("game " + hand.id);

		} else if (line.startsWith("Betting is capped")) {
			// TODO
			println("capped");

		} else if (line.startsWith("Table")) {
			// Table 'Roehla IX' 9-max Seat #7 is the button
			// Table 'Sabauda VI' 6-max (Play Money) Seat #5 is the button
			// Table 'Honoria V' 6-max Seat #6 is the button
			// Table 'Mekbuda VIII' 2-max (Play Money) Seat #2 is the button
			// Table '493078525 1' 9-max Seat #1 is the button
			// Table 'bltable.1225797637.1225917089' 6-max
			// seat 1 is button if unspec
			int a = line.indexOf("'");
			int b = line.indexOf("'", a + 1);
			hand.tablename = cache(line.substring(a + 1, b));
			int c = line.indexOf("-max");
			hand.max = Integer.parseInt(line.substring(c - 1, c));
			int d = line.indexOf("Seat");
			if (d > 0) {
				hand.button = Integer.parseInt(line.substring(d + 6, d + 7));
			} else {
				println("table " + hand.tablename);
				hand.button = 1;
			}

		} else if (line.startsWith("Seat")) {
			int a = line.indexOf(":");
			int seatno = parseInt(line, a - 1);

			if (sum) {
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
					for (Seat seat : seatsl) {
						if (seat.num == seatno) {
							// could be mucking more than they showed
							checkMuckedHand(seat.hand, hand);
							seat.hand = hand;
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
				seat.num = seatno;
				seat.name = cache(line.substring(a + 2, b - 1));
				seat.chips = parseMoney(line, b + 1);
				seats.put(seat.name, seat);
				seatsl.add(seat);
				println("seat " + seat);
			}

		} else if (line.startsWith("***")) {
			// *** HOLE CARDS ***
			// *** FLOP *** [6d 3s Qc]
			// *** TURN *** [6d 3s Qc] [8s]
			// *** RIVER *** [6d 3s Qc 8s] [5d]
			// *** SHOW DOWN ***
			// *** SUMMARY ***

			// *** DEALING HANDS ***
			// discards
			// *** SHOW DOWN ***
			// *** SUMMARY ***

			int a = line.indexOf("***");
			int b = line.indexOf("***", a + 3);
			String name = line.substring(a + 4, b - 1);
			boolean newstr = false;
			boolean ignstr = false;

			if (hand.gametype == HandUtil.HE_TYPE || hand.gametype == HandUtil.OM_TYPE) {
				if (name.equals("FLOP") || name.equals("TURN") || name.equals("RIVER")) {
					newstr = true;
				} else if (name.equals("HOLE CARDS")) {
					ignstr = true;
				}
				
				
			} else if (hand.gametype == HandUtil.FCD_TYPE) {
				if (name.equals("DEALING HANDS")) {
					newstr = true;
				}
			}

			if (newstr) {
				streets.add(new ArrayList<Action>());
				println("street " + streets.size());
				
			} else if (name.equals("SHOW DOWN")) {
				show = true;
				
			} else if (name.equals("SUMMARY")) {
				sum = true;
				
			} else if (!ignstr) {
				throw new RuntimeException("unknown phase " + name);
			}

		} else if (line.startsWith("Board ")) {
			// Board [6d 3s Qc 8s 5d]
			int a = nextToken(line, 0);
			hand.board = parseHand(line, a);
			println("board " + Arrays.asList(hand.board));

		} else if (line.startsWith("Dealt to")) {
			// Dealt to tawvx [4c 6h 7d 5h]
			// after draw...
			// Dealt to tawvx [Ts Th] [Kc 5d Qh]
			// if discard all
			// Dealt to tawvx [5c 7h 3d 4d Jh]
			int a = line.indexOf("[");
			String name = line.substring(9, a - 1);
			Seat myseat = seats.get(name);
			if (myseat == null) {
				throw new RuntimeException("could not find seat " + name);
			}
			hand.myseat = myseat;

			String[] hand = parseHand(line, a);
			if (myseat.discards > 0) {
				if (myseat.hand == null) {
					throw new RuntimeException("draw without prev hand");
				}
				if (myseat.drawn != null) {
					// probably fail for triple draw
					throw new RuntimeException("already drawn");
				}
				int c = line.indexOf("[", a + 1);
				if (c > 0) {
					// discarded 1-4
					String[] drawn = parseHand(line, c);
					myseat.drawn = new String[][] { myseat.hand, hand, drawn };
					String[] newhand = new String[hand.length +  drawn.length];
					for (int n = 0; n < newhand.length; n++) {
						newhand[n] = n < hand.length ? hand[n] : drawn[n - hand.length];
					}
					myseat.hand = newhand;
				} else {
					// discard all
					myseat.drawn = new String[][] { myseat.hand, null, hand };
					myseat.hand = hand;
				}

			} else {
				checkNewHand(myseat.hand, hand);
				myseat.hand = hand;
			}

			println("dealt " + name + " " + Arrays.asList(hand));

		} else if (line.startsWith("Uncalled")) {
			// Uncalled bet ($0.19) returned to Hokage_91
			int i1 = line.indexOf("(");
			int amount = parseMoney(line, i1 + 1);
			int i2 = line.indexOf("to");
			String name = line.substring(i2 + 3);
			Seat seat = seats.get(name);
			if (seat != null) {
				seat.uncalled = amount;
			} else {
				throw new RuntimeException("could not get seat " + name);
			}
			println("uncalled " + name + " " + amount);

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

		} else if (line.contains("joins the table at seat")) {
			// scotty912 joins the table at seat #6 
			println("joins");

		} else if (line.contains("collected")) {
			// olasz53 collected $1.42 from main pot
			// NightPred8or collected $1.41 from main pot
			// olasz53 collected $0.56 from side pot
			// NightPred8or collected $0.56 from side pot
			int a = line.indexOf("collected");
			String name = line.substring(0, a - 1);
			Seat seat = seats.get(name);
			if (seat == null) {
				throw new RuntimeException("could not find seat " + name);
			}
			int amount = parseMoney(line, a + 10);
			seat.won += amount;
			println("collected " + name + " " + amount);

		} else if (line.startsWith("Total pot")) {
			// Total pot $0.30 | Rake $0.01 
			// Total pot $4.15 Main pot $2.83. Side pot $1.12. | Rake $0.20 
			hand.pot = parseMoney(line, 10);
			int a = line.indexOf("Rake");
			hand.rake = parseMoney(line, a + 5);
			println("total " + hand.pot + " rake " + hand.rake);

		} else if (line.contains("said,")) {
			// tawvx said, "it's not a race"
			println("talk");

		} else if (line.contains(": ")) {
			// Bumerang16: posts small blind $0.01
			int a = line.indexOf(": ");
			String name = line.substring(0, a);
			Seat seat = seats.get(name);
			if (seat != null) {
				int b = nextToken(line, a);
				int c = endToken(line, b);
				String act = cache(line.substring(b, c));
				Action action = new Action();
				action.seat = seat;
				action.act = act;
				boolean draw = false;
				boolean post = false;
				boolean show = false;

				// TODO action constants
				if (act.equals("checks")) {
					// NSavov: checks 

				} else if (act.equals("folds")) {
					// azacel77: folds
					// Ninjajundiai: folds [5d 5s]
					int d = line.indexOf("[");
					if (d > 0) {
						String[] hand = parseHand(line, d);
						checkNewHand(seat.hand, hand);
						seat.hand = hand;
					}

				} else if (act.equals("mucks") || act.equals("doesn't")) {
					// scotty912: doesn't show hand 
					show = true;

				} else if (act.equals("calls") || act.equals("raises") || act.equals("bets")) {
					// Bumerang16: calls $0.01
					// bluff.tb: raises $0.05 to $0.07
					int e = line.length() - 1;
					if (line.endsWith("and is all-in")) {
						action.allin = true;
						e -= 14;
					}
					int d = prevToken(line, e);
					int lost = parseMoney(line, d);
					action.amount = lost;
					seat.lost += lost;

				} else if (act.equals("posts")) {
					// Bumerang16: posts small blind $0.01
					// pisti361: posts small & big blinds $0.03
					// Yury.Nik: posts big blind 50 and is all-in
					post = true;
					int end = line.length() - 1;
					if (line.endsWith("and is all-in")) {
						action.allin = true;
						end -= 14;
					}
					int d = prevToken(line, end);
					action.amount = parseMoney(line, d);

				} else if (act.equals("shows")) {
					// bluff.tb: shows [Jc 8h Js Ad] (two pair, Aces and Kings)
					show = true;
					int d = nextToken(line, c);
					String[] hand = parseHand(line, d);
					checkNewHand(seat.hand, hand);
					seat.hand = hand;

				} else if (act.equals("discards")) {
					draw = true;
					// tawvx: discards 1 card [Ah]
					// joven2010: discards 3 cards
					if (seat.discards > 0) {
						throw new RuntimeException("already discarded " + seat.discards);
					}
					int d = nextToken(line, c);
					// should probably put discard in action in case we do triple draw
					seat.discards = parseInt(line, d);
					int e = line.indexOf("[");
					if (e > 0) {
						if (hand.myseat != seat) {
							throw new RuntimeException();
						}
						String[] disc = parseHand(line, e);
						hand.mydiscard = disc;
					}

				} else if (act.equals("stands")) {
					draw = true;
					println("stands");

				} else {
					throw new RuntimeException("unknown action: " + action.act);
				}

				println("action " + action);
				// fake a new street if drawing
				if (streets.size() == 0 || streets.size() == 1 && draw) {
					println("new street");
					streets.add(new ArrayList<Action>());
				}
				List<Action> street = streets.get(streets.size() - 1);
				street.add(action);

			} else {
				throw new RuntimeException("unknown player: " + line);
			}

		} else {
			System.out.println("unknown line");
			System.out.println("> " + line);
			throw new RuntimeException("unknown line");
		}

		return ret;
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

	private static int endToken(String line, int off) {
		while (off < line.length() && line.charAt(off) != ' ') {
			off++;
		}
		return off;
	}

	private static int prevToken(String line, int off) {
		while (line.charAt(off) == ' ') {
			off--;
		}
		while (line.charAt(off) != ' ') {
			off--;
		}
		return off+1;
	}

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

	private static int parseMoney(String line, int off) {
		if (line.charAt(off) == '$') {
			off++;
		}
		int end = off;
		while (end < line.length() && ".0123456789".indexOf(line.charAt(end)) >= 0) {
			end++;
		}
		if (off == end) {
			throw new RuntimeException("no money at " + off);
		}
		String s = line.substring(off, end);
		if (s.contains(".")) {
			return (int) (Float.parseFloat(s) * 100f);
		} else {
			return Integer.parseInt(s);
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
	 * FIXME could lead to concurrent mod exception
	 */
	@Override
	public List<Hand> getHands() {
		return hands;
	}

}
