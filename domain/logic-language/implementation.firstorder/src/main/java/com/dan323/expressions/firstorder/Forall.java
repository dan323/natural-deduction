package com.dan323.expressions.firstorder;

/**
 * {@code forall x. A}
 */
public final class Forall extends Quantifier {

    public static final String KEYWORD = "forall";

    public Forall(String variable, FirstOrderOperation body) {
        super(variable, body);
    }

    @Override
    public String getKeyword() {
        return KEYWORD;
    }

    @Override
    String getSymbol() {
        return "A";
    }

    @Override
    public Forall with(String variable, FirstOrderOperation body) {
        return new Forall(variable, body);
    }
}
