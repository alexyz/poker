package pet.eq;

/**
 * 5 card hand value function
 */
abstract class Value {
	/** non-hilo equity type */
	public final int eqtype;
	public Value(int eqtype) {
		this.eqtype = eqtype;
	}
	/**
	 * get hand value
	 */
	public abstract int value(String[] hand);
	/**
	 * get normalised score (experimental)
	 */
	public abstract float score(String[] hand, float bias);
}
