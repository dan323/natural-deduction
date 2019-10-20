package com.dan323.langauge.classic;

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

        Assertions.assertEquals(clo.toString(), "- (" + v + " & " + c + ")");
    }

    @Test
    public void equalsVarTest() {
        VariableClassic v = new VariableClassic("P");
        VariableClassic w = new VariableClassic("P");
        Assertions.assertEquals(v, w);
        Assertions.assertEquals(v.hashCode(), w.hashCode());
    }

    @Test
    public void equalsConsTest() {
        ConstantClassic v = ConstantClassic.FALSE;
        ConstantClassic w = ConstantClassic.TRUE;
        Assertions.assertNotEquals(v, w);
        Assertions.assertNotEquals(v.hashCode(), w.hashCode());
    }

    @Test
    public void classicalEvaluationTest() {
        ConstantClassic c = ConstantClassic.TRUE;
        VariableClassic v = new VariableClassic("P");
        VariableClassic w = new VariableClassic("Q");
        ClassicalLogicOperation clo = new DisjunctionClassic(new NegationClassic(new ConjunctionClassic(v, w)), c);

        Map<String, Boolean> values = new HashMap<>();
        values.put("P", true);
        values.put("Q", false);
        Assertions.assertTrue(clo.evaluate(values));
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

}
