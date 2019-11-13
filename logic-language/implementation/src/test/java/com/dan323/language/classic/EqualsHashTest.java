package com.dan323.language.classic;

import com.dan323.expresions.classical.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author danco
 */
public class EqualsHashTest {

    @Test
    public void equalsConjTest() {
        VariableClassic v1 = new VariableClassic("P");
        VariableClassic v2 = new VariableClassic("P");
        ConjunctionClassic c1 = new ConjunctionClassic(v1, v1);
        ConjunctionClassic c2 = new ConjunctionClassic(v2, v2);
        Assertions.assertNotSame(c1, c2);
        Assertions.assertEquals(c1, c2);
        Assertions.assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void equalsImpTest() {
        VariableClassic v1 = new VariableClassic("P");
        VariableClassic v2 = new VariableClassic("P");
        ImplicationClassic c1 = new ImplicationClassic(v1, v1);
        ImplicationClassic c2 = new ImplicationClassic(v2, v2);
        Assertions.assertNotSame(c1, c2);
        Assertions.assertEquals(c1, c2);
        Assertions.assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void equalsDisjTest() {
        VariableClassic v1 = new VariableClassic("P");
        VariableClassic v2 = new VariableClassic("P");
        DisjunctionClassic c1 = new DisjunctionClassic(v1, v1);
        DisjunctionClassic c2 = new DisjunctionClassic(v2, v2);
        Assertions.assertNotSame(c1, c2);
        Assertions.assertEquals(c1, c2);
        Assertions.assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void testNotEquals() {
        VariableClassic v1 = new VariableClassic("P");
        VariableClassic v2 = new VariableClassic("Q");
        ConjunctionClassic c1 = new ConjunctionClassic(v1, v1);
        ConjunctionClassic c2 = new ConjunctionClassic(v2, v2);

        Assertions.assertNotEquals(c1, c2);
        Assertions.assertNotEquals(v1, c2);
        Assertions.assertNotEquals(c1, v2);
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
        Assertions.assertNotEquals(v, new ConjunctionClassic(v, w));
    }
}
