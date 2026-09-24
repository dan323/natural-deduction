package com.dan323.proof.modal.nextuntil;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.Until;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Until introduction (now), {@code UI [i]}: from {@code B} in state {@code s}, derive {@code A U B} in state {@code s}
 * for any {@code A}.
 */
public final class ModalUntilI1 extends NextUntilRule {

    private final int line;
    private final ModalLogicalOperation left;

    public ModalUntilI1(int line, ModalLogicalOperation left) {
        super(ParseModalNextUntilAction.UNTIL, List.of(line));
        this.line = line;
        this.left = left;
    }

    @Override
    Optional<Conclusion> conclusion(ModalNaturalDeduction pf) {
        if (left == null) {
            return Optional.empty();
        }
        return formula(pf, line).map(right -> new Conclusion(new Until(left, right), state(pf, line)));
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && Objects.equals(((ModalUntilI1) obj).left, left);
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 7 + Objects.hashCode(left);
    }
}
