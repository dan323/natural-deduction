package com.dan323.expressions.relation;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A state of a logic with discrete time ({@code modal-next-until}): a state name, its {@code base}, followed by a
 * number of successor steps, its {@code offset}. {@code s0} is the base {@code s0} itself, {@code s0+1} is the
 * successor of {@code s0} and {@code s0+2} the successor of {@code s0+1}.
 * <p>
 * A term is written without spaces and with one {@code +k} at most ({@code s0+1+1} and {@code s0 + 2} are read as
 * {@code s0+2}, and {@code s0+0} as {@code s0}), so two terms name the same state exactly when they are written the
 * same. A base is a letter or {@code _} followed by letters, digits or {@code _}.
 *
 * @param base   the state the term counts from
 * @param offset how many successor steps after {@code base}, at least 0
 */
public record StateTerm(String base, int offset) {

    private static final Pattern BASE_PATTERN = Pattern.compile("[\\p{L}_][\\p{L}\\p{N}_]*");
    private static final Pattern STEP_PATTERN = Pattern.compile("\\d+");
    private static final String NOT_A_STATE = "' is not a state: expected a name such as s0, then +1, +2, ...";

    public StateTerm {
        Objects.requireNonNull(base);
        if (!BASE_PATTERN.matcher(base).matches()) {
            throw new IllegalArgumentException("'" + base + "' is not a state name");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("A state cannot come before its base");
        }
    }

    /**
     * @param text a state such as {@code s1} or {@code s0+2}
     * @return the state written as {@code text}
     * @throws IllegalArgumentException if {@code text} is not a state
     */
    public static StateTerm parse(String text) {
        if (text == null) {
            throw new IllegalArgumentException("A state is missing");
        }
        var parts = text.split("\\+", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].strip();
        }
        if (!BASE_PATTERN.matcher(parts[0]).matches()) {
            throw new IllegalArgumentException("'" + text + NOT_A_STATE);
        }
        int offset = 0;
        for (int i = 1; i < parts.length; i++) {
            if (!STEP_PATTERN.matcher(parts[i]).matches()) {
                throw new IllegalArgumentException("'" + text + NOT_A_STATE);
            }
            try {
                offset = Math.addExact(offset, Integer.parseInt(parts[i]));
            } catch (ArithmeticException | NumberFormatException e) {
                throw new IllegalArgumentException("'" + text + "' is too far after its base", e);
            }
        }
        return new StateTerm(parts[0], offset);
    }

    /**
     * @param text a state
     * @return how the state is written, e.g. {@code s0+2} for {@code s0 + 1 + 1}
     * @throws IllegalArgumentException if {@code text} is not a state
     */
    public static String normalize(String text) {
        return parse(text).toString();
    }

    /** @return whether this is a state name with no successor steps, such as {@code s1} */
    public boolean isBase() {
        return offset == 0;
    }

    /** @return whether {@link #successor()} can write the next state, i.e. the offset is below {@link Integer#MAX_VALUE} */
    public boolean hasSuccessor() {
        return offset < Integer.MAX_VALUE;
    }

    /**
     * @return the next state, {@code s+1}
     * @throws IllegalStateException if the offset is already {@link Integer#MAX_VALUE} (see {@link #hasSuccessor()})
     */
    public StateTerm successor() {
        if (!hasSuccessor()) {
            throw new IllegalStateException(this + " has no written successor");
        }
        return new StateTerm(base, offset + 1);
    }

    /**
     * @return the previous state, {@code s} for {@code s+1}
     * @throws IllegalStateException if this is a base, which has no written predecessor
     */
    public StateTerm predecessor() {
        if (isBase()) {
            throw new IllegalStateException(base + " has no written predecessor");
        }
        return new StateTerm(base, offset - 1);
    }

    @Override
    public String toString() {
        return offset == 0 ? base : base + "+" + offset;
    }
}
