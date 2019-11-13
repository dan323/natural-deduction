package com.dan323.expresions.base;

import com.dan323.expresions.base.LogicOperation;
import com.dan323.expresions.base.stub.LogicOperationStub;
import com.dan323.expresions.base.UnaryOperation;
import com.dan323.expresions.base.stub.UnaryOperationStub;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author danco
 */
public class UnaryOperationTest {

    private UnaryOperation<LogicOperation> unaryOperation1;
    private UnaryOperation<LogicOperation> unaryOperation2;
    private UnaryOperation<LogicOperation> unaryOperation3;
    private UnaryOperation<LogicOperation> unaryOperation4;

    @BeforeEach
    public void init() {
        unaryOperation1 = new UnaryOperationStub(new LogicOperationStub(1));
        unaryOperation2 = new UnaryOperationStub(new LogicOperationStub(1));
        unaryOperation3 = new UnaryOperationStub(new LogicOperationStub(4));
        unaryOperation4 = new UnaryOperationStub(unaryOperation1);
    }

    @Test
    public void equalsTest() {
        Assertions.assertEquals(unaryOperation1, unaryOperation1);
        Assertions.assertEquals(unaryOperation1, unaryOperation2);
        Assertions.assertEquals(unaryOperation1.hashCode(), unaryOperation2.hashCode());
        Assertions.assertNotEquals(unaryOperation1, unaryOperation4);
        Assertions.assertNotEquals(unaryOperation1, unaryOperation3);
        Assertions.assertNotEquals(unaryOperation1, unaryOperation1.getElement());
    }

    @Test
    public void gettersTest() {
        Assertions.assertEquals(new LogicOperationStub(1), unaryOperation1.getElement());
    }

    @Test
    public void toStringTest() {
        Assertions.assertEquals("unop " + unaryOperation1.getElement().toString(), unaryOperation1.toString());
        Assertions.assertEquals("unop (" + unaryOperation1.toString() + ")", unaryOperation4.toString());
    }
}
