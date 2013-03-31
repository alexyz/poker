package pet.hp.impl;

import java.util.regex.Pattern;

class PSHandRE {
	/** hand start pattern - no punctuation */
	// PokerStars Hand #84322807903:  Triple Draw 2-7 Lowball No Limit (5/10) - 2012/08/05 14:07:58 ET
	// PokerStars Hand #84393778794:  7 Card Stud Limit (20/40) - 2012/08/07 4:39:16 ET
	// PokerStars Hand #79306750218:  Triple Stud (7 Card Stud Limit, 4/8) - 2012/04/23 5:55:37 UTC [2012/04/23 1:55:37 ET]
	// PokerStars Hand #83338296941:  8-Game (Razz Limit, 100/200) - 2012/07/15 1:45:25 ET
	static final Pattern pat = Pattern.compile("PokerStars (?:(Zoom) )?(?:Hand|Game) (\\d+) "
			+ "(?:Tournament (\\d+) (?:(Freeroll)|(\\S+?)\\+(\\S+?)(?: (USD))?) )?" 
			+ "(?:(Mixed \\S+|Triple Stud|8Game|HORSE) )?"
			+ "(.+?) "
			+ "(No Limit|Pot Limit|Limit) "
			+ "(?:(?:Match Round (\\w+) )?(?:Level (\\w+)) )?" 
			+ "(\\S+?)/(\\S+?)(?: (USD))?");
	/** hand pattern capturing group constants */
	static final int zoom = 1, handid = 2, tournid = 3, freeroll = 4, tbuyin = 5, tcost = 6, tcur = 7, mix = 8, game = 9,
			limit = 10, tround = 11, tlevel = 12, sb = 13, bb = 14, blindcur = 15;
}