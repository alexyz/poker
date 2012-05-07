package pet.eq;

/**
 * 5 card hand value function
 */
abstract class Value {
	/**
	 * get hand value (high or low)
	 */
	public abstract int value(String[] hand);
}