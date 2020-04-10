package com.dan323.expresions.base;

import com.dan323.expresions.base.stub.VariableStub;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author danco
 */
public class VariableTest {

    @Test
    public void toStringTest() {
        Assertions.assertEquals("P", new VariableStub("P").toString());
    }

    @Test
    public void equalsTest() {
        Variable<LogicOperation> p = new VariableStub("P");
        Variable<LogicOperation> p1 = new VariableStub("P");
        Variable<LogicOperation> q = new VariableStub("Q");
        Assertions.assertNotEquals(p, q);
        Assertions.assertEquals(p, p1);
        Assertions.assertEquals(p.hashCode(), p1.hashCode());
    }

    @Test
    public void castTest() {
        Variable<LogicOperation> p = new VariableStub("P");
        assertNotNull(p.castToLanguage());
        assertEquals(p, p.castToLanguage());
    }
}
