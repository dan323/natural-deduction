package com.dan323.expressions.base;

import com.dan323.expressions.base.stub.ConjuntionStub;
import com.dan323.expressions.base.stub.DisjunctionStub;
import com.dan323.expressions.base.stub.ImplicationStub;
import com.dan323.expressions.base.stub.NegationStub;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author danco
 */
public class OperatorTest {

    @Test
    public void operatorTest() {
        Assertions.assertEquals("&", new ConjuntionStub(null, null).getOperator());
        Assertions.assertEquals("|", new DisjunctionStub(null, null).getOperator());
        Assertions.assertEquals("->", new ImplicationStub(null, null).getOperator());
        Assertions.assertEquals("-", new NegationStub(null).getOperator());
    }
}
