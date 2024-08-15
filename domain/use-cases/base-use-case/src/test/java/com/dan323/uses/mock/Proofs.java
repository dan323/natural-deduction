package com.dan323.uses.mock;

import com.dan323.model.ProofDto;

import java.util.List;

public final class Proofs {

    public static ProofDto genericProof(String logic) {
        return new ProofDto(List.of(), logic, "P");
    }

}
