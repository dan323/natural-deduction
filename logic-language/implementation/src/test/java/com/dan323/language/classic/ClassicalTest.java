package com.dan323.language.classic;

import com.dan323.expresions.base.Implication;
import com.dan323.expresions.base.LogicOperation;
import com.dan323.expresions.classical.*;
import com.dan323.expresions.classical.exceptions.InvalidMapValuesException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author danco
 */
public class ClassicalTest {

    @Test
    public void classicalToStringTest() {
        ConstantClassic c = ConstantClassic.TRUE;
        VariableClassic v = new VariableClassic("P");
        ClassicalLogicOperation clo = new NegationClassic(new ConjunctionClassic(v, c));

        Assertions.assertEquals("TRUE", c.toString());
        Assertions.assertEquals("P", v.toString());
        Assertions.assertEquals("- (" + v + " & " + c + ")", clo.toString());
    }

    @Test
    public void classicalTest() {
        ConstantClassic c = ConstantClassic.TRUE;
        VariableClassic v = new VariableClassic("P");
        ClassicalLogicOperation clo = new NegationClassic(new ConjunctionClassic(v, c));
        LogicOperation mock = new LogicOperation() {
        };

        Assertions.assertTrue(ClassicalLogicOperation.isClassical(clo));
        Assertions.assertTrue(ClassicalLogicOperation.areClassical(clo, c, v));
        Assertions.assertFalse(ClassicalLogicOperation.areClassical(clo, c, mock));
    }

    @Test
    public void classicalEvaluationTest() {
        ConstantClassic c = ConstantClassic.FALSE;
        VariableClassic v = new VariableClassic("P");
        VariableClassic w = new VariableClassic("Q");
        ClassicalLogicOperation clo = new DisjunctionClassic(new NegationClassic(new ConjunctionClassic(v, w)), c);

        Map<String, Boolean> values = new HashMap<>();
        values.put("P", true);
        values.put("Q", false);
        Assertions.assertTrue(clo.evaluate(values));
        Assertions.assertFalse((new ConjunctionClassic(ConstantClassic.FALSE, ConstantClassic.TRUE)).evaluate(null));
        Assertions.assertFalse((new ImplicationClassic(clo, new NegationClassic(clo))).evaluate(values));
    }

    @Test
    public void insufficientValuesMap() {
        ConstantClassic c = ConstantClassic.TRUE;
        VariableClassic v = new VariableClassic("P");
        VariableClassic w = new VariableClassic("Q");
        ClassicalLogicOperation clo = new ConjunctionClassic(new NegationClassic(new ConjunctionClassic(v, w)), c);

        Map<String, Boolean> values = new HashMap<>();
        values.put("Q", false);
        Assertions.assertThrows(InvalidMapValuesException.class, () -> clo.evaluate(values));
    }

    @Test
    public void constantHashCodeTest() {
        Assertions.assertTrue(ConstantClassic.FALSE.isFalsehood());
        Assertions.assertFalse(ConstantClassic.TRUE.isFalsehood());
        Assertions.assertNotEquals(ConstantClassic.TRUE.hashCode(), ConstantClassic.FALSE.hashCode());
    }

    @Test
    public void constantEvaluate() {
        Assertions.assertTrue(ConstantClassic.TRUE.evaluate(null));
        Assertions.assertFalse(ConstantClassic.FALSE.evaluate(null));
    }

    @Test
    public void disjunctionTest() {
        VariableClassic P = new VariableClassic("P");
        DisjunctionClassic d = new DisjunctionClassic(P, P);
        Assertions.assertEquals("P | P", d.toString());
    }

    @Test
    public void disjunctionEvaluate() {
        VariableClassic P = new VariableClassic("P");
        VariableClassic Q = new VariableClassic("Q");
        DisjunctionClassic d = new DisjunctionClassic(P, Q);
        Assertions.assertTrue(d.evaluate(Map.of("P", true, "Q", false)));
        Assertions.assertFalse(d.evaluate(Map.of("P", false, "Q", false)));
    }

    @Test
    public void implicationToString() {
        VariableClassic P = new VariableClassic("P");
        Implication d = new ImplicationClassic(P, P);
        Assertions.assertEquals("P -> P", d.toString());
    }

    @Test
    public void negationEquals() {
        VariableClassic P = new VariableClassic("P");
        NegationClassic d = new NegationClassic(P);
        NegationClassic q = new NegationClassic(P);
        Assertions.assertNotEquals(P, d);
        Assertions.assertEquals(q, d);;
        Assertions.assertEquals(q.hashCode(), d.hashCode());
    }

    @Test
    public void toStringComplex() {
        VariableClassic P = new VariableClassic("P");
        NegationClassic d = new NegationClassic(P);
        ImplicationClassic n = new ImplicationClassic(d,d);
        Assertions.assertEquals("(- P) -> (- P)",n.toString());
    }
}
