package com.dan323.expressions.firstorder;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A function symbol applied to one or more terms, such as {@code i(x)} or {@code f(x, y)}.
 *
 * @param name      the function symbol, a lowercase identifier
 * @param arguments the arguments, at least one
 */
public record FunctionApplication(String name, List<Term> arguments) implements Term {

    public FunctionApplication {
        Objects.requireNonNull(name);
        arguments = List.copyOf(arguments);
        if (arguments.isEmpty()) {
            throw new IllegalArgumentException("A function application needs at least one argument");
        }
    }

    @Override
    public Set<String> freeVariables() {
        Set<String> free = new HashSet<>();
        arguments.forEach(arg -> free.addAll(arg.freeVariables()));
        return Set.copyOf(free);
    }

    @Override
    public Term substitute(String variable, Term term) {
        return new FunctionApplication(name, arguments.stream().map(arg -> arg.substitute(variable, term)).toList());
    }

    @Override
    public String toString() {
        return name + arguments.stream().map(Term::toString).collect(Collectors.joining(", ", "(", ")"));
    }
}
