package com.dan323.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author danco
 */
public record ProofActionRequest(ActionDto actionDto, ProofDto proofDto) implements Serializable {

    @Serial
    private static final long serialVersionUID = 982309L;

}
