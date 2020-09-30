package com.dan323.proof.generic.proof;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public String getNameProof() {
        return nameProof;
    }

    public String toString() {
        return getNameProof() + (listLines.isEmpty() ? "" : (" " + listLines.toString()));
    }

    public static ProofReason parseReason(String ruleString, Map<String, Function<String, ProofReason>> extra) {
        ProofReason proofReason = switch (ruleString.substring(0, 3)) {
            case "Ass" -> new ProofReason("Ass", List.of());
            case "|I " -> new ProofReason("|I", Arrays.stream(parseArray(ruleString, 2)).boxed().collect(Collectors.toUnmodifiableList()));
            case "|E " -> new ProofReason("|E", Arrays.stream(parseArray(ruleString, 2)).boxed().collect(Collectors.toUnmodifiableList()));
            case "&I " -> new ProofReason("&I", Arrays.stream(parseArray(ruleString, 2)).boxed().collect(Collectors.toUnmodifiableList()));
            case "&E " -> new ProofReason("&E", Arrays.stream(parseArray(ruleString, 2)).boxed().collect(Collectors.toUnmodifiableList()));
            case "Rep" -> new ProofReason("Rep", Arrays.stream(parseArray(ruleString, 3)).boxed().collect(Collectors.toUnmodifiableList()));
            case "-E " -> new ProofReason("-E", Arrays.stream(parseArray(ruleString, 2)).boxed().collect(Collectors.toUnmodifiableList()));
            case "-I " -> new ProofReason("-I", List.of());
            case "->I" -> new ProofReason("->I", List.of());
            case "->E" -> new ProofReason("->E", Arrays.stream(parseArray(ruleString, 3)).boxed().collect(Collectors.toUnmodifiableList()));
            case "FE " -> new ProofReason("FE", Arrays.stream(parseArray(ruleString, 2)).boxed().collect(Collectors.toUnmodifiableList()));
            case "FI " -> new ProofReason("FI", Arrays.stream(parseArray(ruleString, 2)).boxed().collect(Collectors.toUnmodifiableList()));
            default -> null;
        };
        if (proofReason == null && extra.containsKey(ruleString.substring(0, 3))) {
            return Optional.ofNullable(extra.get(ruleString.substring(0, 3)).apply(ruleString)).orElseThrow(() -> new IllegalArgumentException("Not a valid rule"));
        } else {
            return proofReason;
        }
    }

    private static int[] parseArray(String proofReason, int reasonLength) {
        return Arrays.stream(proofReason.substring(reasonLength + 2, proofReason.length() - 1)
                .split(","))
                .map(String::trim)
                .mapToInt(Integer::parseInt)
                .toArray();
    }
}
