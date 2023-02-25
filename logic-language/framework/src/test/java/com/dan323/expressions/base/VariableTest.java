package com.dan323.expressions.base;

import com.dan323.expressions.base.stub.VariableStub;
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

}
