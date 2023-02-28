package com.dan323.expressions;

import com.dan323.expressions.modal.*;
import com.dan323.expressions.relation.Equals;
import com.dan323.expressions.relation.LessEqual;
import com.fathzer.soft.javaluator.*;

import java.util.Iterator;
import java.util.List;

public class ModalLogicParser<T> extends AbstractEvaluator<ModalOperation> {

    private static final Operator AND = new Operator("&", 2, Operator.Associativity.LEFT, 1);
    private static final Operator OR = new Operator("|", 2, Operator.Associativity.LEFT, 2);
    private static final Operator IMP = new Operator("->", 2, Operator.Associativity.LEFT, 3);
    private static final Operator NEG = new Operator("-", 1, Operator.Associativity.LEFT, 4);
    private static final Operator ALW = new Operator("[]", 1, Operator.Associativity.LEFT, 5);
    private static final Operator SOM = new Operator("<>", 1, Operator.Associativity.LEFT, 6);
    private static final Operator LES = new Operator("<=", 2, Operator.Associativity.LEFT, 7);
    private static final Operator EQU = new Operator("=", 2, Operator.Associativity.LEFT, 8);
    private final java.util.function.Function<String, T> parser;

    public ModalLogicParser(java.util.function.Function<String, T> parser) {
        super(buildParams());
        this.parser = parser;
    }

    private static Parameters buildParams() {
        Parameters parameters = new Parameters();
        parameters.add(new Constant("FALSE"));
        parameters.add(new Constant("TRUE"));
        parameters.addOperators(List.of(AND, OR, IMP, NEG, ALW, SOM, LES, EQU));
        parameters.addExpressionBracket(BracketPair.PARENTHESES);
        return parameters;
    }

    @Override
    protected ModalOperation evaluate(Constant constant, Object evaluationContext) {
        return "FALSE".equals(constant.getName()) ? ConstantModal.FALSE : ConstantModal.TRUE;
    }

    @Override
    protected ModalOperation evaluate(Operator operator, Iterator<ModalOperation> operands, Object evaluationContext) {
        return switch (operator.getSymbol()) {
            case "&" -> new ConjunctionModal(operands.next(), operands.next());
            case "|" -> new DisjunctionModal(operands.next(), operands.next());
            case "->" -> new ImplicationModal(operands.next(), operands.next());
            case "-" -> new NegationModal(operands.next());
            case "[]" -> new Always((ModalLogicalOperation) operands.next());
            case "<>" -> new Sometime((ModalLogicalOperation) operands.next());
            case "<=" -> new LessEqual<>(parser.apply(operands.next().toString()), parser.apply(operands.next().toString()));
            case "=" -> new Equals<>(parser.apply(operands.next().toString()), parser.apply(operands.next().toString()));
            default -> throw new IllegalArgumentException("Invalid operator");
        };
    }

    @Override
    protected ModalLogicalOperation toValue(String s, Object o) {
        return new VariableModal(s.trim());
    }
}
