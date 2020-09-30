package com.dan323.proof.generic.pattern;

import com.dan323.expressions.base.Conjunction;
import com.dan323.expressions.base.Constant;
import com.dan323.expressions.base.LogicOperation;
import com.dan323.expressions.base.Negation;
import com.dan323.expressions.base.Variable;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PatternMapperTest {

    @Test
    public void constantPatternTest() {
        var f = ConstantStub.FALSE;
        var v = ConstantStub.TRUE;
        PatternMapper<LogicOperation> patternMapper = new PatternMapperUnaryBinaryOps();
        assertNull(patternMapper.compareLogic(v, f));
        assertTrue(patternMapper.compareLogic(v, v).isEmpty());
    }

    @Test
    public void variablePatternTest() {
        var p = new Variable("P") {
        };
        var f = ConstantStub.FALSE;
        PatternMapper<LogicOperation> patternMapper = new PatternMapperUnaryBinaryOps();
        assertNull(patternMapper.compareLogic(f, p));
        assertEquals(f, patternMapper.compareLogic(p, f).get("P"));
    }

    @Test
    public void example1Test() {
        var p = new Variable("P") {
        };
        var q = new Variable("Q") {
        };
        var r = new Variable("R") {
        };
        var pq = new Conjunction<LogicOperation>(p, q) {
        };
        PatternMapper<LogicOperation> patternMapper = new PatternMapperUnaryBinaryOps();
        assertEquals(patternMapper.compareLogic(r, pq).get("R"), pq);
    }

    @Test
    public void example2Test() {
        var p = new Variable("P") {
        };
        var q = new Variable("Q") {
        };
        var r = new Variable("R") {
        };
        PatternMapper<LogicOperation> patternMapper = new PatternMapperUnaryBinaryOps();
        Map<String, LogicOperation> solution = patternMapper.compareLogic(new ConjunctionStub(p, q), new ConjunctionStub(q, r));
        assertEquals(solution.get("P"), q);
        assertEquals(solution.get("Q"), r);
    }

    @Test
    public void exampl3Test() {
        var p = new Variable("P") {
        };
        var q = new Variable("Q") {
        };
        PatternMapper<LogicOperation> patternMapper = new PatternMapperUnaryBinaryOps();
        Map<String, LogicOperation> solution = patternMapper.compareLogic(new NegationStub(p), new NegationStub(q));
        assertEquals(solution.get("P"), q);
    }

    private enum ConstantStub implements Constant {

        FALSE, TRUE;

        ConstantStub() {
        }

        @Override
        public boolean isFalsehood() {
            return equals(FALSE);
        }
    }

    private static class ConjunctionStub extends Conjunction<LogicOperation> {

        public ConjunctionStub(LogicOperation left, LogicOperation right) {
            super(left, right);
        }
    }

    private static class NegationStub extends Negation<LogicOperation> {

        public NegationStub(LogicOperation element) {
            super(element);
        }
    }
}
