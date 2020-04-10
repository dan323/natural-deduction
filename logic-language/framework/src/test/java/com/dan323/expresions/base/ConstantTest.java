package com.dan323.expresions.base;

import com.dan323.expresions.base.stub.ConstantStub;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConstantTest {

    @Test
    public void castTest() {
        ConstantStub constantStub = new ConstantStub();
        Assertions.assertNotNull(constantStub.castToLanguage());
        Assertions.assertEquals(constantStub, constantStub.castToLanguage());
    }
}
