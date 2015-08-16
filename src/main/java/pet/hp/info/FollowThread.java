package pet.hp.info;

import java.io.*;
import java.util.*;

import pet.hp.*;

/**
 * find and parse files in given directory.
 * scan for file changes
 */
public class FollowThread extends Thread {
	
	/** parser implementation */
	private final Parser parser;
	/** map of found files to position read to */
	private final Map<String,long[]> fileMap = new TreeMap<>();
	/** listeners to send hands to */
	private final List<FollowListener> listeners = new ArrayList<>();
	/** should scan directory or just wait */
	private volatile boolean follow;
	/** directory to scan */
	private File dir;
	/** files to parse */
	private final Set<File> files = new TreeSet<>();
	/** rejected files */
	private final Set<File> rejFiles = new TreeSet<>();
	private int age;
	
	public FollowThread(Parser parser) {
		super("follow thread");
		setPriority(Thread.MIN_PRIORITY);
		setDaemon(true);
		this.parser = parser;
	}
	
	public synchronized void addListener(FollowListener l) {
		listeners.add(l);
	}
	
	/** set the directory to scan for changes in. does nothing if file is not a directory */
	public synchronized void setPath(File dir) {
		if (dir.isDirectory()) {
			System.out.println("follow " + dir);
			this.dir = dir;
		} else {
			System.out.println("not a directory: " + dir);
		}
	}
	
	public synchronized void setAge(int age) {
		System.out.println("max age in days: " + age);
		this.age = age;
	}
	
	/** set whether the thread is currently scanning for file changes */
	public synchronized void setFollow(boolean follow) {
		System.out.println("follow " + follow);
		this.follow = follow;
		// wake up the follow thread
		notify();
	}
	
	/** unconditionally parse the given file */
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
				long t = System.nanoTime();
				if (follow) {
					collect();
				}
				if (files.size() > 0) {
					process();
					t = System.nanoTime() - t;
					System.out.println("parsed in " + (t / 1000000000.0) + " seconds");
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
			long t = age > 0 ? System.currentTimeMillis() - (age * 24 * 60 * 60 * 1000L) : 0;
			for (File f : dir.listFiles()) {
				if (f.isFile() && parser.isHistoryFile(f.getName())) {
					if (f.lastModified() > t) {
						long[] s = fileMap.get(f.getName());
						if (s == null) {
							fileMap.put(f.getName(), s = new long[1]);
						}
						if (f.length() > s[0]) {
							files.add(f);
						}
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
		
		try (FileInputStream fis = new FileInputStream(file)) {
			long skip = fis.skip(offset);
			if (skip != offset) {
				System.out.println("skip " + offset + " of " + file + " actually skipped " + skip);
			}
			try (BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"))) {
				long pos = 0;
				String line;
				while ((line = br.readLine()) != null) {
					boolean hand = parser.parseLine(line);
					if (hand) {
						pos = fis.getChannel().position();
					}
				}
				
				// XXX should check if halfway though hand
				System.out.println("  read from " + offset + " to " + pos);
				return pos;
			}
			
		} catch (IOException e) {
			e.printStackTrace(System.out);
			return offset;
			
		} catch (RuntimeException e) {
			System.out.println("could not parse file " + file);
			for (String l : parser.getDebug()) {
				System.out.println(l);
			}
			throw e;
			
		}
	}
	
}

