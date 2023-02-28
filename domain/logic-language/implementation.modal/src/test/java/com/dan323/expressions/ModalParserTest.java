package com.dan323.expressions;

import com.dan323.expressions.modal.*;
import com.dan323.expressions.relation.Equals;
import com.dan323.expressions.relation.LessEqual;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModalParserTest {

    private final ModalLogicParser<String> parser = new ModalLogicParser<>(Function.identity());

    @Test
    public void parseBox() {
        ModalOperation m = parser.evaluate("[]P");
        assertTrue(m instanceof Always);
        assertEquals("P", ((Always) m).getElement().toString());
    }

    @Test
    public void parseDia() {
        ModalOperation m = parser.evaluate("<>P");
        assertTrue(m instanceof Sometime);
        assertEquals("P", ((Sometime) m).getElement().toString());
    }

    @Test
    public void parseLess() {
        ModalOperation m = parser.evaluate("i <= j");
        assertTrue(m instanceof LessEqual);
        assertEquals("i", ((LessEqual<String>) m).getLeft());
        assertEquals("j", ((LessEqual<String>) m).getRight());
    }

    @Test
    public void parseEq() {
        ModalOperation m = parser.evaluate("i = j");
        assertTrue(m instanceof Equals);
        assertEquals("i", ((Equals<String>) m).getLeft());
        assertEquals("j", ((Equals<String>) m).getRight());
    }

    @Test
    public void parseAnd() {
        ModalOperation m = parser.evaluate("P & Q");
        assertTrue(m instanceof ConjunctionModal);
        assertEquals("P", ((ConjunctionModal) m).getLeft().toString());
    }

    @Test
    public void parseOr() {
        ModalOperation m = parser.evaluate("P | Q");
        assertTrue(m instanceof DisjunctionModal);
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
        assertTrue(m instanceof ImplicationModal);
        assertEquals("P", ((ImplicationModal) m).getLeft().toString());
    }

    @Test
    public void parseNeg() {
        ModalOperation m = parser.evaluate("-P");
        assertTrue(m instanceof NegationModal);
        assertEquals("P", ((NegationModal) m).getElement().toString());
    }
}
