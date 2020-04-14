package com.dan323.expresions.base;

import com.dan323.expresions.base.stub.BinaryOperationStub;
import com.dan323.expresions.base.LogicOperation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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
