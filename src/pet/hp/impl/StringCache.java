package pet.hp.impl;

import java.util.HashMap;

import pet.eq.Poker;

/** string cache to avoid multiple instances of same string */
public class StringCache {
	
	private static final HashMap<String,String> cache = new HashMap<>(4096);
	
	static {
		for (String c : Poker.deck) {
			get(c);
		}
	}
	
	/**
	 * get cached string instance
	 */
	public static synchronized String get(String s) {
		if (s != null) {
			String s2 = cache.get(s);
			if (s2 != null) {
				return s2;
			}
			// strings don't have a start/end index
			cache.put(s, s);
		}
		return s;
	}

	public static synchronized int size () {
		return cache.size();
	}
	
}
