package com.dan323.expressions.relation;

import com.dan323.expressions.base.LogicOperation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;

public class RelationOperationTest {

    private final LogicOperation logicOperation1 = mock(LogicOperation.class);

    private final LogicOperation logicOperation2 = mock(LogicOperation.class);

    @Test
    public void equalsTest() {
        Equals equals = new Equals(logicOperation1.toString(), logicOperation2.toString());
        assertEquals(logicOperation1.toString(), equals.getLeft());
        assertEquals(logicOperation2.toString(), equals.getRight());
        assertNotEquals(equals, logicOperation1);
        Equals equals2 = new Equals(logicOperation1.toString(), logicOperation2.toString());
        assertEquals(equals, equals2);
        assertNotSame(equals, equals2);
        assertNotEquals(null, equals);
        Equals equals3 = new Equals(logicOperation2.toString(), logicOperation1.toString());
        assertEquals(equals3, equals2);
    }

    @Test
    public void lessEqualTest() {
        LessEqual lessEqual = new LessEqual(logicOperation1.toString(), logicOperation2.toString());
        assertEquals(logicOperation1.toString(), lessEqual.getLeft());
        assertEquals(logicOperation2.toString(), lessEqual.getRight());
        assertNotEquals(lessEqual, logicOperation1);
        LessEqual lessEqual2 = new LessEqual(logicOperation1.toString(), logicOperation2.toString());
        assertEquals(lessEqual, lessEqual2);
        assertNotSame(lessEqual, lessEqual2);
        assertNotEquals(null, lessEqual);
        LessEqual lessEqual3 = new LessEqual(logicOperation2.toString(), logicOperation1.toString());
        assertNotEquals(lessEqual3, lessEqual2);
        assertTrue(RelationOperation.areRelation(lessEqual, lessEqual2));
        assertFalse(RelationOperation.areRelation(logicOperation2, lessEqual2));
        assertFalse(RelationOperation.areRelation(lessEqual2, logicOperation2));
    }
}
