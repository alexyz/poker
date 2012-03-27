package pet.hp.util;

import java.io.*;
import java.util.*;
import pet.eq.*;
import pet.hp.*;
import pet.hp.impl.PSParser;

class P {
	Seat seat;
	int pip;
	int aino; // 1 = first all in, 2 = second all in
	boolean fold;
}

class A {
	List<Float> eq = new ArrayList<Float>();
	float ev;
	// spr
}

public class HandInfo2 {
	
	public static void main(String[] args) throws Exception {
		PSParser p = new PSParser();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("x"), "UTF-8"));
		String line;
		while ((line = br.readLine()) != null) {
			Hand h = p.parseLine(line);
			if (h != null) {
				printHand3(h);
			}
		}
		br.close();
	}
	
	public static void printHand3(Hand hand) {
		boolean omaha = hand.gametype == HandUtil.OM_TYPE;
		boolean holdem = hand.gametype == HandUtil.HE_TYPE;
		
		Map<String,P> pl = new TreeMap<String,P>();
		for (Seat s : hand.seats) {
			P p = new P();
			p.seat = s;
			pl.put(s.name, p);
		}
		
		if (omaha || holdem) {
			HEPoker he = new HEPoker();
			int pot = 0;
			int aino = pl.size();
			
			for (int s = 0; s < hand.streets.length; s++) {
				System.out.println("street " + s);
				String[] board = s > 0 ? Arrays.copyOf(hand.board, s + 2) : null;
				
				for (Action a : hand.streets[s]) {
					System.out.println("  act " + a);
					P p = pl.get(a.seat.name);
					
						
					p.pip += a.amount;
					pot += a.amount;
					if (a.allin) {
						p.aino = aino--;
					}

					// print hand eq.. for each pot
					// get hands of those not folded
					// get eq of each pl

					// tawvx bets 2000 (eq=45%,65% ev=12345)

					// pot=p bet=b eq=e ev=(p+b)e-b
					// pot=2000 bet=2000 eq=45% ev=-200... assume 2b?
					// pot=4000 call=2000 eq=45% ev=700
					// pot=4000 call=2000 eq=55% ev=1300
					// pot=2000 bet=1 eq=45% ev=899.45
					// pot=2000 bet=2000 eq=1% ev=40-2000
					// pot=2000 bet=2000 eq=99% ev=2000-40
					// just checking has +ev

					// f map pn -> p, amount    ->     map pn -> (list eq, ev)

					
				}
			}
			
		}
		
	}
	
	static Map<String,A> eq(Map<String,P> pmap, String[] board, boolean omaha, int pot, String name, int amount) {
		Map<String,A> amap = new TreeMap<String,A>();
		// first pot is ai <= pl.size
		// naive impl, assume one pot
		
		List<String> names = new ArrayList<String>();
		List<String[]> holes = new ArrayList<String[]>();
		for (P p : pmap.values()) {
			if (!p.fold && p.seat.hole != null && p.seat.hole.length > 0) {
				names.add(p.seat.name);
				holes.add(p.seat.hole);
			}
		}
		
		String[][] holesarr = holes.toArray(new String[holes.size()][]);
		HandEq[] eqs = new HEPoker().equity(board, holesarr, omaha);
		for (int p = 0; p < names.size(); p++) {
			HandEq eq = eqs[p];
			A a = new A();
			a.eq = Arrays.asList(new Float(eq.won + (eq.tied / 2)));
			if (names.get(p).equals(name)) {
				// FIXME
				a.ev = 0;
			}
		}
		
		return amap;
		
	}
	
	
	
	
	
	
	
	

	// TODO spr?
	// TODO per action not per street...
	public static void printhand2(Hand hand) {
		boolean omaha = hand.gametype == HandUtil.OM_TYPE;
		boolean holdem = hand.gametype == HandUtil.HE_TYPE;
		List<S2> strs = new ArrayList<S2>();
		
		Set<String> winners = new TreeSet<String>();
		for (Seat s : hand.seats) {
			if (s.won > 0) {
				winners.add(s.name);
			}
		}
		//System.out.println("winners: " + winners);

		if (omaha || holdem) {
			HEPoker he = new HEPoker();

			for (int s = 0; s < hand.streets.length; s++) {
				//System.out.println("new street " + s);
				S2 str = new S2();
				strs.add(str);
				
				// get hole cards and bet amounds
				for (Action act : hand.streets[s]) {
					SP2 p = str.get(act.seat.name);
					p.hole = act.seat.hole;
					if (act.act.equals("folds")) {
						//System.out.println(p.name + " folds");
						p.fold = true;
					} else if (act.amount > 0) {
						//System.out.println(p.name + " pips " + act.amount);
						p.pip += act.amount;
						str.pot += act.amount;
					}
				}
				
				// TODO refund only if called
				// refund uncalled bet
				/*
				if (s == hand.streets.length - 1) {
					for (Seat seat : hand.seats) {
						if (seat.uncalled > 0) {
							SP p = strs.get(strs.size() - 1).spmap.get(seat.name);
							System.out.println(p.name + " refunded " + seat.uncalled);
							p.pip -= seat.uncalled;
							p.pot -= seat.uncalled;
						}
					}
				}*/

				// get board for this street (if any)
				String[] board = s > 0 ? Arrays.copyOf(hand.board, s + 2) : null;
				str.name = HandUtil.getStreetName(hand.gametype, s) + " " + (board != null ? Arrays.asList(board) : "");

				if (hand.showdown) {
					// get players hole cards who made it to end of street
					List<String> names = new ArrayList<String>();
					List<String[]> holes = new ArrayList<String[]>();
					for (SP2 p : str.spmap.values()) {
						if (!p.fold && p.hole != null) {
							names.add(p.name);
							holes.add(p.hole);
							//System.out.println("to showdown: name=" + p.name + " hole=" + Arrays.asList(p.hole));
						}
					}
					
					// get their equity
					String[][] holesarr = holes.toArray(new String[holes.size()][]);
					HandEq[] eqs = he.equity(board, holesarr, omaha);
					for (int p = 0; p < names.size(); p++) {
						SP2 sp = str.spmap.get(names.get(p));
						HandEq eq = eqs[p];
						// TODO need number of players tied
						sp.eq = eq.won + (eq.tied / 2);
						float ev = (str.pot * (sp.eq / 100f)) - sp.pip;
						sp.ev = ev;
						sp.val = eq.current;
						sp.hasev = true;
					}
					
				} else {
					// set winner to 100
					for (SP2 sp : str.spmap.values()) {
						if (winners.contains(sp.name)) {
							//System.out.println(sp.name + " wins by default");
							sp.hasev = true;
							sp.eq = 100;
							sp.ev = str.pot - sp.pip;
							if (sp.hole != null && board != null && board.length >= 3) {
								sp.val = he.value(sp.hole, board, omaha);
							}
						} else {
							// TODO -ev for pip/fold
						}
					}
				}
			}
			
			for (int s = 0; s < strs.size(); s++) {
				S2 str = strs.get(s);
				System.out.println(str.name);
				for (SP2 p : str.spmap.values()) {
					System.out.printf("  %-20s  %-20s  %-10s  %-10s  %-10s  %s\n",
							p.name,
							p.hole != null ? Arrays.asList(p.hole) : "",
							String.format("%2.1f%%", p.eq),
							p.pip > 0 ? p.pip : "",
							p.hasev ? String.format("EV %2.1f", p.ev) : "",
							p.val > 0 ? Poker.desc(p.val) : "");
				}
			}
			
			// TODO sum player ev and all ev
			
			System.out.println();
		}

	}



	public static void printhand(Hand hand) {

		boolean omaha = hand.gametype == HandUtil.OM_TYPE;
		boolean holdem = hand.gametype == HandUtil.HE_TYPE;


		// TODO ...
		// equities for each player on each street
		// requires showdown or opp to show cards
		List<String> names = new ArrayList<String>();
		List<String[]> hands = new ArrayList<String[]>();
		int me = -1;
		for (Seat s : hand.seats) {
			if (s.hole != null) {
				names.add(s.name);
				hands.add(s.hole);
				if (s == hand.myseat) {
					me = names.size() - 1;
				}
			}
		}
		if (hands.size() > 1) {
			List<String> eqsn = new ArrayList<String>();
			List<HandEq[]> eqs = new ArrayList<HandEq[]>();
			// TODO should really remove these once folded
			String[][] holes = hands.toArray(new String[hands.size()][]);
			String[] board = hand.board;

			if (holdem || omaha) {
				HEPoker he = new HEPoker();
				eqsn.add("preflop");
				eqs.add(he.equity(null, holes, omaha));
				for (int bs = 3; bs <= 5; bs++) {
					if (board != null && bs <= board.length) {
						String[] b2 = Arrays.copyOf(board, bs);
						eqsn.add("board " + Arrays.asList(b2));
						eqs.add(he.equity(b2, holes, omaha));
					}
				}

				for (int s = 0; s < eqs.size(); s++) {
					System.out.println(eqsn.get(s));
					HandEq[] eq = eqs.get(s);
					for (int n = 0; n < hands.size(); n++) {
						System.out.printf("%-1s %-15s  %s    %s\n", n == me ? "*" : "", names.get(n), Arrays.asList(hands.get(n)), eq[n]);
					}
				}
				System.out.println();

				// pot and ev for each player
				// turn heq=50% pip=50 peq=33% evst=50 ev=100
				// HandPl { pot; }
				// HandPlStr { heq; pot; } pot for str that player is eligable for

				// should really be list of pots...
				float pot = 0;
				for (int s = 0; s < hand.streets.length; s++) {
					System.out.println("street " + s);
					Action[] acts = hand.streets[s];
					HandEq[] eq = s > 0 && s < 5 ? eqs.get(s - 1) : null;
					for (Action a : acts) {
						String l = "  " + a;
						pot += a.amount;
						if (a.amount > 0 && eq != null) {
							int i = names.indexOf(a.seat.name);
							if (i >=0) {
								float e = eq[i].won + eq[i].tied;
								float p = (a.amount / pot) * 100f;
								l += "  -  pot=" + pot + " eq=" + e + " pc=" + p + "  " + (e > p ? "good" : "bad!");
							}
						}
						System.out.println(l);
					}
				}


			} else {
				System.out.println("can't go game type " + hand.gametype);
			}
		} else {
			System.out.println("not enough hands to compare");
		}

		System.out.println();

	}
}


class SP2 {
	String name;
	String[] hole;
	int val;
	float eq;
	int pip;
	boolean fold;
	int pot;
	boolean hasev;
	float ev;
}

class S2 {
	String name;
	int pot;
	Map<String,SP2> spmap = new TreeMap<String,SP2>();
	SP2 get(String name) {
		SP2 sp = spmap.get(name);
		if (sp == null) {
			spmap.put(name, sp = new SP2());
			sp.name = name;
		}
		return sp;
	}
}

