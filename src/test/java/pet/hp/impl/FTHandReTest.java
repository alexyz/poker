package pet.hp.impl;

import org.junit.Test;

import static org.junit.Assert.*;

public class FTHandReTest {
    @Test
    public void testMatchesRushPLOmahaHiHand() {
        assertTrue("Rush PL Omaha Hi hand didn't match", FTHandRe.pattern.matcher("Full Tilt Poker Game #35828218454: Table Rush Play 6 (6 max) - PL Omaha Hi - 5/10 - 05:28:44 ET - 2015/08/16").matches());
    }
    @Test
    public void testMatchesRushNLHoldemHand() {
        assertTrue("Rush NL Hold'em hand didn't match", FTHandRe.pattern.matcher("Full Tilt Poker Game #35824272255: Table Rush Play 1 (6 max) - NL Hold'em - 5/10 - 15:05:47 ET - 2015/08/14").matches());
    }
}