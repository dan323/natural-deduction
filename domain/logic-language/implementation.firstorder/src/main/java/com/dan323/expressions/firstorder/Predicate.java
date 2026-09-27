package com.dan323.expressions.firstorder;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A predicate applied to terms, such as {@code P(x, e)}. Without arguments it is a propositional variable, printed as
 * its bare name.
 */
public final class Predicate implements FirstOrderOperation {

    private final String name;
    private final List<Term> arguments;

    public Predicate(String name, List<Term> arguments) {
        this.name = Objects.requireNonNull(name);
        this.arguments = List.copyOf(arguments);
    }

    public Predicate(String name) {
        this(name, List.of());
    }

    public String getName() {
        return name;
    }

    public List<Term> getArguments() {
        return arguments;
    }

    @Override
    public Set<String> freeVariables() {
        Set<String> free = new HashSet<>();
        arguments.forEach(arg -> free.addAll(arg.freeVariables()));
        return Set.copyOf(free);
    }

    @Override
    public Predicate substitute(String variable, Term term) {
        return new Predicate(name, arguments.stream().map(arg -> arg.substitute(variable, term)).toList());
    }

    @Override
    public String toString() {
        if (arguments.isEmpty()) {
            return name;
        }
        return name + arguments.stream().map(Term::toString).collect(Collectors.joining(", ", "(", ")"));
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
