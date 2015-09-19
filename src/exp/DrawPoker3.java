
package exp;

import java.util.Arrays;

public class DrawPoker3 {
	
	public static String[] get27DrawingHand (String[] oppHand, String[] hand, int drawn) {
		System.out.println("get drawing hand: " + Arrays.toString(hand) + " drawn: " + drawn);
		
		if (drawn < 0 || drawn > 5) {
			throw new RuntimeException("invalid drawn: " + drawn);
			
		} else if (drawn == 5) {
			// special case, no draw and no meaningful score
			return new String[0];
			
		} else if (drawn == 0) {
			// special case, nothing to test other than given hand
			return hand.clone();
		}
		
		// draw 1-4
		
		return null;
		
	}
	
}
