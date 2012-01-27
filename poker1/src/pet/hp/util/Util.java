package pet.hp.util;

import java.awt.Dimension;
import java.io.*;
import java.util.*;

import javax.swing.JFrame;

import pet.hp.HP;
import pet.hp.Hand;
import pet.hp.impl.PSHP;
import pet.ui.Graph;
import pet.ui.GraphData;

public class Util {


	public static final PrintStream nullps = new PrintStream(new OutputStream() {
		@Override
		public void write(int arg0) throws IOException {
			//
		}
		@Override
		public void write(byte[] b) throws IOException {
			//
		}
		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			//
		}
	});

	public static void main(String[] args) {
		try {
			HP p = Util.parse();
			History his = new History();
			for (Hand h : p.getHands()) {
				his.addHand(h);
			}
			printMostPopular(his);
			List<GraphData> data = Bankroll.getBankRoll(p, "tawvx", "Omaha Pot Limit ($0.01/$0.02 USD)");

			JFrame f = new JFrame();
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			Graph g = new Graph(data, Bankroll.nf);
			g.setPreferredSize(new Dimension(800,600));
			f.setContentPane(g);
			f.pack();
			f.show();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	public static HP parse() throws Exception {
		PSHP.out = Util.nullps;
		PSHP p = new PSHP();
		String dirname = "/Users/alex/Library/Application Support/PokerStars/HandHistory/tawvx";
		File dir = new File(dirname);
		long t = System.nanoTime();
		List<File> files = new ArrayList<File>();
		for (File f : dir.listFiles()) {
			if (p.isHistoryFile(f.getName())) {
				files.add(f);
			}
		}

		for (File f : files) {
			//System.out.println("file " + f);
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
			int lineno = 0;
			String line;
			while ((line = br.readLine()) != null) {
				lineno++;
				try {
					p.parseLine(line);
				} catch (Exception e) {
					System.out.println();
					System.out.println(f);
					System.out.println("line " + lineno + ":");
					System.out.println(line);
					throw e;
				}
			}
			br.close();
		}

		System.out.println("took " + ((System.nanoTime() - t) / 1000000000.0) + " secs");

		/*
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream("data"));
			os.writeObject(p);
			os.close();
			File f = new File("data");
			System.out.println("wrote file " + f + " size " + f.length());
		 */

		return p;
	}


	private static void printMostPopular(History h) {
		// print most popular players
		Map<String, PlayerInfo> pmap = h.getInfo();
		PlayerInfo[] players = pmap.values().toArray(new PlayerInfo[pmap.size()]);
		Arrays.sort(players, PlayerInfo.handscmp);
		for (int n = players.length - 1; n >= 0 && n > players.length - 20; n--) {
			System.out.println(n + " -> " + players[n]);
		}
	}


}
