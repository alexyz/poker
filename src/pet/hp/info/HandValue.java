package pet.hp.info;

import pet.eq.Poker;

/** represents a hand value */
public class HandValue implements Comparable<HandValue> {
	private final int v; 
	public HandValue(int v) {
		this.v = v;
	}
	@Override
	public String toString() {
		return v != 0 ? Poker.valueString(v) : null;
	}
	@Override
	public int compareTo(HandValue r) {
		return r.v - v;
	}
	@Override
	public boolean equals(Object o) {
		return o instanceof HandValue && ((HandValue)o).v == v;
	}
	@Override
	public int hashCode() {
		return v;
	}
}
