package pet.hp.impl;

import org.junit.Test;
import pet.hp.Game;

import static org.junit.Assert.*;

public class ParseUtilTest {

    @Test
    public void testParseLimit() throws Exception {
        assertEquals(Game.Limit.NL, ParseUtil.parseLimit( "No Limit" ));
        assertEquals(Game.Limit.NL, ParseUtil.parseLimit( "NL" ));
        assertEquals(Game.Limit.PL, ParseUtil.parseLimit( "Pot Limit" ));
        assertEquals(Game.Limit.PL, ParseUtil.parseLimit( "PL" ));
        assertEquals(Game.Limit.FL, ParseUtil.parseLimit( "Limit" ));
        assertEquals(Game.Limit.FL, ParseUtil.parseLimit( "FL" ));
    }

    @Test (expected = IllegalArgumentException.class)
    public void testParseLimitReportsUnknownLimit() {
        ParseUtil.parseLimit("Unknown limit");
    }
}