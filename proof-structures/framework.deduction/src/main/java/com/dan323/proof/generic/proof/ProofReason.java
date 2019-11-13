package com.dan323.proof.generic.proof;

import java.util.List;

public class ProofReason {

    private final String nameProof;
    private final List<Integer> listLines;

    public ProofReason(String name, List<Integer> line) {
        nameProof = name;
        listLines = line;
    }

    public boolean equals(Object obj) {
        return (obj instanceof ProofReason) && obj.toString().equals(toString());
    }

    public String getNameProof() {
        return nameProof;
    }

    public String toString() {
        return getNameProof() + (listLines.isEmpty() ? "" : (" " + listLines.toString()));
    }
}
