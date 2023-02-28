package com.dan323.expressions.modal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author danco
 */
public class ModalTest {

    @Test
    public void modalToStringTest() {
        ConstantModal c = ConstantModal.TRUE;
        VariableModal v = new VariableModal("P");
        ModalLogicalOperation clo = new NegationModal(new ConjunctionModal(v, c));

        Assertions.assertEquals(v, (new ConjunctionModal(v, c)).getLeft());
        Assertions.assertEquals(c, (new ConjunctionModal(v, c)).getRight());

        Assertions.assertEquals("TRUE", c.toString());
        Assertions.assertEquals("P", v.toString());
        Assertions.assertEquals("- (" + v + " & " + c + ")", clo.toString());
    }

    @Test
    public void constantConstructTest() {
        Assertions.assertNotEquals(ConstantModal.TRUE.hashCode(), ConstantModal.FALSE.hashCode());
    }

    @Test
    public void disjunctionTest() {
        VariableModal P = new VariableModal("P");
        DisjunctionModal d = new DisjunctionModal(P, P);
        Assertions.assertEquals("P | P", d.toString());
    }

    @Test
    public void implicationToString() {
        VariableModal P = new VariableModal("P");
        ImplicationModal d = new ImplicationModal(P, P);
        Assertions.assertEquals("P -> P", d.toString());
    }

    @Test
    public void constantValue() {
        Assertions.assertTrue(ConstantModal.TRUE.getValue());
        Assertions.assertTrue(ConstantModal.FALSE.isFalsehood());
        Assertions.assertFalse(ConstantModal.FALSE.getValue());
        Assertions.assertFalse(ConstantModal.TRUE.isFalsehood());
    }

    @Test
    public void toStringComplex() {
        VariableModal P = new VariableModal("P");
        NegationModal d = new NegationModal(P);
        ImplicationModal n = new ImplicationModal(d, d);
        Assertions.assertEquals("(- P) -> (- P)", n.toString());
    }
}
