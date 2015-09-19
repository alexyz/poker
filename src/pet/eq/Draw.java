
package pet.eq;

/** represents a possible draw and its average score */
public class Draw implements Comparable<Draw> {
	
	public final String[] cards;
	public float score;
	
	public Draw(String[] hole, float score) {
		this.cards = hole;
		this.score = score;
	}
	
	@Override
	public int compareTo (Draw other) {
		return (int) Math.signum(score - other.score);
	}
	
	@Override
	public String toString () {
		return String.format("%.3f -> ", score) + PokerUtil.cardsString(cards);
	}
}
