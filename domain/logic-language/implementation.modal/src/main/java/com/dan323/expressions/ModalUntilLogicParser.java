package com.dan323.expressions;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.modal.Until;
import com.fathzer.soft.javaluator.Operator;
import com.fathzer.soft.javaluator.Tokenizer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The modal language plus {@link Until}, written {@code A U B} as {@link Until#toString()} prints it.
 * <p>
 * {@code U} binds tighter than the binary connectives ({@code ->}, {@code |}, {@code &}) and looser than the unary ones
 * ({@code -}, {@code []}, {@code <>}), and groups to the left: {@code - p U q -> r} is {@code ((- p) U q) -> r}.
 * <p>
 * javaluator cuts every token at every operator symbol, even inside a name, so a plain {@code U} operator would split
 * {@code TRUE} or {@code pUq}. Here {@code U} is an operator only as a word of its own (no letter, digit or {@code _}
 * next to it), and a variable cannot be called {@code U}.
 */
public class ModalUntilLogicParser extends ModalLogicParser {

    private static final String UNTIL_SYMBOL = "U";
    private static final Operator UNTIL = new Operator(UNTIL_SYMBOL, 2, Operator.Associativity.LEFT, 35);
    private static final Pattern UNTIL_WORD = Pattern.compile("(?<![\\p{L}\\p{N}_])U(?![\\p{L}\\p{N}_])");

    private final Tokenizer tokenizerWithoutUntil;

    public ModalUntilLogicParser() {
        super(List.of(UNTIL));
        var delimiters = Stream.concat(getOperators().stream().map(Operator::getSymbol), Stream.of("(", ")"))
                .filter(symbol -> !UNTIL_SYMBOL.equals(symbol))
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
        // The Tokenizer sorts the list it is given (longest symbol first), so it must be mutable.
        tokenizerWithoutUntil = new Tokenizer(delimiters);
    }

    @Override
    protected Iterator<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<>();
        var matcher = UNTIL_WORD.matcher(expression);
        int start = 0;
        while (matcher.find()) {
            tokenizerWithoutUntil.tokenize(expression.substring(start, matcher.start())).forEachRemaining(tokens::add);
            tokens.add(UNTIL_SYMBOL);
            start = matcher.end();
        }
        tokenizerWithoutUntil.tokenize(expression.substring(start)).forEachRemaining(tokens::add);
        return tokens.iterator();
    }

    @Override
    protected ModalOperation evaluate(Operator operator, Iterator<ModalOperation> operands, Object evaluationContext) {
        if (UNTIL_SYMBOL.equals(operator.getSymbol())) {
            return new Until((ModalLogicalOperation) operands.next(), (ModalLogicalOperation) operands.next());
        }
        return super.evaluate(operator, operands, evaluationContext);
    }
}
