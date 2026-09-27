package com.dan323.expressions.firstorder;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AlphaEquivalenceTest {

    private final FirstOrderParser parser = new FirstOrderParser();

    private void assertAlphaEqual(String left, String right) {
        FirstOrderOperation l = parser.parse(left);
        FirstOrderOperation r = parser.parse(right);
        assertEquals(l, r);
        assertEquals(r, l);
        assertEquals(l.hashCode(), r.hashCode());
    }

    private void assertNotAlphaEqual(String left, String right) {
        assertNotEquals(parser.parse(left), parser.parse(right));
        assertNotEquals(parser.parse(right), parser.parse(left));
    }

    @Test
    void renamingBoundVariablesKeepsFormulasEqual() {
        assertAlphaEqual("forall x. P(x)", "forall y. P(y)");
        assertAlphaEqual("exists x. forall y. R(x, y)", "exists y. forall x. R(y, x)");
        assertAlphaEqual("forall x. forall y. forall z. (x*y)*z = x*(y*z)",
                "forall a. forall b. forall c. (a*b)*c = a*(b*c)");
        assertAlphaEqual("P(z) & (forall x. Q(x, z))", "P(z) & (forall w. Q(w, z))");
        assertAlphaEqual("forall x. forall x. P(x)", "forall y. forall z. P(z)");
        assertAlphaEqual("- (exists x. x = e) | FALSE", "- (exists u. u = e) | FALSE");
        assertAlphaEqual("(forall x. P(x)) -> Q", "(forall y. P(y)) -> Q");
    }

    @Test
    void differentFormulasAreNotEqual() {
        assertNotAlphaEqual("forall x. P(x)", "exists x. P(x)");
        assertNotAlphaEqual("forall x. P(x)", "forall x. P(y)");
        assertNotAlphaEqual("forall x. forall y. R(x, y)", "forall x. forall y. R(y, x)");
        assertNotAlphaEqual("forall x. forall x. P(x)", "forall x. forall y. P(x)");
        assertNotAlphaEqual("a = b", "b = a");
        assertNotAlphaEqual("P(a)", "Q(a)");
        assertNotAlphaEqual("P(f(a))", "P(g(a))");
        assertNotAlphaEqual("p & q", "p | q");
        assertNotAlphaEqual("p & q", "p -> q");
        assertNotAlphaEqual("p", "- p");
        assertNotAlphaEqual("TRUE", "FALSE");
        assertNotAlphaEqual("TRUE", "T");
        assertNotAlphaEqual("a*b = c", "b*a = c");
        assertNotAlphaEqual("P(a*b)", "P(ab)");
        assertNotEquals(parser.parse("p"), "p");
        assertNotEquals(null, parser.parse("forall x. P(x)"));
    }

    @Test
    void equalFormulasWorkAsSetElements() {
        Set<FirstOrderOperation> set = Set.of(parser.parse("forall x. P(x)"), parser.parse("p"));
        assertTrue(set.contains(parser.parse("forall y. P(y)")));
        assertTrue(set.contains(new Predicate("p")));
        assertFalse(set.contains(parser.parse("exists y. P(y)")));
    }

    @Test
    void everyKindOfFormulaEqualsItsCopy() {
        String[] formulas = {"p & q", "p | q", "p -> q", "- p", "TRUE", "P(a, f(b))", "a*b = c",
                "forall x. P(x)", "exists x. P(x)"};
        for (String text : formulas) {
            FirstOrderOperation formula = parser.parse(text);
            FirstOrderOperation copy = parser.parse(text);
            assertEquals(formula, copy, text);
            assertEquals(formula.hashCode(), copy.hashCode(), text);
            assertEquals(text, formula.toString());
        }
    }
}
