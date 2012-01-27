package pet.hp;

import java.util.List;

public abstract class HP {
	public abstract List<Hand> getHands();
	public abstract Hand parseLine(String line);
	public abstract boolean isHistoryFile(String name);
}
