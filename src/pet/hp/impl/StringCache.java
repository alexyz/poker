package pet.hp.impl;

import java.util.HashMap;

import pet.eq.Poker;


public class StringCache {
	/** string cache to avoid multiple instances of same string */
	private static final HashMap<String,String> cache = new HashMap<>(1000);
	
	static {
		for (String c : Poker.deck) {
			get(c, false);
		}
	}
	
	/**
	 * get cached string instance
	 */
	public static synchronized String get(String s) {
		return get(s, true);
	}
	
	/**
	 * get cached string instance
	 */
	private static synchronized String get(String s, boolean reallocate) {
		if (s != null) {
			String s2 = cache.get(s);
			if (s2 != null) {
				return s2;
			}
			if (reallocate) {
				// create a new string that does not share the potentially large
				// backing array of the original
				s = new String(s);
			}
			cache.put(s, s);
		}
		return s;
	}

	public static synchronized int size () {
		return cache.size();
	}
	
}
