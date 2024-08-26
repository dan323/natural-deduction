package com.dan323.proof.generic.proof;

import java.util.*;
import java.util.function.Function;

public class ProofReason {

    private final String nameProof;
    private final List<Integer> listLines;
    private final List<Range> ranges;

    public ProofReason(String name, List<Range> ranges, List<Integer> line) {
        this.nameProof = name;
        this.listLines = line;
        this.ranges = ranges;
    }

    public record Range(int start, int end) {
        @Override
        public String toString() {
            return start + "-" + end;
        }
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
        return getNameProof() + (printable().isEmpty() ? "" : (" " + printable()));
    }

    private List<Object> printable() {
        var printable = new ArrayList<Object>(listLines);
        printable.addAll(ranges);
        return printable;
    }

    public static ProofReason parseReason(String ruleString, Map<String, Function<String, ProofReason>> extra) {
        ProofReason proofReason = switch (ruleString.substring(0, 3)) {
            case "Ass" -> new ProofReason("Ass", List.of(), List.of());
            case "|I " -> new ProofReason("|I", List.of(), Arrays.stream(parseArray(ruleString, 2)).boxed().toList());
            case "|E " -> new ProofReason("|E", List.of(), Arrays.stream(parseArray(ruleString, 2)).boxed().toList());
            case "&I " -> new ProofReason("&I", List.of(), Arrays.stream(parseArray(ruleString, 2)).boxed().toList());
            case "&E " -> new ProofReason("&E", List.of(), Arrays.stream(parseArray(ruleString, 2)).boxed().toList());
            case "Rep" -> new ProofReason("Rep", List.of(), Arrays.stream(parseArray(ruleString, 3)).boxed().toList());
            case "-E " -> new ProofReason("-E", List.of(), Arrays.stream(parseArray(ruleString, 2)).boxed().toList());
            case "-I " -> {
                var lst =parseRange(ruleString, 2);
                yield new ProofReason("-I", List.of(new Range(lst[0], lst[1])), List.of());
            }
            case "->I" -> {
                var lst =parseRange(ruleString, 3);
                yield new ProofReason("->I", List.of(new Range(lst[0], lst[1])), List.of());
            }
            case "->E" -> new ProofReason("->E", List.of(), Arrays.stream(parseArray(ruleString, 3)).boxed().toList());
            case "FE " -> new ProofReason("FE", List.of(), Arrays.stream(parseArray(ruleString, 2)).boxed().toList());
            case "FI " -> new ProofReason("FI", List.of(), Arrays.stream(parseArray(ruleString, 2)).boxed().toList());
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

    private static int[] parseRange(String proofReason, int reasonLength) {
        return Arrays.stream(proofReason.substring(reasonLength + 2, proofReason.length() - 1)
                        .split("-"))
                .mapToInt(Integer::parseInt)
                .toArray();
    }
}
