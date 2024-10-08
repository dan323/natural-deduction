package com.dan323.expressions.base;

import com.dan323.expressions.base.stub.LogicOperationStub;
import com.dan323.expressions.base.stub.UnaryOperationStub;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author danco
 */
public class UnaryOperationTest {

    private UnaryOperation<LogicOperation> unaryOperation1;
    private UnaryOperation<LogicOperation> unaryOperation2;


    @Test
    public void castTest() {
        assertNotNull(unaryOperation1.castToLanguage());
        assertEquals(unaryOperation1, unaryOperation1.castToLanguage());
    }

    @BeforeEach
    public void init() {
        unaryOperation1 = new UnaryOperationStub(new LogicOperationStub(1));
        unaryOperation2 = new UnaryOperationStub(unaryOperation1);
    }

    @Test
    public void gettersTest() {
        Assertions.assertEquals(new LogicOperationStub(1), unaryOperation1.getElement());
    }

    @Test
    public void toStringTest() {
        Assertions.assertEquals("unop " + unaryOperation1.getElement().toString(), unaryOperation1.toString());
        Assertions.assertEquals("unop (" + unaryOperation1.toString() + ")", unaryOperation2.toString());
    }
}
