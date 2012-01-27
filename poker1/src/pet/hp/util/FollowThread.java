package pet.hp.util;

import java.io.*;
import java.util.*;

import pet.HEPoker;
import pet.hp.HP;
import pet.hp.Hand;
import pet.hp.impl.PSHP;

// TODO start and stop
public class FollowThread extends Thread {
	
	private final HP parser;
	private final File file;
	private final FollowListener listener;

	public static void main(String[] args) throws Exception {
		//File d = new File("/Users/alex/Library/Application Support/PokerStars/HandHistory/tawvx");
		File d = new File("/Users/alex/Library/Application Support/PokerStars/HandHistory/tawvx/HH20120114 Tokai VI - 100-200 - Play Money No Limit Hold'em.txt");
		HP p = new PSHP();
		PSHP.out = Util.nullps;
		HEPoker.out = Util.nullps;
		FollowListener fl = new FollowListener() {
			@Override
			public void nextHand(Hand h) {
				System.out.println("next hand is " + h);
				//HandInfo.printhand(h);
				HandInfo.printhand2(h);
			}
		};
		FollowThread f = new FollowThread(p, d, fl);
		f.start();
	}
	
	public FollowThread(HP parser, File file, FollowListener listener) {
		setName("follow thread");
		setPriority(Thread.MIN_PRIORITY);
		this.parser = parser;
		this.file = file;
		this.listener = listener;
	}
	
	Map<String,int[]> fmap = new TreeMap<String,int[]>();
	
	@Override
	public void run() {
		while (true) {
			if (file.isDirectory()) {
				for (File f : file.listFiles()) {
					parse(f);
				}
			} else {
				parse(file);
			}
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void parse(File f) {
		String name = f.getName();
		if (parser.isHistoryFile(name)) {
			int[] size = fmap.get(name);
			if (size == null) {
				// size, line
				fmap.put(name, size = new int[2]);
			}

			int fsize = (int) f.length();
			long mt = f.lastModified();
			
			if (fsize > size[0] && mt < (System.currentTimeMillis() - 1000L)) {
				System.out.println("activity on " + name);
				size[0] = fsize;
				size[1] = read(f, size[1]);
				System.out.println("hands: " + parser.getHands().size());
			}

		}
	}
	
	private int read(File f, int lineno) {
		try {
			String line;
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
			int n = 0;
			// TODO should probably seek on fis
			while ((line = br.readLine()) != null) {
				n++;
				if (n > lineno) {
					Hand h = parser.parseLine(line);
					if (h != null) {
						listener.nextHand(h);
					}
				}
			}
			br.close();
			return n;
			
		} catch (IOException e) {
			System.out.println("file " + f);
			e.printStackTrace(System.out);
			return 0;
		}
	}
	
}

