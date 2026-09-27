package com.dan323.expressions.firstorder;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FirstOrderParserTest {

    private final FirstOrderParser parser = new FirstOrderParser();

    private static VariableTerm v(String name) {
        return new VariableTerm(name);
    }

    private static final String[] GROUP_FORMULAS = {
            "forall x. forall y. forall z. (x*y)*z = x*(y*z)",
            "forall x. e*x = x & x*e = x",
            "forall x. i(x)*x = e & x*i(x) = e",
            "forall x. forall y. i(x*y) = i(y)*i(x)",
            "forall x. i(i(x)) = x",
            "(forall x. x*x = e) -> (forall x. forall y. x*y = y*x)"
    };

    @Test
    void groupAxiomsPrintBackUnchanged() {
        for (String axiom : GROUP_FORMULAS) {
            FirstOrderOperation formula = parser.parse(axiom);
            assertEquals(axiom, formula.toString());
            assertEquals(formula, parser.parse(formula.toString()));
        }
    }

    private static final String[] ROUND_TRIP = {
            "p -> q",
            "P(x, f(y, z))",
            "- (P(a) & Q)",
            "(- P(a)) | TRUE",
            "exists x. - P(x)",
            "- (forall x. P(x))",
            "(forall x. P(x)) & Q",
            "Q & (forall x. P(x))",
            "(exists x. P(x)) -> FALSE",
            "a*b*c = d",
            "(a = b) & (b = c)",
            "- (a = b)",
            "forall x. exists y. R(x, y) | x = y"
    };

    @Test
    void roundTrip() {
        for (String text : ROUND_TRIP) {
            FirstOrderOperation formula = parser.parse(text);
            String printed = formula.toString();
            assertEquals(printed, parser.parse(printed).toString(), text);
            assertEquals(formula, parser.parse(printed), text);
        }
    }

    @Test
    void productIsLeftAssociativeAndTighterThanEquals() {
        FirstOrderOperation formula = parser.parse("a*b*c = d");
        Equals equals = assertInstanceOf(Equals.class, formula);
        assertEquals(new Product(new Product(v("a"), v("b")), v("c")), equals.getLeft());
        assertEquals(v("d"), equals.getRight());
        assertEquals("(a*b)*c = d", formula.toString());
    }

    @Test
    void productInsideFunctionArguments() {
        Term term = parser.parseTerm("f(a*b, i(c))");
        assertEquals(new FunctionApplication("f", List.of(new Product(v("a"), v("b")),
                new FunctionApplication("i", List.of(v("c"))))), term);
        assertEquals("f(a*b, i(c))", term.toString());
    }

    @Test
    void equalsBindsTighterThanConnectives() {
        FirstOrderOperation formula = parser.parse("a = b & - b = c");
        ConjunctionFirstOrder conjunction = assertInstanceOf(ConjunctionFirstOrder.class, formula);
        assertEquals(new Equals(v("a"), v("b")), conjunction.getLeft());
        assertEquals(new NegationFirstOrder(new Equals(v("b"), v("c"))), conjunction.getRight());
        assertEquals("a = b & (- b = c)", formula.toString());
    }

    @Test
    void connectivesKeepTheClassicalPrecedence() {
        // as in ClassicalParser: '-' binds tightest, then '->', then '|', then '&'
        FirstOrderOperation formula = parser.parse("p & q -> r | - s");
        ConjunctionFirstOrder conjunction = assertInstanceOf(ConjunctionFirstOrder.class, formula);
        assertEquals(new Predicate("p"), conjunction.getLeft());
        DisjunctionFirstOrder disjunction = assertInstanceOf(DisjunctionFirstOrder.class, conjunction.getRight());
        assertEquals(new ImplicationFirstOrder(new Predicate("q"), new Predicate("r")), disjunction.getLeft());
        assertEquals(new NegationFirstOrder(new Predicate("s")), disjunction.getRight());
        assertInstanceOf(ImplicationFirstOrder.class, ((ImplicationFirstOrder) parser.parse("p -> q -> r")).getLeft());
    }

    @Test
    void quantifierScopesAsFarRightAsItCan() {
        FirstOrderOperation formula = parser.parse("forall x. P(x) & Q");
        Forall forall = assertInstanceOf(Forall.class, formula);
        assertEquals("x", forall.getVariable());
        assertEquals(new ConjunctionFirstOrder(new Predicate("P", List.of(v("x"))), new Predicate("Q")), forall.getBody());
        assertEquals("forall x. P(x) & Q", formula.toString());

        FirstOrderOperation limited = parser.parse("(forall x. P(x)) & Q");
        ConjunctionFirstOrder conjunction = assertInstanceOf(ConjunctionFirstOrder.class, limited);
        assertInstanceOf(Forall.class, conjunction.getLeft());
        assertNotEquals(formula, limited);

        FirstOrderOperation right = parser.parse("Q -> exists y. P(y) | R");
        ImplicationFirstOrder implication = assertInstanceOf(ImplicationFirstOrder.class, right);
        Exists exists = assertInstanceOf(Exists.class, implication.getRight());
        assertInstanceOf(DisjunctionFirstOrder.class, exists.getBody());

        FirstOrderOperation negated = parser.parse("- forall x. P(x) & Q");
        NegationFirstOrder negation = assertInstanceOf(NegationFirstOrder.class, negated);
        assertInstanceOf(ConjunctionFirstOrder.class, ((Forall) negation.getElement()).getBody());
    }

    @Test
    void atoms() {
        assertEquals(new Predicate("p"), parser.parse("p"));
        assertEquals(new Predicate("P", List.of(v("x"), new FunctionApplication("f", List.of(v("y"))))),
                parser.parse("P(x, f(y))"));
        assertEquals(new Equals(new FunctionApplication("f", List.of(v("x"))), v("y")), parser.parse("f(x) = y"));
        assertEquals(new Equals(v("x"), v("y")), parser.parse("(x) = ((y))"));
        assertEquals(new Equals(v("x"), v("y")), parser.parse("(x = y)"));
        assertSame(ConstantFirstOrder.TRUE, parser.parse("TRUE"));
        assertSame(ConstantFirstOrder.FALSE, parser.parse("(FALSE)"));
        assertTrue(ConstantFirstOrder.FALSE.isFalsehood());
        assertFalse(ConstantFirstOrder.TRUE.isFalsehood());
        assertEquals("P", new Predicate("P").toString());
    }

    private static final String[] BAD_INPUT = {
            "P(", "forall . P", "x = ", "", "P)", "P(x,)", "P()", "(P", "a*b", "a * = b", "P(x) = y",
            "forall x P(x)", "forall X. P(X)", "exists forall. P", "x = Y", "P & ", "p q", "P # Q", "f(TRUE) = x",
            "forall x. ", "- ", "a = b = c", "x = y*"
    };

    @Test
    void badInputIsRejected() {
        for (String text : BAD_INPUT) {
            assertThrows(IllegalArgumentException.class, () -> parser.parse(text), text);
        }
    }

    @Test
    void nullIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(null));
    }

    @Test
    void badTermsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> parser.parseTerm("x = y"));
        assertThrows(IllegalArgumentException.class, () -> parser.parseTerm("P(x)"));
        assertThrows(IllegalArgumentException.class, () -> parser.parseTerm("f()"));
        assertEquals(v("e"), parser.parseTerm(" e "));
    }

    @Test
    void errorMessageNamesWhatWasExpected() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> parser.parse("forall . P"));
        assertEquals("Expected a variable but found '.'", error.getMessage());
        error = assertThrows(IllegalArgumentException.class, () -> parser.parse("x = "));
        assertEquals("Expected a term but found the end of the input", error.getMessage());
    }
}
