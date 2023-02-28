package com.dan323.expressions.base;

import com.dan323.expressions.base.stub.BinaryOperationStub;
import com.dan323.expressions.base.stub.LogicOperationStub;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author danco
 */
public class BinaryOperationTest {

    private BinaryOperation<LogicOperation> binaryOperation1;
    private BinaryOperation<LogicOperation> binaryOperation2;
    private BinaryOperation<LogicOperation> binaryOperation3;
    private BinaryOperation<LogicOperation> binaryOperation4;
    private BinaryOperation<LogicOperation> binaryOperation5;

    @BeforeEach
    public void init() {
        binaryOperation1 = new BinaryOperationStub(new LogicOperationStub(1), new LogicOperationStub(2));
        binaryOperation2 = new BinaryOperationStub(new LogicOperationStub(1), new LogicOperationStub(2));
        binaryOperation3 = new BinaryOperationStub(new LogicOperationStub(4), new LogicOperationStub(5));
        binaryOperation4 = new BinaryOperationStub(new LogicOperationStub(1), binaryOperation1);
        binaryOperation5 = new BinaryOperationStub(binaryOperation2, binaryOperation1);
    }

    @Test
    public void gettersTest() {
        Assertions.assertEquals(new LogicOperationStub(1), binaryOperation1.getLeft());
        Assertions.assertEquals(new LogicOperationStub(2), binaryOperation1.getRight());
    }

    @Test
    public void toStringTest() {
        Assertions.assertEquals(binaryOperation1.getLeft().toString() + " op " + binaryOperation1.getRight().toString(), binaryOperation1.toString());
        Assertions.assertEquals("(" + binaryOperation2.toString() + ") op (" + binaryOperation1.toString() + ")", binaryOperation5.toString());
    }
}
