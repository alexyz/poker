package pet.hp.impl.ft;

import java.io.*;
import java.text.DateFormat;
import java.util.*;
import java.util.regex.*;

/**
 * A Full Tilt Poker hand parser, designed to be fast and efficient, to cover
 * (at least) holdem and omaha, real money and play money, and cash games and
 * sit and goes. 
 * 
 * XXX substring retains original string
 */
class HP {
    static final PrintStream out = System.out;
    /**
     * cache the strings so we don't have multiple instances of the same string
     */
    static final Map<String,String> cache = new HashMap<String,String>();
    static final Map<Integer,Game> games = new TreeMap<Integer,Game>();
    // map of player -> list of hand or seat?
    static final Set<String> players = new TreeSet<String>();
    
    static final Pattern handPat = Pattern.compile("#(\\d+)");
    static final Pattern gamePat = Pattern.compile("\\((\\d+)\\)");
    static final Pattern datePat = Pattern.compile("(\\d+)/(\\d+)/(\\d+)");
    static final Pattern timePat = Pattern.compile("(\\d+):(\\d+):(\\d+)");
    static final Pattern blindPat = Pattern.compile("posts the (\\w+) blind of (\\d+)");
    static final Pattern buttonPat = Pattern.compile("#(\\d+)");
    static final Pattern seatPat = Pattern.compile("Seat (\\d+): (.+?) \\(([\\d,]+)\\)");
    static final Pattern seatPat2 = Pattern.compile("Seat (\\d+):");
    static final Pattern cardPat = Pattern.compile("[2-9TJQKA][sdch]");
    static final Pattern potPat = Pattern.compile("Total pot ([\\d,]+)");
    static final Pattern rakePat = Pattern.compile("Rake (\\d+)");
    static final Pattern dealPat = Pattern.compile("Dealt to (.+?) \\[");
    static final Pattern wonPat = Pattern.compile("\\(([\\d,]+)\\)");
    static final Pattern amountPat = Pattern.compile("[\\d,]+");
    static final Pattern numberPat = Pattern.compile("\\d+");
    static final Pattern moneyPat = Pattern.compile("\\$(\\d+(\\.\\d+)?)");
    
    static String getCachedValue(String item) {
        if (item.startsWith(" ") || item.endsWith(" ")) {
            new Exception("## item is '" + item + "'").printStackTrace(out);
        }
        if (cache.containsKey(item)) {
            item = cache.get(item);
        } else {
            cache.put(item, item = new String(item));
        }
        return item;
    }
    
    public static void main (String[] args) throws Exception {
        out.println("dir is " + System.getProperty("user.dir"));
        /*
         * test real money, ring game
         * 
         * TODO load database, don't overwrite existing hands
         * store filename
         * game classification, pl/r, t/ring, tex/om
         * summary parsing
         * gui, filtered selection -> analysis
         * hud
         */
        
        //parseHand(loadFile("playsng"));
        //parseSummary(loadFile("playsngsum"));
        parseHand(loadFile("his/realsng"));
        parseSummary(loadFile("his/realsngsum"));
        
        out.println();
        
        for (Game game : games.values()) {
            out.println("GAME: " + game);
            out.println();
            for (Hand hand : game.hands) {
                out.println("  HAND: " + hand);
            }
            out.println();
        }
        
        out.println("cache size is " + cache.size());
                
    }

    private static List<String> loadFile (String file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        List<String> lines = new ArrayList<String>();
        while ((line = br.readLine()) != null) {
            lines.add(line);
        }
        br.close();
        return lines;
    }
    
    static void parseSummary(List<String> lines) {
        Game game = null;
        for (String line : lines) {
            if (line.startsWith("Full Tilt Poker Tournament Summary")) {
                // Full Tilt Poker Tournament Summary 250 Play Money Sit & Go (164106334) Omaha Hi Pot Limit
                Matcher m = gamePat.matcher(line);
                if (m.find()) {
                    int id = Integer.parseInt(m.group(1));
                    game = games.get(id);
                    if (game == null) {
                        game = new Game(id);
                    }
                    game.name1 = getCachedValue(line.substring(35, m.start() - 1));
                    game.name2 = getCachedValue(line.substring(m.end() + 1));
                        
                } else {
                    out.println("## could not match sum game id");
                }
                
            } else if (line.startsWith("Buy-In: ")) {
                if (line.contains("Play Chips")) {
                    game.unit = Game.Unit.PLAY;
                    // Buy-In: 250 Play Chips + 0 Play Chips
                    Matcher m = numberPat.matcher(line);
                    if (m.find()) {
                        game.buyin = Integer.parseInt(m.group(0));
                    } else {
                        out.println("## could not match play money buy in");
                    }
                    if (m.find()) {
                        game.cost = Integer.parseInt(m.group(0));
                    } else {
                        out.println("## could not match play money buy in cost");
                    }
                    
                } else if (line.contains("$")) {
                    game.unit = Game.Unit.REAL;
                    // Buy-In: $1 + $0.20
                    Matcher m = moneyPat.matcher(line);
                    if (m.find()) {
                        game.buyin = (int) (Float.parseFloat(m.group(1)) * 100);
                    } else {
                        out.println("## could not match real money buy in");
                    }
                    if (m.find()) {
                        game.cost =  (int) (Float.parseFloat(m.group(1)) * 100);
                    } else {
                        out.println("## could not match real money buy in cost");
                    }
                    
                } else {
                    // ftps?
                    out.println("## unknown buy in");
                }
                
            } else if (line.endsWith(" Entries")) {
                // 9 Entries
                
            } else {
                out.println("# unmatched sum line: " + line);
            }
        }
    }
    
    static void parseHand(List<String> lines) {
        Game game = null;
        Hand hand = null;
        String round = null;
        for (String line : lines) {
            if (line.startsWith("Full Tilt Poker Game")) {
                round = null;
                long handid = 0;
                int gameid = 0;
                {
                    // hand id
                    
                    Matcher m = handPat.matcher(line);
                    if (m.find()) {
                        handid = Long.parseLong(m.group(1));
                        
                    } else {
                        out.println("## could not match hand id");
                    }
                }
                {
                    // game id
                    
                    Matcher m = gamePat.matcher(line);
                    if (m.find()) {
                        gameid = Integer.parseInt(m.group(1));
                        
                    } else {
                        out.println("## could not match game id");
                    }
                }
                
                if (game == null || game.id != gameid) {
                    games.put(gameid, game = new Game(gameid));
                    if (game.unit == null) {
                        // do this in case there is no summary
                        game.unit = line.contains("$") ? Game.Unit.REAL : line.contains("Play") ? Game.Unit.PLAY : null;
                    }
                }
                
                hand = new Hand();
                hand.id = handid;
                hand.game = game;
                game.hands.add(hand);
                
                {
                    // date and time
                    // 10:49:11 ET - 2009/12/31
                    
                    Matcher dm = datePat.matcher(line);
                    Matcher tm = timePat.matcher(line);
                    if (dm.find() && tm.find()) {
                        int h = Integer.parseInt(tm.group(1));
                        int mi = Integer.parseInt(tm.group(2));
                        int s = Integer.parseInt(tm.group(3));
                        int y = Integer.parseInt(dm.group(1));
                        int mo = Integer.parseInt(dm.group(2));
                        int d = Integer.parseInt(dm.group(3));
                        Calendar cal = new GregorianCalendar(y, mo, d, h, mi, s);
                        hand.date = cal.getTime();
                        
                    } else {
                        out.println("## could not match date and time");
                    }
                }
                
            } else if (line.startsWith("The button is in seat")) {
                // The button is in seat #6
                Matcher m = buttonPat.matcher(line);
                if (m.find()) {
                    int but = Integer.parseInt(m.group(1));
                    for (Seat seat : hand.seats.values()) {
                        if (seat.id == but) {
                            seat.button = true;
                            break;
                        }
                    }
                    
                } else {
                    out.println("## could not match button");
                }
                
            } else if (line.startsWith("Seat")) {
                if (round == null) {
                    // Seat 3: Keynell (1,500)
                    Matcher m = seatPat.matcher(line);
                    if (m.find()) {
                        int seatno = Integer.parseInt(m.group(1));
                        String player = getCachedValue(m.group(2));
                        players.add(player);
                        int stack = Integer.parseInt(m.group(3).replace(",",""));
                        Seat seat = new Seat();
                        seat.id = seatno;
                        seat.player = player;
                        seat.stack = stack;
                        hand.seats.put(player, seat);
                        
                    } else {
                        out.println("## could not match pre hole seat");
                    }
                    
                } else if (round.equals("SUMMARY")) {
                    Matcher m = seatPat2.matcher(line);
                    if (m.find()) {
                        int seatno = Integer.parseInt(m.group(1));
                        Seat seat = null;
                        for (Seat seat_ : hand.seats.values()) {
                            if (seat_.id == seatno) {
                                seat = seat_;
                                break;
                            }
                        }
                        //Seat 1: effy71 (small blind) folded before the Flop
                        //Seat 2: One Big KAHUNA (big blind) folded before the Flop
                        //Seat 3: Keynell folded before the Flop
                        //Seat 4: snowmoney11 didn't bet (folded)
                        //Seat 5: Jarrett254 showed [Qs Kh] and lost with King Queen high
                        //Seat 6: temc1956 (button) showed [7c Kc] and won (3,075) with a pair of Sevens
                        if (line.contains("folded")) { // XXX too vauge?
                            seat.folded = true;
                        } else if (line.contains(" and lost ")) {
                            seat.lost = true;
                        } else if (line.contains(" and won ") || line.contains(" collected ")) {
                            Matcher wm = wonPat.matcher(line);
                            if (wm.find()) {
                                seat.won = Integer.parseInt(wm.group(1).replace(",", ""));
                            } else {
                                out.println("## could not match won amount");
                            }
                        } else {
                            //ignore
                            //out.println("# unknown seat outcome: " + line);
                        }
                    } else {
                        out.println("## could not match summary seat");
                    }
                        
                } else {
                    out.println("## seat on unknown round");
                }
                
            } else if (line.startsWith("***")) {
                // *** RIVER *** [7s 9s 3c 2h] [Jc]
                round = getCachedValue(line.substring(4, line.lastIndexOf("***") - 1));
                Matcher m = cardPat.matcher(line.substring(line.lastIndexOf("***")));
                for (int n = 0; m.find(); n++) {
                    if (hand.board == null) {
                        hand.board = new String[5];
                    }
                    if (hand.board[n] == null) {
                        hand.board[n] = getCachedValue(m.group(0));
                    }
                }
                
            } else if (line.startsWith("Total pot") && line.contains("Rake")) {
                // Total pot 3,075 | Rake 0
                Matcher pm = potPat.matcher(line);
                Matcher rm = rakePat.matcher(line);
                if (pm.find() && rm.find()) {
                    hand.pot = Integer.parseInt(pm.group(1).replace(",",""));
                    hand.rake = Integer.parseInt(rm.group(1).replace(",",""));
                } else {
                    out.println("## could not match pot");
                }
                
            } else if (line.startsWith("Dealt to")) {
                // Dealt to Keynell [4h 4c]
                String player = getCachedValue(line.substring("Dealt to ".length(), line.indexOf("[") - 1));
                Matcher m = cardPat.matcher(line.substring(line.indexOf("[")));
                Seat seat = hand.seats.get(player);
                List<String> holes = new LinkedList<String>();
                while (m.find()) {
                    holes.add(getCachedValue(m.group(0)));
                }
                seat.hole = holes.toArray(new String[holes.size()]);
                
            } else if (line.startsWith("Uncalled bet of ")) {
                // Uncalled bet of 30 returned to One Big KAHUNA
                Matcher m = amountPat.matcher(line);
                if (m.find()) {
                    int amount = Integer.parseInt(m.group(0).replace(",",""));
                    String player = line.substring(line.indexOf("returned to") + 12);
                    Seat seat = hand.seats.get(player);
                    seat.returned += amount;
                } else {
                    out.println("## could not match amount");
                }
                
            } else if (line.startsWith("Board: [") || line.startsWith("The blinds are now")) {
                // ignore
                
            } else if (line.length() > 0) {
                Seat seat = null;
                for (Seat s : hand.seats.values()) {
                    if (line.startsWith(s.player)) {
                        if (seat == null || s.player.length() > seat.player.length()) {
                            seat = s;
                        }
                    }
                }
                if (seat != null) {
                    // ignore player name
                    String line2 = line.substring(seat.player.length());
                    
                    if (line2.startsWith(" posts the ")) {
                        // effy71 posts the small blind of 15
                        Matcher m = blindPat.matcher(line2);
                        if (m.find()) {
                            String blind = m.group(1);
                            int amount = Integer.parseInt(m.group(2));
                            if (blind.equals("big")) {
                                seat.bb = true;
                                hand.bb = amount;
                                seat.blind += amount;
                                
                            } else if (blind.equals("small")) {
                                seat.sb = true;
                                hand.sb = amount;
                                seat.blind += amount;
                                
                            } else {
                                out.println("## unknown blind");
                            }
                            
                        } else {
                            out.println("## could not match blind");
                        }
                        
                    } else if (line2.startsWith(" bets ")) {
                        // Keynell bets 160
                        Matcher m = amountPat.matcher(line2);
                        if (m.find()) {
                            seat.bet += Integer.parseInt(m.group(0).replace(",",""));
                        } else {
                            out.println("## could not match bet amount");
                        }
                        
                        
                    } else if (line2.startsWith(" raises to ")) {
                        // Keynell raises to 160
                        Matcher m = amountPat.matcher(line2);
                        if (m.find()) {
                            seat.raised += Integer.parseInt(m.group(0).replace(",",""));
                        } else {
                            out.println("## could not match raise amount");
                        }
                        
                    } else if (line2.startsWith(" calls ")) {
                        // effy71 calls 80
                        Matcher m = amountPat.matcher(line2);
                        if (m.find()) {
                            seat.called += Integer.parseInt(m.group(0).replace(",",""));
                        } else {
                            out.println("## could not match called amount");
                        }
                        
                    } else if (line2.startsWith(" shows ")) {
                        // Keynell shows [9c 9h]
                        if (seat.hole == null) {
                            Matcher m = cardPat.matcher(line2);
                            List<String> hole = new LinkedList<String>();
                            while (m.find()) {
                                hole.add(m.group(0));
                            }
                            if (hole.size() == 0) {
                                out.println("## could not match hole cards: " + line);
                            } else {
                                seat.hole = hole.toArray(new String[hole.size()]);
                            }
                        }
                        
                    } else if (line2.startsWith(" folds") || 
                            line2.startsWith(" mucks") || 
                            line2.startsWith(" wins the pot ") ||
                            line2.startsWith(" has 15 seconds left to act") ||
                            line2.startsWith(" has reconnected") ||
                            line2.startsWith(" checks") ||
                            line2.startsWith(" has been disconnected") ||
                            line2.startsWith(" has returned") ||
                            line2.startsWith(" has timed out") ||
                            line2.startsWith(" is sitting out") ||
                            line2.startsWith(" has requested TIME") ||
                            line2.charAt(0) == ':') {
                        
                        // ignore these
                            
                    } else {
                        out.println("# unmatched seat action: "  + line);
                    }
                    
                } else {
                    out.println("# unmatched line: " + line);
                }
                
            } else {
                // TODO three blank lines and set hand to null
            }
                
        }   
    }
    
    static String cards (String[] cards, int len) {
        StringBuilder sb = new StringBuilder();
        if (cards != null) {
            for (String c : cards) {
                sb.append(c != null ? c : "..");
            }
        } else {
            for (int n = 0; n < len; n++) {
                sb.append("..");
            }
        }
        return sb.toString();
    }
}

class Game {
    enum Unit { REAL, PLAY }
    public Game(int id) {
        this.id = id;
    }
    String name1;
    String name2;
    int id;
    int buyin, cost;
    int entries;
    Unit unit;
    //boolean tourn;
    List<Hand> hands = new ArrayList<Hand>();
    @Override
    public String toString () {
        return String.format("Game " + id + " is " + unit + ": _" + name1 + "_ _" + name2 + "_");
    }
}

class Hand {
    String file;
    //String complete;
    Game game;
    long id;
    int sb, bb;
    Date date;
    int rake;
    Map<String,Seat> seats = new TreeMap<String,Seat>();
    String[] board;
    int pot;
    @Override
    public String toString () {
        String dateStr = "unknown";
        if (date != null) {
            dateStr = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(date);
        }
        return String.format("Hand[%s %s %s/%s %s %s %s]", id, dateStr, bb, sb, HP.cards(board, 5), pot, seats);
    }
}

class Seat {
    int id;
    String player;
    int stack;
    String[] hole;
    boolean bb, sb, button, folded, lost;
    // TODO position
    int blind;
    int raised;
    int called;
    int returned;
    int bet;
    int won;
    int lost() {
        return blind + raised + called + bet;
    }
    int won() {
        return won + returned;
    }
    float ev() {
        return ((float)(won() - lost())) / stack;
    }
    String pos() {
        StringBuilder sb = new StringBuilder();
        if (button)
            sb.append("B");
        if (bb)
            sb.append(sb.length() > 0 ? "/" : "").append("BB");
        if (this.sb)
            sb.append(sb.length() > 0 ? "/" : "").append("SB");
        return sb.toString();
    }
    @Override
    public String toString () {
        String holes = HP.cards(hole, 2);
        /*
        return String.format("[%s]%s s=%s bb=%s b=%s r=%s c=%s ret=%s cost=%s reward=%s EV=%.2f", 
                holes, pos(), stack, blind, bet, raised, called, returned, lost(), won(), ev());
                */
        return String.format("[%s]%s s=%s lost=%s won=%s EV=%.2f", holes, pos(), stack, lost(), won(), ev());
    }
}
