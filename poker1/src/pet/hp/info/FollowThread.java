package pet.hp.info;

import java.io.*;
import java.util.*;

import pet.hp.*;

/**
 * find and parse files in given directory.
 * wait for file changes
 */
public class FollowThread extends Thread {
	
	/** parser implementation */
	private final Parser parser;
	/** map of found files to position read to */
	private final Map<String,long[]> fileMap = new TreeMap<String,long[]>();
	/** listeners to send hands to */
	private final List<FollowListener> listeners = new ArrayList<FollowListener>();
	/** should scan directory */
	private volatile boolean follow;
	
	/** directory to scan */
	private File dir;
	/** files to parse */
	private final Set<File> files = new TreeSet<File>();
	/** rejected files */
	private final Set<File> rejFiles = new TreeSet<File>();

	public FollowThread(Parser parser) {
		super("follow thread");
		setPriority(Thread.MIN_PRIORITY);
		setDaemon(true);
		this.parser = parser;
	}
	
	public synchronized void addListener(FollowListener l) {
		listeners.add(l);
	}
	
	public synchronized void setPath(File dir) {
		if (dir.isDirectory()) {
			System.out.println("follow " + dir);
			this.dir = dir;
		} else {
			System.out.println("not a directory: " + dir);
		}
	}
	
	public synchronized void setFollow(boolean follow) {
		System.out.println("follow " + follow);
		this.follow = follow;
		notify();
	}
	
	public synchronized void addFile(File file) {
		System.out.println("added file " + file);
		files.add(file);
		notify();
	}
	
	@Override
	public void run() {
		System.out.println("follow thread running");
		while (true) {
			synchronized (this) {
				if (follow) {
					collect();
				}
				if (files.size() > 0) {
					process();
				}
			}

			try {
				if (follow) {
					Thread.sleep(1000);
				} else {
					synchronized (this) {
						while (!follow && files.size() == 0) {
							System.out.println("waiting");
							// wait for notify
							wait();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void process() {
		System.out.println("process " + files.size() + " files");
		for (FollowListener l : listeners) {
			l.doneFile(0, files.size());
		}
		int n = 0;
		for (File f : files) {
			long[] size = fileMap.get(f.getName());
			if (size == null) {
				fileMap.put(f.getName(), size = new long[1]);
			}
			long fsize = f.length();
			long lastmod = f.lastModified();
			long now = System.currentTimeMillis();
			// don't parse if written to in last second
			if (fsize > size[0] && lastmod < (now - 1000L)) {
				size[0] = read(f, size[0]);
				for (FollowListener l : listeners) {
					l.doneFile(n, files.size());
				}
			}
			n++;
		}
		files.clear();
	}

	private void collect() {
		//System.out.println("collect files");
		if (dir != null) {
			// collect files to parse
			for (File f : dir.listFiles()) {
				if (f.isFile() && parser.isHistoryFile(f.getName())) {
					long[] s = fileMap.get(f.getName());
					if (s == null) {
						fileMap.put(f.getName(), s = new long[1]);
					}
					if (f.length() > s[0]) {
						files.add(f);
					}
				} else if (!rejFiles.contains(f)) {
					System.out.println("rejected " + f);
					rejFiles.add(f);
				}
			}
		}
	}
	
	private long read(File file, long offset) {
		System.out.println("parsing " + file.getName());
		try {
			FileInputStream fis = new FileInputStream(file);
			fis.skip(offset);
			String line;
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
			long pos = 0;
			while ((line = br.readLine()) != null) {
				Hand hand = parser.parseLine(line);
				if (hand != null) {
					for (FollowListener l : listeners) {
						l.nextHand(hand);
						pos = fis.getChannel().position();
					}
				}
			}
			
			// XXX should check if halfway though hand
			br.close();
			System.out.println("  read from " + offset + " to " + pos);
			return pos;
			
		} catch (IOException e) {
			System.out.println("could not parse file " + file);
			for (String l : parser.getDebug()) {
				System.out.println(l);
			}
			e.printStackTrace(System.out);
			return offset;
		}
	}
	
}

