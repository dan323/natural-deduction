package com.dan323.language.modal;

import com.dan323.expresions.base.Implication;
import com.dan323.expresions.base.LogicOperation;
import com.dan323.expresions.modal.*;
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
        ModalLogicalExpression clo = new NegationModal(new ConjunctionModal(v, c));

        Assertions.assertEquals(v, (new ConjunctionModal(v, c)).getLeft());
        Assertions.assertEquals(c, (new ConjunctionModal(v, c)).getRight());

        Assertions.assertEquals("true", c.toString());
        Assertions.assertEquals("P", v.toString());
        Assertions.assertEquals("- ((" + v + ") & (" + c + "))", clo.toString());
    }

    @Test
    public void modalTest() {
        ConstantModal c = ConstantModal.TRUE;
        VariableModal v = new VariableModal("P");
        ModalLogicalExpression clo = new NegationModal(new ConjunctionModal(v, c));
        LogicOperation mock = new LogicOperation() {
        };
        Assertions.assertTrue(ModalLogicalExpression.isModal(clo));
        Assertions.assertTrue(ModalLogicalExpression.areModal(clo, c, v));
        Assertions.assertFalse(ModalLogicalExpression.areModal(clo, c, mock));
    }

    @Test
    public void constantConstructTest() {
        Assertions.assertNotEquals(ConstantModal.TRUE.hashCode(), ConstantModal.FALSE.hashCode());
    }

    @Test
    public void disjunctionTest() {
        VariableModal P = new VariableModal("P");
        DisjunctionModal d = new DisjunctionModal(P, P);
        Assertions.assertEquals("(P) | (P)", d.toString());
    }

    @Test
    public void implicationToString() {
        VariableModal P = new VariableModal("P");
        Implication d = new ImplicationModal(P, P);
        Assertions.assertEquals("(P) -> (P)", d.toString());
    }

    @Test
    public void negationEquals() {
        VariableModal P = new VariableModal("P");
        NegationModal d = new NegationModal(P);
        NegationModal q = new NegationModal(P);
        Assertions.assertNotEquals(P, d);
        Assertions.assertEquals(q, d);
        Assertions.assertEquals(q.hashCode(), d.hashCode());
    }

    @Test
    public void constantValue() {
        Assertions.assertTrue(ConstantModal.TRUE.getValue());
        Assertions.assertFalse(ConstantModal.FALSE.getValue());
    }

    @Test
    public void toStringComplex() {
        VariableModal P = new VariableModal("P");
        NegationModal d = new NegationModal(P);
        ImplicationModal n = new ImplicationModal(d, d);
        Assertions.assertEquals("(- (P)) -> (- (P))", n.toString());
    }
}
