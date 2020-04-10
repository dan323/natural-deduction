package com.dan323.expresions.modal;

import com.dan323.expresions.modal.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author danco
 */
public class EqualsHashTest {

    @Test
    public void equalsConjTest() {
        VariableModal v1 = new VariableModal("P");
        VariableModal v2 = new VariableModal("P");
        ConjunctionModal c1 = new ConjunctionModal(v1, v1);
        ConjunctionModal c2 = new ConjunctionModal(v2, v2);
        Assertions.assertNotSame(c1, c2);
        Assertions.assertEquals(c1, c2);
        Assertions.assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void negationEquals() {
        VariableModal P = new VariableModal("P");
        VariableModal Q = new VariableModal("Q");
        NegationModal d = new NegationModal(P);
        NegationModal q = new NegationModal(P);
        NegationModal t = new NegationModal(Q);
        Assertions.assertNotEquals(P, d);
        Assertions.assertNotEquals(t, d);
        Assertions.assertEquals(q, d);
        Assertions.assertEquals(q.hashCode(), d.hashCode());
    }

    @Test
    public void equalsImpTest() {
        VariableModal v1 = new VariableModal("P");
        VariableModal v2 = new VariableModal("P");
        VariableModal v3 = new VariableModal("Q");
        ImplicationModal c1 = new ImplicationModal(v1, v1);
        ImplicationModal c2 = new ImplicationModal(v2, v2);
        ImplicationModal c3 = new ImplicationModal(v2, v3);
        Assertions.assertNotSame(c1, c2);
        Assertions.assertEquals(c1, c2);
        Assertions.assertNotEquals(c1, c3);
        Assertions.assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void equalsDisjTest() {
        VariableModal v1 = new VariableModal("P");
        VariableModal v2 = new VariableModal("P");
        VariableModal v3 = new VariableModal("Q");
        DisjunctionModal c1 = new DisjunctionModal(v1, v1);
        DisjunctionModal c2 = new DisjunctionModal(v2, v2);
        DisjunctionModal c3 = new DisjunctionModal(v2, v3);
        Assertions.assertNotSame(c1, c2);
        Assertions.assertEquals(c1, c2);
        Assertions.assertNotEquals(c1, c3);
        Assertions.assertEquals(c1.hashCode(), c2.hashCode());
    }


    @Test
    public void testNotEquals() {
        VariableModal v1 = new VariableModal("P");
        VariableModal v2 = new VariableModal("Q");
        ConjunctionModal c1 = new ConjunctionModal(v1, v1);
        ConjunctionModal c2 = new ConjunctionModal(v2, v2);

        Assertions.assertNotEquals(c1, c2);
        Assertions.assertNotEquals(v1, c2);
        Assertions.assertNotEquals(c1, v2);
    }

    @Test
    public void equalsVarTest() {
        VariableModal v = new VariableModal("P");
        VariableModal w = new VariableModal("P");
        Assertions.assertEquals(v, w);
        Assertions.assertEquals(v.hashCode(), w.hashCode());
    }

    @Test
    public void equalsConsTest() {
        ConstantModal v = ConstantModal.FALSE;
        ConstantModal w = ConstantModal.TRUE;
        Assertions.assertNotEquals(v, w);
        Assertions.assertNotEquals(v.hashCode(), w.hashCode());
        Assertions.assertNotEquals(v, new ConjunctionModal(v, w));
    }
}
