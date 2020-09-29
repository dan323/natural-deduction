package com.dan323.expressions.base;

import com.dan323.expressions.base.stub.BinaryOperationStub;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author danco
 */
public class LogicOperationTest {

    @Test
    public void operationEqualsIsFalse() {
        Assertions.assertFalse(LogicOperation.logicOperationEquals(null, lo -> false, obj -> true));
        Assertions.assertFalse(LogicOperation.logicOperationEquals(List.of(), lo -> false, obj -> true));
        Assertions.assertFalse(LogicOperation.logicOperationEquals(new BinaryOperationStub(null, null), lo -> false, obj -> false));
        Assertions.assertFalse(LogicOperation.logicOperationEquals(new BinaryOperationStub(null, null), lo -> true, obj -> false));
        Assertions.assertTrue(LogicOperation.logicOperationEquals(new BinaryOperationStub(null, null), lo -> true, obj -> true));
    }
}
