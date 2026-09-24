package com.dan323.expressions.relation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StateTermTest {

    @Test
    void termsAreReadAndWritten() {
        assertEquals(new StateTerm("s0", 0), StateTerm.parse("s0"));
        assertEquals(new StateTerm("s0", 1), StateTerm.parse("s0+1"));
        assertEquals(new StateTerm("s1", 3), StateTerm.parse(" s1 + 1 + 2 "));
        assertEquals(new StateTerm("_a1", 0), StateTerm.parse("_a1+0"));
        assertEquals("s0", StateTerm.normalize("s0+0"));
        assertEquals("s0+2", StateTerm.normalize("s0+1+1"));
        assertEquals("s2+10", new StateTerm("s2", 10).toString());
    }

    @Test
    void successorAndPredecessor() {
        var s0 = StateTerm.parse("s0");

        assertTrue(s0.isBase());
        assertEquals("s0+1", s0.successor().toString());
        assertFalse(s0.successor().isBase());
        assertEquals(s0, s0.successor().predecessor());
        assertThrows(IllegalStateException.class, s0::predecessor);
    }

    @Test
    void whatIsNotAStateIsRejected() {
        for (var text : List.of("", "+1", "1", "0+1", "s0+", "s0++1", "s0+a", "s0-1", "s 0", "s0+99999999999", "p & q")) {
            assertThrows(IllegalArgumentException.class, () -> StateTerm.parse(text), text);
        }
        assertThrows(IllegalArgumentException.class, () -> StateTerm.parse(null));
        assertThrows(IllegalArgumentException.class, () -> new StateTerm("s0", -1));
        assertThrows(IllegalArgumentException.class, () -> new StateTerm("s0+1", 0));
    }
}
