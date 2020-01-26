package com.dan323.language.modal;

import com.dan323.expresions.modal.Always;
import com.dan323.expresions.modal.ConstantModal;
import com.dan323.expresions.modal.Sometime;
import com.dan323.expresions.modal.Until;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author danco
 */
public class ModalOperatorsTest {

    @Test
    public void alwaysTest() {
        Always always = new Always(ConstantModal.TRUE);
        Assertions.assertEquals("[] TRUE", always.toString());
    }

    @Test
    public void alwaysEqualsTest() {
        Always always = new Always(ConstantModal.TRUE);
        Always always2 = new Always(ConstantModal.TRUE);
        Always always3 = new Always(ConstantModal.FALSE);
        Assertions.assertNotEquals(always, always3);
        Assertions.assertEquals(always, always2);
        Assertions.assertEquals(always.hashCode(), always2.hashCode());
    }

    @Test
    public void sometimesTest() {
        Sometime sometime = new Sometime(ConstantModal.TRUE);
        Assertions.assertEquals("<> TRUE", sometime.toString());
    }

    @Test
    public void sometimesEqualsTest() {
        Sometime sometime = new Sometime(ConstantModal.TRUE);
        Sometime sometime1 = new Sometime(ConstantModal.TRUE);
        Sometime sometime2 = new Sometime(ConstantModal.FALSE);
        Assertions.assertNotEquals(sometime, sometime2);
        Assertions.assertEquals(sometime, sometime1);
        Assertions.assertEquals(sometime.hashCode(), sometime1.hashCode());
    }

    @Test
    public void untilTest() {
        Until until = new Until(ConstantModal.FALSE, ConstantModal.TRUE);
        Assertions.assertEquals("FALSE U TRUE", until.toString());
    }

    @Test
    public void untilEqualsTest() {
        Until until = new Until(ConstantModal.FALSE, ConstantModal.TRUE);
        Until until3 = new Until(ConstantModal.FALSE, ConstantModal.TRUE);
        Until until1 = new Until(ConstantModal.TRUE, ConstantModal.FALSE);
        Until until2 = new Until(ConstantModal.FALSE, ConstantModal.FALSE);
        Assertions.assertNotEquals(until, until2);
        Assertions.assertEquals(until, until3);
        Assertions.assertEquals(until.hashCode(), until3.hashCode());
        Assertions.assertNotEquals(until, until1);
    }
}
