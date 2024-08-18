package com.dan323.rest.model;

import com.dan323.model.ActionDto;
import com.dan323.model.ProofDto;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author danco
 */
public record ProofActionRequest(ActionDto actionDto, ProofDto proofDto) implements Serializable {

    @Serial
    private static final long serialVersionUID = 982309L;

}
