package pet.hp.util;

import java.io.*;
import java.util.*;

import pet.hp.*;

/**
 * find and parse files in given directory
 * wait for file changes
 */
public class FollowThread extends Thread {
	
	public volatile boolean stop;
	
	private final Parser parser;
	/** set of files to scan including directories */
	private final Set<String> files = new TreeSet<String>();
	/** map of found files to position read to */
	private final Map<String,long[]> fileMap = new TreeMap<String,long[]>();
	private final List<FollowListener> listeners = new ArrayList<FollowListener>();

	public FollowThread(Parser parser) {
		setName("follow thread");
		setPriority(Thread.MIN_PRIORITY);
		this.parser = parser;
	}
	
	public synchronized void addListener(FollowListener l) {
		listeners.add(l);
	}
	
	public synchronized void addFile(String filename) {
		files.add(filename);
		System.out.println("following " + files);
	}
	
	@Override
	public void run() {
		while (true) {
			synchronized (this) {
				for (String filename : files) {
					File file = new File(filename);
					if (file.isDirectory()) {
						for (File f : file.listFiles()) {
							parse(f);
						}
					} else {
						parse(file);
					}
				}
			}
			do {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} while (stop);
		}
	}
	
	private void parse(File f) {
		String name = f.getName();
		if (parser.isHistoryFile(name)) {
			long[] size = fileMap.get(name);
			if (size == null) {
				// offset
				fileMap.put(name, size = new long[1]);
			}

			long fsize = (int) f.length();
			long mt = f.lastModified();
			
			if (fsize > size[0] && mt < (System.currentTimeMillis() - 1000L)) {
				System.out.println("activity on " + name);
				read(f, size[0]);
				size[0] = fsize;
				System.out.println("hands: " + parser.getHands().size());
			}

		} else {
			System.out.println("-- ignoring " + name); 
		}
	}
	
	private void read(File f, long offset) {
		try {
			InputStream is = new FileInputStream(f);
			is.skip(offset);
			String line;
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			while ((line = br.readLine()) != null) {
				Hand h = null;
				try {
					h = parser.parseLine(line);
				} catch (RuntimeException e) {
					for (String l : parser.getDebug()) {
						System.out.println(l);
					}
					throw e;
				}
				if (h != null) {
					for (FollowListener l : listeners) {
						l.nextHand(h);
					}
				}
			}
			br.close();
			
		} catch (IOException e) {
			System.out.println("file " + f);
			e.printStackTrace(System.out);
		}
	}
	
}

