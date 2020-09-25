package com.dan323.proof.generic.pattern;

import com.dan323.expresions.base.Conjunction;
import com.dan323.expresions.base.Constant;
import com.dan323.expresions.base.LogicOperation;
import com.dan323.expresions.base.Variable;
import org.junit.jupiter.api.Test;

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

    private enum ConstantStub implements Constant<LogicOperation> {

        FALSE, TRUE;

        ConstantStub() {
        }

        @Override
        public boolean isFalsehood() {
            return equals(FALSE);
        }
    }
}
