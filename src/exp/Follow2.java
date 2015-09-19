
package exp;

import java.io.File;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * experimental class to use file system watcher
 */
public class Follow2 extends Thread {
	
	public static void main (String[] args) {
		File f = new File(System.getProperty("user.dir"));
		System.out.println("f=" + f);
		new Follow2(f).start();
	}
	
	private File file;
	
	public Follow2(File file) {
		this.file = file;
	}
	
	@Override
	public void run () {
		try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
			Path dir = file.toPath();
			System.out.println("dir=" + dir);
			dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
			
			WatchKey key;
			do {
				System.out.println("take");
				key = watcher.take();
				System.out.println("key=" + key);
				
				for (WatchEvent<?> event : key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();
					System.out.println("kind=" + kind);
					
					if (kind == ENTRY_CREATE || kind == ENTRY_DELETE || kind == ENTRY_MODIFY) {
						WatchEvent<Path> ev = (WatchEvent<Path>) event;
						Path filename = ev.context();
						System.out.println("filename=" + filename);
						Path child = dir.resolve(filename);
						System.out.println("child=" + child);
					}
				}
				
			} while (key.reset());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
