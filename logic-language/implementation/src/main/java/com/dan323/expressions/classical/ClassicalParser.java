package com.dan323.expressions.classical;

import com.fathzer.soft.javaluator.*;

import java.util.Iterator;
import java.util.List;

public final class ClassicalParser extends AbstractEvaluator<ClassicalLogicOperation> {

    private static final Operator AND = new Operator("&", 2, Operator.Associativity.LEFT, 1);
    private static final Operator OR = new Operator("|", 2, Operator.Associativity.LEFT, 2);
    private static final Operator IMP = new Operator("->", 2, Operator.Associativity.LEFT, 3);
    private static final Operator NEG = new Operator("-", 1, Operator.Associativity.LEFT, 4);

    public ClassicalParser() {
        super(buildParams());
    }

    private static Parameters buildParams() {
        Parameters parameters = new Parameters();
        parameters.add(new Constant("FALSE"));
        parameters.add(new Constant("TRUE"));
        parameters.addOperators(List.of(AND, OR, IMP, NEG));
        parameters.addExpressionBracket(BracketPair.PARENTHESES);
        return parameters;
    }

    @Override
    protected ClassicalLogicOperation evaluate(Constant constant, Object evaluationContext) {
        return constant.getName().equals("FALSE") ? ConstantClassic.FALSE : ConstantClassic.TRUE;
    }

    @Override
    protected ClassicalLogicOperation evaluate(Operator operator, Iterator<ClassicalLogicOperation> operands, Object evaluationContext) {
        return switch (operator.getSymbol()) {
            case "&" -> new ConjunctionClassic(operands.next(), operands.next());
            case "|" -> new DisjunctionClassic(operands.next(), operands.next());
            case "->" -> new ImplicationClassic(operands.next(), operands.next());
            case "-" -> new NegationClassic(operands.next());
            default -> throw new IllegalArgumentException("Invalid operator");
        };
    }

    @Override
    protected ClassicalLogicOperation toValue(String s, Object o) {
        if (s.trim().contains(" ")) {
            throw new IllegalArgumentException("The expression is not valid");
        } else {
            return new VariableClassic(s.trim());
        }
    }
}