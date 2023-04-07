package com.dan323.expressions.relation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RelationOperationTest {

    @Test
    public void equalsTest() {
        Equals equals = new Equals("s0", "s1");
        assertEquals("s0", equals.getLeft());
        assertEquals("s1", equals.getRight());
        assertNotEquals("s2", equals);
        Equals equals2 = new Equals("s0", "s1");
        assertEquals(equals, equals2);
        assertNotSame(equals, equals2);
        assertNotEquals(null, equals);
        Equals equals3 = new Equals("s1", "s0");
        assertEquals(equals3, equals2);
    }

    @Test
    public void lessEqualTest() {
        LessEqual lessEqual = new LessEqual("s0", "s1");
        assertEquals("s0", lessEqual.getLeft());
        assertEquals("s1", lessEqual.getRight());
        assertNotEquals(lessEqual, new LessEqual("s1","s2"));
        LessEqual lessEqual2 = new LessEqual("s0", "s1");
        assertEquals(lessEqual, lessEqual2);
        assertNotSame(lessEqual, lessEqual2);
        assertNotEquals(null, lessEqual);
    }
}
