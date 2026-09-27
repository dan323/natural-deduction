package com.dan323.expressions.firstorder;

import java.util.Objects;
import java.util.Set;

/**
 * A name used as a term: a variable when a quantifier binds it, a constant (such as {@code e}) otherwise.
 *
 * @param name a lowercase identifier
 */
public record VariableTerm(String name) implements Term {

    public VariableTerm {
        Objects.requireNonNull(name);
    }

    @Override
    public Set<String> freeVariables() {
        return Set.of(name);
    }

    @Override
    public Term substitute(String variable, Term term) {
        return name.equals(variable) ? term : this;
    }

    @Override
    public String toString() {
        return name;
    }
}
