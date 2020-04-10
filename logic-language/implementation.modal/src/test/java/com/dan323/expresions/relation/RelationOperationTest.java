package com.dan323.expresions.relation;

import com.dan323.expresions.base.LogicOperation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;

public class RelationOperationTest {

    private LogicOperation logicOperation1 = mock(LogicOperation.class);

    private LogicOperation logicOperation2 = mock(LogicOperation.class);

    @Test
    public void equalsTest() {
        Equals<LogicOperation> equals = new Equals<>(logicOperation1, logicOperation2);
        assertEquals(logicOperation1, equals.getLeft());
        assertEquals(logicOperation2, equals.getRight());
        assertNotEquals(equals, logicOperation1);
        Equals<LogicOperation> equals2 = new Equals<>(logicOperation1, logicOperation2);
        assertEquals(equals, equals2);
        assertNotSame(equals, equals2);
        assertNotEquals(null, equals);
        Equals<LogicOperation> equals3 = new Equals<>(logicOperation2, logicOperation1);
        assertEquals(equals3, equals2);
    }

    @Test
    public void lessTest() {
        LessEqual<LogicOperation> lessEqual = new LessEqual<>(logicOperation1, logicOperation2);
        assertEquals(logicOperation1, lessEqual.getLeft());
        assertEquals(logicOperation2, lessEqual.getRight());
        assertNotEquals(lessEqual, logicOperation1);
        LessEqual<LogicOperation> lessEqual2 = new LessEqual<>(logicOperation1, logicOperation2);
        assertEquals(lessEqual, lessEqual2);
        assertNotSame(lessEqual, lessEqual2);
        assertNotEquals(null, lessEqual);
        assertNotEquals(lessEqual, null);
        LessEqual<LogicOperation> lessEqual3 = new LessEqual<>(logicOperation2, logicOperation1);
        assertNotEquals(lessEqual3, lessEqual2);
        assertTrue(RelationOperation.areRelation(lessEqual, lessEqual2));
        assertFalse(RelationOperation.areRelation(logicOperation2, lessEqual2));
        assertFalse(RelationOperation.areRelation(lessEqual2, logicOperation2));
    }
}
