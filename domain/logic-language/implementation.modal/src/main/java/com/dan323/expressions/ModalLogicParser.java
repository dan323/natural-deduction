package com.dan323.expressions;

import com.dan323.expressions.modal.*;
import com.dan323.expressions.relation.Equals;
import com.dan323.expressions.relation.LessEqual;
import com.fathzer.soft.javaluator.*;

import java.util.Iterator;
import java.util.List;

public class ModalLogicParser extends AbstractEvaluator<ModalOperation> {

    // Precedences go in steps of 10 (only their order matters) so that an extension can put an operator in between.

    private static final Operator AND = new Operator("&", 2, Operator.Associativity.LEFT, 10);
    private static final Operator OR = new Operator("|", 2, Operator.Associativity.LEFT, 20);
    private static final Operator IMP = new Operator("->", 2, Operator.Associativity.LEFT, 30);
    private static final Operator NEG = new Operator("-", 1, Operator.Associativity.LEFT, 40);
    private static final Operator ALW = new Operator("[]", 1, Operator.Associativity.LEFT, 50);
    private static final Operator SOM = new Operator("<>", 1, Operator.Associativity.LEFT, 60);
    private static final Operator LES = new Operator("<=", 2, Operator.Associativity.LEFT, 70);
    private static final Operator EQU = new Operator("=", 2, Operator.Associativity.LEFT, 80);

    public ModalLogicParser() {
        this(List.of());
    }

    /**
     * A parser for a language that extends this one: it also reads {@code extraOperators}, which a subclass builds in
     * its own {@link #evaluate(Operator, Iterator, Object)} before falling back to this one.
     *
     * @param extraOperators the operators that the subclass adds
     */
    protected ModalLogicParser(List<Operator> extraOperators) {
        super(buildParams(extraOperators));
    }

    private static Parameters buildParams(List<Operator> extraOperators) {
        Parameters parameters = new Parameters();
        parameters.add(new Constant("FALSE"));
        parameters.add(new Constant("TRUE"));
        parameters.addOperators(List.of(AND, OR, IMP, NEG, ALW, SOM, LES, EQU));
        parameters.addOperators(extraOperators);
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
            case "|" -> new DisjunctionModal((ModalLogicalOperation) operands.next(), (ModalLogicalOperation) operands.next());
            case "->" -> new ImplicationModal((ModalLogicalOperation) operands.next(), (ModalLogicalOperation) operands.next());
            case "-" -> new NegationModal((ModalLogicalOperation) operands.next());
            case "[]" -> new Always((ModalLogicalOperation) operands.next());
            case "<>" -> new Sometime((ModalLogicalOperation) operands.next());
            case "<=" -> new LessEqual(operands.next().toString(), operands.next().toString());
            case "=" -> new Equals(operands.next().toString(), operands.next().toString());
            default -> throw new IllegalArgumentException("Invalid operator");
        };
    }

    @Override
    protected ModalLogicalOperation toValue(String s, Object o) {
        return new VariableModal(s.trim());
    }
}
