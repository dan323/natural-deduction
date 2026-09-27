package com.dan323.expressions.firstorder;

/**
 * {@code exists x. A}
 */
public final class Exists extends Quantifier {

    public static final String KEYWORD = "exists";

    public Exists(String variable, FirstOrderOperation body) {
        super(variable, body);
    }

    @Override
    public String getKeyword() {
        return KEYWORD;
    }

    @Override
    String getSymbol() {
        return "E";
    }

    @Override
    public Exists with(String variable, FirstOrderOperation body) {
        return new Exists(variable, body);
    }
}
