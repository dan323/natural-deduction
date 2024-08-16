package com.dan323.expressions.classical;

import com.fathzer.soft.javaluator.Operator;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    private final ClassicalParser classicalParser = new ClassicalParser();

    @Test
    public void andParseTest() {
        ClassicalLogicOperation log = classicalParser.evaluate("P & Q");
        assertInstanceOf(ConjunctionClassic.class, log);
        assertEquals(new VariableClassic("P"), ((ConjunctionClassic) log).getLeft());
        assertEquals(new VariableClassic("Q"), ((ConjunctionClassic) log).getRight());
    }

    @Test
    public void impParseTest() {
        ClassicalLogicOperation log = classicalParser.evaluate("P -> Q");
        assertInstanceOf(ImplicationClassic.class, log);
        assertEquals(new VariableClassic("P"), ((ImplicationClassic) log).getLeft());
        assertEquals(new VariableClassic("Q"), ((ImplicationClassic) log).getRight());
    }

    @Test
    public void orParseTest() {
        ClassicalLogicOperation log = classicalParser.evaluate("(P & Q) | K");
        assertInstanceOf(DisjunctionClassic.class, log);
        assertInstanceOf(ConjunctionClassic.class, ((DisjunctionClassic) log).getLeft());
    }

    @Test
    public void negParseTest() {
        ClassicalLogicOperation log = classicalParser.evaluate("- (P & Q)");
        assertInstanceOf(NegationClassic.class, log);
        assertInstanceOf(ConjunctionClassic.class, ((NegationClassic) log).getElement());
    }

    @Test
    public void errorParseTest() {
        assertThrows(IllegalArgumentException.class, () -> classicalParser.evaluate("P O Q"));
        Operator fake = new Operator("WOWO", 1, Operator.Associativity.LEFT, 100);
        Iterator<ClassicalLogicOperation> oneElement = List.<ClassicalLogicOperation>of(new VariableClassic("P")).iterator();
        assertThrows(IllegalArgumentException.class, () -> classicalParser.evaluate(fake, oneElement, null));
    }

    @Test
    public void constParseTest() {
        ClassicalLogicOperation log = classicalParser.evaluate("TRUE & FALSE");
        assertInstanceOf(ConjunctionClassic.class, log);
        assertEquals(ConstantClassic.TRUE, ((ConjunctionClassic) log).getLeft());
        assertEquals(ConstantClassic.FALSE, ((ConjunctionClassic) log).getRight());
    }
}
