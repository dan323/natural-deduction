package com.dan323.proof;

import com.dan323.expresions.clasical.*;

import java.util.HashMap;
import java.util.Map;

public class PatternMapperUnaryBinaryOps implements PatternMapper<ClassicalLogicOperation> {

    @Override
    public Map<String, ClassicalLogicOperation> compareLogic(ClassicalLogicOperation l1, ClassicalLogicOperation l2) {
        if (l1 instanceof ConstantClassic && l2 instanceof ConstantClassic && l1.evaluate(null) == l2.evaluate(null)) {
            return new HashMap<>();
        }
        if (l1 instanceof VariableClassic) {
            Map<String, ClassicalLogicOperation> sol = new HashMap<>();
            sol.put(l1.toString(), l2);
            return sol;
        }
        if (l1 instanceof UnaryOperationClassic && l1.getClass().equals(l2.getClass())) {
            return compareLogic(((UnaryOperationClassic) l1).getElement(), ((UnaryOperationClassic) l2).getElement());
        }
        if (l1 instanceof BinaryOperationClassic && l1.getClass().equals(l2.getClass())) {
            return checkBinaryOperation((BinaryOperationClassic) l1, (BinaryOperationClassic) l2);
        }
        return null;
    }

    private Map<String, ClassicalLogicOperation> checkBinaryOperation(BinaryOperationClassic l1, BinaryOperationClassic l2) {
        Map<String, ClassicalLogicOperation> m1 = compareLogic(l1.getLeft(), l2.getLeft());
        Map<String, ClassicalLogicOperation> m2 = compareLogic(l1.getRight(), l2.getRight());
        for (Map.Entry<String, ClassicalLogicOperation> key1 : m1.entrySet()) {
            if (m2.containsKey(key1.getKey()) && !key1.getValue().equals(m2.get(key1))) {
                return null;
            }
        }
        m1.putAll(m2);
        return m1;
    }
}
