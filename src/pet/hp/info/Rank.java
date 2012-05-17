package pet.hp.info;

import pet.eq.Poker;

public class Rank implements Comparable<Rank> {
	private final int v; 
	public Rank(int v) {
		this.v = v;
	}
	@Override
	public String toString() {
		return v != 0 ? Poker.valueString(v) : null;
	}
	@Override
	public int compareTo(Rank r) {
		return r.v - v;
	}
	@Override
	public boolean equals(Object o) {
		return o instanceof Rank && ((Rank)o).v == v;
	}
	@Override
	public int hashCode() {
		return v;
	}
}
