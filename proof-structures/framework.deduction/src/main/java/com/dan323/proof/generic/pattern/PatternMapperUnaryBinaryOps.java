package com.dan323.proof.generic.pattern;

import com.dan323.expressions.base.*;

import java.util.HashMap;
import java.util.Map;

public class PatternMapperUnaryBinaryOps implements PatternMapper<LogicOperation> {

    @Override
    public Map<String, LogicOperation> compareLogic(LogicOperation l1, LogicOperation l2) {
        if (l1 instanceof Constant && l2 instanceof Constant && l1.equals(l2)) {
            return new HashMap<>();
        } else if (l1 instanceof Variable) {
            Map<String, LogicOperation> sol = new HashMap<>();
            sol.put(l1.toString(), l2);
            return sol;
        } else if (l1 instanceof UnaryOperation && l1.getClass().equals(l2.getClass())) {
            return compareLogic(((UnaryOperation<?>) l1).getElement(), ((UnaryOperation<?>) l2).getElement());
        } else if (l1 instanceof BinaryOperation && l1.getClass().equals(l2.getClass())) {
            return checkBinaryOperation((BinaryOperation<?>) l1, (BinaryOperation<?>) l2);
        }
        return null;
    }

    private Map<String, LogicOperation> checkBinaryOperation(BinaryOperation<? extends LogicOperation> l1, BinaryOperation<? extends LogicOperation> l2) {
        Map<String, LogicOperation> m1 = compareLogic(l1.getLeft(), l2.getLeft());
        Map<String, LogicOperation> m2 = compareLogic(l1.getRight(), l2.getRight());
        for (Map.Entry<String, LogicOperation> key1 : m1.entrySet()) {
            if (m2.containsKey(key1.getKey()) && !key1.getValue().equals(m2.get(key1.getKey()))) {
                return null;
            }
        }
        m1.putAll(m2);
        return m1;
    }
}
