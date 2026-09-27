package com.dan323.expressions.firstorder;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A formula that binds a variable in its body: {@link Forall} or {@link Exists}. It prints as
 * {@code keyword x. body}, the body without parentheses of its own, since it reaches as far right as it can.
 */
public abstract sealed class Quantifier implements FirstOrderOperation permits Forall, Exists {

    private final String variable;
    private final FirstOrderOperation body;

    protected Quantifier(String variable, FirstOrderOperation body) {
        this.variable = Objects.requireNonNull(variable);
        this.body = Objects.requireNonNull(body);
    }

    public String getVariable() {
        return variable;
    }

    public FirstOrderOperation getBody() {
        return body;
    }

    /**
     * @return the keyword the parser reads, {@code forall} or {@code exists}
     */
    public abstract String getKeyword();

    /**
     * @return a symbol that tells the kind of quantifier apart in the canonical form
     */
    abstract String getSymbol();

    /**
     * @param variable the bound variable
     * @param body     the body
     * @return a quantifier of the same kind binding {@code variable} in {@code body}
     */
    public abstract Quantifier with(String variable, FirstOrderOperation body);

    @Override
    public Set<String> freeVariables() {
        Set<String> free = new HashSet<>(body.freeVariables());
        free.remove(variable);
        return Set.copyOf(free);
    }

    @Override
    public Quantifier substitute(String name, Term term) {
        if (variable.equals(name) || !body.freeVariables().contains(name)) {
            return this;
        }
        Set<String> termVariables = term.freeVariables();
        if (!termVariables.contains(variable)) {
            return with(variable, body.substitute(name, term));
        }
        Set<String> avoid = new HashSet<>(termVariables);
        avoid.addAll(body.freeVariables());
        String renamed = Alpha.fresh(variable, avoid);
        FirstOrderOperation renamedBody = body.substitute(variable, new VariableTerm(renamed));
        return with(renamed, renamedBody.substitute(name, term));
    }

    @Override
    public String toString() {
        return getKeyword() + " " + variable + ". " + body;
    }

    @Override
    public boolean equals(Object obj) {
        return Alpha.equivalent(this, obj);
    }

    @Override
    public int hashCode() {
        return Alpha.hash(this);
    }
}
