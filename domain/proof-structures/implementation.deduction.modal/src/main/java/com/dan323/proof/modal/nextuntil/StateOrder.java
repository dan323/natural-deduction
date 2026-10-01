package com.dan323.proof.modal.nextuntil;

import com.dan323.expressions.relation.Equals;
import com.dan323.expressions.relation.LessEqual;
import com.dan323.expressions.relation.RelationOperation;
import com.dan323.expressions.relation.StateTerm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * What a set of relations between states says about their order, in linear discrete time: every state is a natural
 * number, {@code s+k} is {@code k} after {@code s}, and {@code u <= v} says that {@code u} is not after {@code v}.
 * <p>
 * A relation {@code a+i <= b+j} is the difference constraint {@code a - b <= j - i} on the bases, so the relations are
 * a weighted graph with an edge from {@code b} to {@code a} of weight {@code j - i} for each of them ({@code a = b} gives
 * both {@code a <= b} and {@code b <= a}). {@code x+p <= y+q} follows exactly when the shortest path from {@code y} to
 * {@code x} weighs at most {@code q - p} (the same base counts as a path of weight 0), and the relations contradict each
 * other exactly when the graph has a cycle of negative weight. For difference constraints the integer answer is the
 * rational one, so these are the consequences in the naturals too.
 * <p>
 * Each relation comes with the line it is on, so that an entailment or a contradiction can say which lines it uses.
 */
public final class StateOrder {

    private record Edge(String from, String to, int weight, int line) {
    }

    private final List<Edge> edges = new ArrayList<>();
    private final Set<String> nodes = new LinkedHashSet<>();
    /** The weights of the lightest paths between bases, computed on the first question; null means no path. */
    private Map<String, Map<String, Integer>> distances;
    /** The states read so far, since reading one is the costly part of a question. */
    private final Map<String, Optional<StateTerm>> terms = new HashMap<>();
    private Optional<SortedSet<Integer>> contradiction = Optional.empty();
    private boolean contradictionSearched;

    /**
     * @param relations the relations, each with the 1-based line it is on; relations whose sides are not state terms
     *                  are left out
     */
    public StateOrder(Map<Integer, RelationOperation> relations) {
        relations.forEach((line, relation) -> {
            var left = term(relation.getLeft());
            var right = term(relation.getRight());
            if (left.isEmpty() || right.isEmpty()) {
                return;
            }
            if (relation instanceof LessEqual) {
                add(left.get(), right.get(), line);
            } else if (relation instanceof Equals) {
                add(left.get(), right.get(), line);
                add(right.get(), left.get(), line);
            }
        });
    }

    /** {@code a+i <= b+j}: {@code a - b <= j - i}, an edge from {@code b} to {@code a}. */
    private void add(StateTerm smaller, StateTerm greater, int line) {
        nodes.add(smaller.base());
        nodes.add(greater.base());
        edges.add(new Edge(greater.base(), smaller.base(), greater.offset() - smaller.offset(), line));
    }

    static Optional<StateTerm> term(String state) {
        try {
            return Optional.of(StateTerm.parse(state));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * @return whether the relations give {@code u <= v} (a quick answer for many questions; {@link #entails} also says
     * which lines give it). When the relations contradict each other, the answer may be either.
     */
    public boolean holds(String u, String v) {
        var x = terms.computeIfAbsent(u, StateOrder::term);
        var y = terms.computeIfAbsent(v, StateOrder::term);
        if (x.isEmpty() || y.isEmpty()) {
            return false;
        }
        int bound = y.get().offset() - x.get().offset();
        if (x.get().base().equals(y.get().base()) && bound >= 0) {
            return true;
        }
        Integer distance = distances().getOrDefault(y.get().base(), Map.of()).get(x.get().base());
        return distance != null && distance <= bound;
    }

    /**
     * @return whether the relations make two different bases the same state (a path of weight 0 each way between
     * them), which is when a formula may need to be carried from one to the other
     */
    public boolean hasEqualStates() {
        var all = distances();
        for (String a : nodes) {
            for (String b : nodes) {
                if (!a.equals(b) && all.get(a).containsKey(b) && all.get(b).containsKey(a)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Floyd-Warshall on the bases. */
    private Map<String, Map<String, Integer>> distances() {
        if (distances == null) {
            distances = new HashMap<>();
            for (String node : nodes) {
                distances.put(node, new HashMap<>());
            }
            for (Edge edge : edges) {
                distances.get(edge.from()).merge(edge.to(), edge.weight(), Math::min);
            }
            for (String through : nodes) {
                for (String from : nodes) {
                    shortenThrough(from, through);
                }
            }
        }
        return distances;
    }

    /** Make the paths from {@code from} go through {@code through} where that is lighter. */
    private void shortenThrough(String from, String through) {
        Integer first = distances.get(from).get(through);
        if (first == null) {
            return;
        }
        Map.copyOf(distances.get(through)).forEach((to, second) -> distances.get(from).merge(to, first + second, Math::min));
    }

    /**
     * @return the lines whose relations give {@code u <= v}, empty when they do not give it (or when {@code u} or
     * {@code v} is not a state term); an empty set of lines when it holds whatever the relations say, as
     * {@code s0 <= s0+1} does
     */
    public Optional<SortedSet<Integer>> entails(String u, String v) {
        var x = term(u);
        var y = term(v);
        if (x.isEmpty() || y.isEmpty()) {
            return Optional.empty();
        }
        int bound = y.get().offset() - x.get().offset();
        if (x.get().base().equals(y.get().base()) && bound >= 0) {
            return Optional.of(new TreeSet<>());
        }
        return shortestPath(y.get().base(), x.get().base())
                .filter(path -> weight(path) <= bound)
                .map(StateOrder::lines);
    }

    /**
     * @return the lines whose relations give both {@code first <= second} and {@code second <= first}, so that the two
     * are the same state; empty when they do not
     */
    public Optional<SortedSet<Integer>> sameState(String first, String second) {
        var forth = entails(first, second);
        var back = entails(second, first);
        if (forth.isEmpty() || back.isEmpty()) {
            return Optional.empty();
        }
        SortedSet<Integer> lines = new TreeSet<>(forth.get());
        lines.addAll(back.get());
        return Optional.of(lines);
    }

    /** @return the lines of relations that contradict each other (a cycle of negative weight), empty when they do not */
    public Optional<SortedSet<Integer>> contradiction() {
        if (!contradictionSearched) {
            contradiction = findContradiction();
            contradictionSearched = true;
        }
        return contradiction;
    }

    private Optional<SortedSet<Integer>> findContradiction() {
        // Bellman-Ford from a virtual source joined to every node with weight 0
        Map<String, Integer> distance = new HashMap<>();
        Map<String, Edge> previous = new HashMap<>();
        nodes.forEach(node -> distance.put(node, 0));
        String changed = null;
        for (int i = 0; i <= nodes.size(); i++) {
            changed = null;
            for (Edge edge : edges) {
                if (distance.get(edge.from()) + edge.weight() < distance.get(edge.to())) {
                    distance.put(edge.to(), distance.get(edge.from()) + edge.weight());
                    previous.put(edge.to(), edge);
                    changed = edge.to();
                }
            }
            if (changed == null) {
                return Optional.empty();
            }
        }
        // A node relaxed in the last round leads back into a negative cycle: walk back into the cycle, then around it
        String node = changed;
        for (int i = 0; i < nodes.size() && previous.containsKey(node); i++) {
            node = previous.get(node).from();
        }
        SortedSet<Integer> lines = new TreeSet<>();
        String start = node;
        do {
            Edge edge = previous.get(node);
            if (edge == null) {
                // Not reached in a well-formed run: report every relation rather than a wrong cycle
                edges.forEach(e -> lines.add(e.line()));
                return Optional.of(lines);
            }
            lines.add(edge.line());
            node = edge.from();
        } while (!node.equals(start));
        return Optional.of(lines);
    }

    /** The lightest path from {@code from} to {@code to}, when there is one and no negative cycle is on the way. */
    private Optional<List<Edge>> shortestPath(String from, String to) {
        if (!nodes.contains(from) || !nodes.contains(to) || to.equals(from)) {
            // A path from a base to itself only matters through a negative cycle, which contradiction() reports
            return Optional.empty();
        }
        var previous = lightestEdgesFrom(from);
        if (!previous.containsKey(to)) {
            return Optional.empty();
        }
        List<Edge> path = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        String node = to;
        while (!node.equals(from)) {
            if (!seen.add(node)) {
                // A negative cycle: the relations contradict each other, which contradiction() reports instead
                return Optional.empty();
            }
            Edge edge = previous.get(node);
            path.add(edge);
            node = edge.from();
        }
        return Optional.of(path);
    }

    /** Bellman-Ford from {@code from}: for each node reached, the last edge of the lightest path to it. */
    private Map<String, Edge> lightestEdgesFrom(String from) {
        Map<String, Integer> distance = new HashMap<>();
        Map<String, Edge> previous = new HashMap<>();
        distance.put(from, 0);
        boolean relaxed = true;
        for (int i = 0; i < nodes.size() && relaxed; i++) {
            relaxed = false;
            for (Edge edge : edges) {
                Integer start = distance.get(edge.from());
                Integer end = distance.get(edge.to());
                if (start != null && (end == null || start + edge.weight() < end)) {
                    distance.put(edge.to(), start + edge.weight());
                    previous.put(edge.to(), edge);
                    relaxed = true;
                }
            }
        }
        return previous;
    }

    private static int weight(List<Edge> path) {
        return path.stream().mapToInt(Edge::weight).sum();
    }

    private static SortedSet<Integer> lines(List<Edge> path) {
        SortedSet<Integer> lines = new TreeSet<>();
        path.forEach(edge -> lines.add(edge.line()));
        return lines;
    }
}
