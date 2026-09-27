package com.dan323.expressions.firstorder;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SubstitutionTest {

    private final FirstOrderParser parser = new FirstOrderParser();

    private static VariableTerm v(String name) {
        return new VariableTerm(name);
    }

    @Test
    void substitutionAvoidsCapture() {
        FirstOrderOperation substituted = parser.parse("forall y. x = y").substitute("x", v("y"));
        Forall forall = assertInstanceOf(Forall.class, substituted);
        assertNotEquals("y", forall.getVariable());
        assertEquals("forall y1. y = y1", substituted.toString());
        assertEquals(Set.of("y"), substituted.freeVariables());
        assertNotEquals(parser.parse("forall y. y = y"), substituted);
        assertEquals(parser.parse("forall z. y = z"), substituted);
    }

    @Test
    void renamedVariableAvoidsTheBodyAndTheTerm() {
        // y1 is free in the body and y2 in the term, so the bound y becomes y3
        FirstOrderOperation substituted = parser.parse("exists y. P(x, y, y1)")
                .substitute("x", parser.parseTerm("f(y, y2)"));
        assertEquals("exists y3. P(f(y, y2), y3, y1)", substituted.toString());
        // trailing digits of the bound name are replaced, not appended to
        assertEquals("forall y1. y2 = y1",
                parser.parse("forall y2. x = y2").substitute("x", v("y2")).toString());
    }

    @Test
    void boundOccurrencesAreNotReplaced() {
        FirstOrderOperation formula = parser.parse("forall x. P(x)");
        assertSame(formula, formula.substitute("x", v("a")));
        FirstOrderOperation mixed = parser.parse("P(x) & (forall x. Q(x))");
        assertEquals(parser.parse("P(a) & (forall x. Q(x))"), mixed.substitute("x", v("a")));
        assertEquals("P(a) & (forall x. Q(x))", mixed.substitute("x", v("a")).toString());
    }

    @Test
    void substitutionWithoutCaptureKeepsTheBinder() {
        FirstOrderOperation substituted = parser.parse("forall y. m(x, y) = m(y, x)")
                .substitute("x", parser.parseTerm("i(z)"));
        assertEquals("forall y. m(i(z), y) = m(y, i(z))", substituted.toString());
        FirstOrderOperation untouched = parser.parse("forall y. P(y)");
        assertSame(untouched, untouched.substitute("x", v("y")));
    }

    @Test
    void substitutionReachesEveryConnective() {
        FirstOrderOperation formula = parser.parse("- P(x) | (x = e -> Q(f(x))) & TRUE");
        FirstOrderOperation substituted = formula.substitute("x", parser.parseTerm("m(a, b)"));
        assertEquals(parser.parse("- P(m(a, b)) | (m(a, b) = e -> Q(f(m(a, b)))) & TRUE"), substituted);
        assertEquals(Set.of("a", "b", "e"), substituted.freeVariables());
        assertSame(ConstantFirstOrder.FALSE, ConstantFirstOrder.FALSE.substitute("x", v("a")));
    }

    @Test
    void termSubstitution() {
        Term term = parser.parseTerm("m(m(x, y), f(x, e))");
        assertEquals(parser.parseTerm("m(m(i(y), y), f(i(y), e))"), term.substitute("x", parser.parseTerm("i(y)")));
        assertEquals(Set.of("x", "y", "e"), term.freeVariables());
        assertEquals(v("z"), v("z").substitute("x", v("a")));
    }

    @Test
    void freeVariables() {
        assertEquals(Set.of("e"), parser.parse("forall x. m(e, x) = x & m(x, e) = x").freeVariables());
        assertEquals(Set.of("x", "y"), parser.parse("P(x) & (forall x. R(x, y))").freeVariables());
        assertEquals(Set.of(), parser.parse("p -> q").freeVariables());
        assertEquals(Set.of(), ConstantFirstOrder.TRUE.freeVariables());
        assertEquals(Set.of("x"), parser.parse("- (exists y. y = x)").freeVariables());
    }

    @Test
    void functionApplicationNeedsArguments() {
        List<Term> none = List.of();
        assertThrows(IllegalArgumentException.class, () -> new FunctionApplication("f", none));
    }
}
