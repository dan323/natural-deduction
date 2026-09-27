package com.dan323.expressions.firstorder;

import org.junit.jupiter.api.Test;

import java.util.List;
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
        assertAlphaEqual("forall x. forall y. forall z. m(m(x, y), z) = m(x, m(y, z))",
                "forall a. forall b. forall c. m(m(a, b), c) = m(a, m(b, c))");
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
        assertNotAlphaEqual("m(a, b) = c", "m(b, a) = c");
        assertNotAlphaEqual("P(m(a, b))", "P(ab)");
        assertNotAlphaEqual("P(f(a))", "P(f, a)");
        assertNotEquals("p", parser.parse("p"));
        assertNotEquals(null, parser.parse("forall x. P(x)"));
    }

    @Test
    void quantifiersDoNotCollideWithPredicatesNamedLikeTheirTag() {
        assertNotAlphaEqual("forall x. p(y)", "A(p(y))");
        assertNotAlphaEqual("exists x. f(c)", "E(f(c))");
        assertNotEquals(parser.parse("forall x. p(y)").hashCode(), parser.parse("A(p(y))").hashCode());
    }

    @Test
    void boundVariablesDoNotCollideWithFreeNames() {
        // the parser never reads '#0', but the constructors take any name
        FirstOrderOperation bound = new Forall("x", new Predicate("p", List.of(new VariableTerm("x"))));
        FirstOrderOperation free = new Forall("x", new Predicate("p", List.of(new VariableTerm("#0"))));
        assertNotEquals(bound, free);
        assertNotEquals(free, bound);
    }

    @Test
    void namesWithDelimitersDoNotCollide() {
        FirstOrderOperation one = new Predicate("p", List.of(new VariableTerm("x,y")));
        FirstOrderOperation two = new Predicate("p", List.of(new VariableTerm("x"), new VariableTerm("y")));
        assertNotEquals(one, two);
        assertNotEquals(two, one);
        assertNotEquals(new Predicate("p(x)"), new Predicate("p", List.of(new VariableTerm("x"))));
        assertNotEquals(new Predicate("p", List.of(new FunctionApplication("f(a", List.of(new VariableTerm("b"))))),
                new Predicate("p", List.of(new FunctionApplication("f", List.of(new VariableTerm("a,b"))))));
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
        String[] formulas = {"p & q", "p | q", "p -> q", "- p", "TRUE", "P(a, f(b))", "m(a, b) = c",
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
