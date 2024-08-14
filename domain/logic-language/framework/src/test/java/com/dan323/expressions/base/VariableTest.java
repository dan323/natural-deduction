package com.dan323.expressions.base;

import com.dan323.expressions.base.stub.VariableStub;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author danco
 */
public class VariableTest {

    @Test
    public void toStringTest() {
        assertEquals("P", new VariableStub("P").toString());
    }

}
