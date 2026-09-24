package com.dan323.expressions;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.modal.Next;
import com.dan323.expressions.modal.Until;
import com.dan323.expressions.modal.VariableModal;
import com.dan323.expressions.relation.Equals;
import com.dan323.expressions.relation.LessEqual;
import com.dan323.expressions.relation.StateTerm;
import com.fathzer.soft.javaluator.BracketPair;
import com.fathzer.soft.javaluator.Constant;
import com.fathzer.soft.javaluator.Operator;
import com.fathzer.soft.javaluator.Parameters;
import com.fathzer.soft.javaluator.Tokenizer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The language of {@code modal-next-until}: the modal language plus Next, {@code X A} ({@link Next}), and Until,
 * {@code A U B} ({@link Until}), written as their {@code toString()} prints them.
 * <p>
 * {@code X} is a unary connective like {@code -}, {@code []} and {@code <>}, and here any unary connective can follow
 * any other ({@code X - p}, {@code [] X p}). {@code U} binds tighter than the binary connectives ({@code ->}, {@code |},
 * {@code &}) and looser than the unary ones, and groups to the left: {@code - p U X q -> r} is
 * {@code ((- p) U (X q)) -> r}. The other connectives bind as in the modal parser.
 * <p>
 * javaluator cuts every token at every operator symbol, even inside a name, so a plain {@code U} or {@code X} operator
 * would split {@code TRUE} or {@code Xp}. Here they are operators only as words of their own (no letter, digit or
 * {@code _} next to them), so {@code TRUE}, {@code Up}, {@code pUq} and {@code Xp} stay names, and no variable can be
 * called {@code U} or {@code X}.
 * <p>
 * The sides of a relation ({@code s0 <= s0+1}) must be states ({@link StateTerm}), and are kept the way
 * {@link StateTerm#normalize} writes them.
 */
public class ModalNextUntilLogicParser extends ModalLogicParser {

    private static final String UNTIL_SYMBOL = "U";
    private static final String NEXT_SYMBOL = "X";
    // Only the order of the precedences matters. The unary connectives share one, so that any of them can follow
    // another (the modal parser reads "- [] p" but not "[] - p").
    private static final int UNARY = 5;
    private static final List<Operator> OPERATORS = List.of(
            new Operator("&", 2, Operator.Associativity.LEFT, 1),
            new Operator("|", 2, Operator.Associativity.LEFT, 2),
            new Operator("->", 2, Operator.Associativity.LEFT, 3),
            new Operator(UNTIL_SYMBOL, 2, Operator.Associativity.LEFT, 4),
            new Operator("-", 1, Operator.Associativity.RIGHT, UNARY),
            new Operator("[]", 1, Operator.Associativity.RIGHT, UNARY),
            new Operator("<>", 1, Operator.Associativity.RIGHT, UNARY),
            new Operator(NEXT_SYMBOL, 1, Operator.Associativity.RIGHT, UNARY),
            new Operator("<=", 2, Operator.Associativity.LEFT, 7),
            new Operator("=", 2, Operator.Associativity.LEFT, 8));
    private static final Set<String> WORD_OPERATORS = Set.of(UNTIL_SYMBOL, NEXT_SYMBOL);
    private static final Pattern WORD_OPERATOR = Pattern.compile("(?<![\\p{L}\\p{N}_])[UX](?![\\p{L}\\p{N}_])");

    private final Tokenizer tokenizerWithoutWords;

    public ModalNextUntilLogicParser() {
        super(buildParams());
        var delimiters = Stream.concat(getOperators().stream().map(Operator::getSymbol), Stream.of("(", ")"))
                .filter(symbol -> !WORD_OPERATORS.contains(symbol))
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
        // The Tokenizer sorts the list it is given (longest symbol first), so it must be mutable.
        tokenizerWithoutWords = new Tokenizer(delimiters);
    }

    private static Parameters buildParams() {
        Parameters parameters = new Parameters();
        parameters.add(new Constant("FALSE"));
        parameters.add(new Constant("TRUE"));
        parameters.addOperators(OPERATORS);
        parameters.addExpressionBracket(BracketPair.PARENTHESES);
        return parameters;
    }

    @Override
    protected Iterator<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<>();
        var matcher = WORD_OPERATOR.matcher(expression);
        int start = 0;
        while (matcher.find()) {
            tokenizerWithoutWords.tokenize(expression.substring(start, matcher.start())).forEachRemaining(tokens::add);
            tokens.add(matcher.group());
            start = matcher.end();
        }
        tokenizerWithoutWords.tokenize(expression.substring(start)).forEachRemaining(tokens::add);
        return tokens.iterator();
    }

    @Override
    protected ModalOperation evaluate(Operator operator, Iterator<ModalOperation> operands, Object evaluationContext) {
        return switch (operator.getSymbol()) {
            case UNTIL_SYMBOL -> new Until(formula(operands.next()), formula(operands.next()));
            case NEXT_SYMBOL -> new Next(formula(operands.next()));
            case "<=" -> new LessEqual(state(operands.next()), state(operands.next()));
            case "=" -> new Equals(state(operands.next()), state(operands.next()));
            default -> super.evaluate(operator, operands, evaluationContext);
        };
    }

    private static ModalLogicalOperation formula(ModalOperation operand) {
        if (operand instanceof ModalLogicalOperation formula) {
            return formula;
        }
        throw new IllegalArgumentException("'" + operand + "' is a relation, and X and U take formulas");
    }

    private static String state(ModalOperation operand) {
        if (operand instanceof VariableModal variable) {
            return StateTerm.normalize(variable.toString());
        }
        throw new IllegalArgumentException("'" + operand + "' is not a state: the sides of a relation are states such as s0 or s0+1");
    }
}
