package pet.hp.impl;

import java.util.regex.Pattern;

class PSHandRE {
	/** hand start pattern - no punctuation */
	// PokerStars Hand #84322807903:  Triple Draw 2-7 Lowball No Limit (5/10) - 2012/08/05 14:07:58 ET
	// PokerStars Hand #84393778794:  7 Card Stud Limit (20/40) - 2012/08/07 4:39:16 ET
	// PokerStars Hand #79306750218:  Triple Stud (7 Card Stud Limit, 4/8) - 2012/04/23 5:55:37 UTC [2012/04/23 1:55:37 ET]
	// PokerStars Hand #83338296941:  8-Game (Razz Limit, 100/200) - 2012/07/15 1:45:25 ET
	// PokerStars Home Game Hand #212631111111: {Club #3311111} Tournament #2871111111, £5.00+£0.50 GBP Hold'em No Limit - Level I (15/30) - 2020/01/01 01:01:01 WET [2020/01/01 01:01:01 ET]
	static final Pattern pat = Pattern.compile("PokerStars (?:(Home Game) )(?:(Zoom) )?(?:Hand|Game) (\\d+) "
			+ "(?:\\{(.*)\\} )(?:Tournament (\\d+) (?:(Freeroll)|(\\S+?)\\+(\\S+?)(?: (USD|GBP))?) )?"
			+ "(?:(Mixed \\S+|Triple Stud|8Game|HORSE) )?"
			+ "(.+?) "
			+ "(No Limit|Pot Limit|Limit) "
			+ "(?:(?:Match Round (\\w+) )?(?:Level (\\w+)) )?"
			+ "(\\S+?)/(\\S+?)(?: (USD|GBP))?");
	/** hand pattern capturing group constants */
	static final int homegame = 1, zoom = 2, handid = 3, clubidname = 4, tournid = 5, freeroll = 6, tbuyin = 7, tcost = 8, tcur = 9, mix = 10, game = 11,
			limit = 12, tround = 13, tlevel = 14, sb = 15, bb = 16, blindcur = 17;
}