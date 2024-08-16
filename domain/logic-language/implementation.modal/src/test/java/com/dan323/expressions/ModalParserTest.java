package com.dan323.expressions;

import com.dan323.expressions.modal.*;
import com.dan323.expressions.relation.Equals;
import com.dan323.expressions.relation.LessEqual;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ModalParserTest {

    private final ModalLogicParser parser = new ModalLogicParser();

    @Test
    public void parseBox() {
        ModalOperation m = parser.evaluate("[]P");
        assertInstanceOf(Always.class, m);
        assertEquals("P", ((Always) m).getElement().toString());
    }

    @Test
    public void parseDia() {
        ModalOperation m = parser.evaluate("<>P");
        assertInstanceOf(Sometime.class, m);
        assertEquals("P", ((Sometime) m).getElement().toString());
    }

    @Test
    public void parseLess() {
        ModalOperation m = parser.evaluate("i <= j");
        assertInstanceOf(LessEqual.class, m);
        assertEquals("i", ((LessEqual) m).getLeft());
        assertEquals("j", ((LessEqual) m).getRight());
    }

    @Test
    public void parseEq() {
        ModalOperation m = parser.evaluate("i = j");
        assertInstanceOf(Equals.class, m);
        assertEquals("i", ((Equals) m).getLeft());
        assertEquals("j", ((Equals) m).getRight());
    }

    @Test
    public void parseAnd() {
        ModalOperation m = parser.evaluate("P & Q");
        assertInstanceOf(ConjunctionModal.class, m);
        assertEquals("P", ((ConjunctionModal) m).getLeft().toString());
    }

    @Test
    public void parseOr() {
        ModalOperation m = parser.evaluate("P | Q");
        assertInstanceOf(DisjunctionModal.class, m);
        assertEquals("P", ((DisjunctionModal) m).getLeft().toString());
    }

    @Test
    public void parseConst() {
        ModalOperation m = parser.evaluate("TRUE");
        assertEquals(ConstantModal.TRUE, m);
        m = parser.evaluate("FALSE");
        assertEquals(ConstantModal.FALSE, m);
    }

    @Test
    public void parseImp() {
        ModalOperation m = parser.evaluate("P -> Q");
        assertInstanceOf(ImplicationModal.class, m);
        assertEquals("P", ((ImplicationModal) m).getLeft().toString());
    }

    @Test
    public void parseNeg() {
        ModalOperation m = parser.evaluate("-P");
        assertInstanceOf(NegationModal.class, m);
        assertEquals("P", ((NegationModal) m).getElement().toString());
    }
}
