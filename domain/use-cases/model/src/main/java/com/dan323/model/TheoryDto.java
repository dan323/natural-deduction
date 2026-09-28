package com.dan323.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * A theory a logic offers: a named set of {@code premises} (its axioms) to start a proof from. The formulas are written
 * in the logic's input syntax, so a client can use them as premises as they are. {@code id} is stable and unique within
 * a logic.
 */
public record TheoryDto(String id, String name, List<String> premises) implements Serializable {

    @Serial
    private static final long serialVersionUID = 4417362096L;

    public TheoryDto {
        premises = premises == null ? List.of() : List.copyOf(premises);
    }
}
