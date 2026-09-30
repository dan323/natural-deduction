package com.dan323.proof.modal.nextuntil;

import com.dan323.expressions.modal.ConstantModal;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.relation.Equals;
import com.dan323.expressions.relation.LessEqual;
import com.dan323.expressions.relation.RelationOperation;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.modal.AbstractModalAction;
import com.dan323.proof.modal.ModalAssume;
import com.dan323.proof.modal.ModalBoxI;
import com.dan323.proof.modal.ModalFI;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The rules of linear time in {@code modal-next-until}: {@code Ord}, {@code Eq}, {@code Lin}, {@code -U} and the witness
 * of an Until ({@code UW}, {@code UB}, {@code UA}), and the order of states they reason with ({@link StateOrder}).
 */
class LinearTimeRulesTest {

    private static ModalOperation parse(String formula) {
        return ParseModalNextUntilAction.parseExpression(formula);
    }

    private static ModalLogicalOperation formula(String formula) {
        return (ModalLogicalOperation) parse(formula);
    }

    private static ModalNextUntilNaturalDeduction proof(String... premises) {
        var proof = new ModalNextUntilNaturalDeduction("s0");
        proof.initializeProof(List.of(premises).stream().map(LinearTimeRulesTest::parse).toList(), parse("p"));
        return proof;
    }

    private static void assume(ModalNaturalDeduction proof, String formula, String state) {
        new ModalAssume(formula(formula), state).apply(proof);
    }

    private static void assumeRelation(ModalNaturalDeduction proof, String relation) {
        new ModalAssume((RelationOperation) parse(relation)).apply(proof);
    }

    /** Adds {@code formula} in {@code state} to the current subproof, as if a rule derived it. */
    private static void line(ModalNaturalDeduction proof, String formula, String state) {
        proof.getSteps().add(new ProofStepModal(state, proof.getSteps().getLast().getAssumptionLevel(), formula(formula),
                new ProofReason("TST", List.of(), List.of())));
    }

    private static void applies(ModalNaturalDeduction proof, AbstractModalAction action, String result, String state) {
        assertTrue(action.isValid(proof), action.getClass().getSimpleName());
        action.apply(proof);
        var last = proof.getSteps().getLast();
        assertEquals(parse(result), last.getStep());
        assertEquals(state, last.getState());
    }

    private static StateOrder order(String... relations) {
        Map<Integer, RelationOperation> map = new LinkedHashMap<>();
        for (int i = 0; i < relations.length; i++) {
            map.put(i + 1, (RelationOperation) parse(relations[i]));
        }
        return new StateOrder(map);
    }

    private static SortedSet<Integer> lines(Integer... lines) {
        return new TreeSet<>(List.of(lines));
    }

    // ---------------------------------------------------------------- StateOrder

    @Test
    void theOrderFollowsChainsOfRelations() {
        var order = order("s0 <= s1", "s1 <= s2+1", "s5 <= s6");

        assertEquals(lines(1, 2), order.entails("s0", "s2+1").orElseThrow());
        assertEquals(lines(1), order.entails("s0+1", "s1+1").orElseThrow(), "linear time: s0 <= s1 gives s0+1 <= s1+1");
        assertEquals(lines(), order.entails("s0", "s0+2").orElseThrow(), "the same base holds whatever the relations");
        assertTrue(order.entails("s0+1", "s0").isEmpty());
        assertTrue(order.entails("s1", "s0").isEmpty());
        assertTrue(order.entails("s0", "s5").isEmpty(), "s0 and s5 are not related");
        assertTrue(order.entails("s0", "s7").isEmpty(), "s7 is in no relation");
        assertTrue(order.entails("s 0", "s1").isEmpty(), "not a state");
        assertTrue(order.holds("s0+1", "s1+1"));
        assertFalse(order.holds("s1", "s0"));
        assertFalse(order.holds("s 0", "s1"));
        assertTrue(order.contradiction().isEmpty());
        assertFalse(order.hasEqualStates());
    }

    @Test
    void theOrderFindsEqualAndContradictoryStates() {
        var equal = order("s0 <= s1", "s1 <= s0");
        assertEquals(lines(1, 2), equal.equal("s0", "s1").orElseThrow());
        assertEquals(lines(1, 2), equal.equal("s0+1", "s1+1").orElseThrow());
        assertTrue(equal.hasEqualStates());
        assertTrue(equal.contradiction().isEmpty());

        var contradiction = order("s0 <= s1", "s1+1 <= s0", "s0 <= s2");
        assertEquals(lines(1, 2), contradiction.contradiction().orElseThrow(), "s1+1 <= s1: only the lines of the cycle");
        assertEquals(lines(1), order("s0+1 <= s0").contradiction().orElseThrow());
    }

    @Test
    void anEqualityIsTwoRelations() {
        var order = new StateOrder(Map.of(1, new Equals("s0", "s1+1")));

        assertTrue(order.holds("s0", "s1+1"));
        assertTrue(order.holds("s1+1", "s0"));
        assertTrue(order.equal("s0+1", "s1+2").isPresent());
    }

    // ---------------------------------------------------------------- Ord

    @Test
    void orderDerivesWhatTheRelationsGive() {
        var proof = proof();
        assumeRelation(proof, "s0 <= s1");
        assumeRelation(proof, "s1 <= s2");

        var rule = new ModalOrder(parse("s0+1 <= s2+1"));
        assertTrue(rule.isValid(proof));
        rule.apply(proof);
        assertEquals(parse("s0+1 <= s2+1"), proof.getSteps().getLast().getStep());
        assertEquals("Ord [1, 2]", proof.getSteps().getLast().getProof().toString());

        new ModalOrder(parse("s0 <= s0+2")).apply(proof);
        assertEquals("Ord", proof.getSteps().getLast().getProof().toString(), "no relation is needed");

        assertFalse(new ModalOrder(parse("s2 <= s0")).isValid(proof));
        assertFalse(new ModalOrder(parse("FALSE")).isValid(proof), "the relations agree");
        assertFalse(new ModalOrder(parse("p")).isValid(proof), "not a relation");
        assertFalse(new ModalOrder(List.of(1), parse("s0 <= s2")).isValid(proof), "line 2 is needed too");
        assertTrue(new ModalOrder(List.of(1, 2), parse("s0 <= s2")).isValid(proof));
        assertFalse(new ModalOrder(List.of(9), parse("s0 <= s0")).isValid(proof), "there is no line 9");
    }

    @Test
    void orderDerivesFalseFromContradictoryRelations() {
        var proof = proof();
        assumeRelation(proof, "s1+1 <= s0");
        assumeRelation(proof, "s0 <= s1");

        applies(proof, new ModalOrder(ConstantModal.FALSE), "FALSE", "s0");
        assertEquals("Ord [1, 2]", proof.getSteps().getLast().getProof().toString());
        assertFalse(new ModalOrder(List.of(3), parse("s0 <= s0")).isValid(new ModalNextUntilNaturalDeduction("s0")), "no lines");
    }

    // ---------------------------------------------------------------- Eq

    @Test
    void equalStatesShareTheirFormulas() {
        var proof = proof("p");
        assumeRelation(proof, "s0 <= s1");
        assumeRelation(proof, "s1 <= s0");

        applies(proof, new ModalEqualState(1, "s1"), "p", "s1");
        assertEquals("Eq [1, 2, 3]", proof.getSteps().getLast().getProof().toString());
        assertFalse(new ModalEqualState(1, "s2").isValid(proof), "s2 is not s0");
        assertFalse(new ModalEqualState(1, "s0+1").isValid(proof), "s0+1 is not s0");
        assertFalse(new ModalEqualState(2, "s1").isValid(proof), "a relation has no state");
        assertFalse(new ModalEqualState(1, null).isValid(proof));
        assertTrue(new ModalEqualState(1, "s1", List.of(2, 3)).isValid(proof));
        assertFalse(new ModalEqualState(1, "s1", List.of(2)).isValid(proof), "one relation is not enough");
    }

    // ---------------------------------------------------------------- Lin

    @Test
    void linearityClosesTheCaseItRefutes() {
        var proof = proof("p");
        assumeRelation(proof, "s1 <= s0");
        line(proof, "FALSE", "s0");

        var rule = new ModalLinearity();
        assertTrue(rule.isValid(proof));
        rule.apply(proof);
        var last = proof.getSteps().getLast();
        assertEquals(new LessEqual("s0+1", "s1"), last.getStep());
        assertEquals(0, last.getAssumptionLevel());
        assertEquals("Lin [2-3]", last.getProof().toString());
        assertFalse(proof.getSteps().get(1).isValid(), "the case is closed");
    }

    @Test
    void linearityNeedsARelationAssumedAndFalse() {
        var top = proof("p");
        assertFalse(new ModalLinearity().isValid(top), "no subproof");

        var formula = proof("p");
        assume(formula, "q", "s0");
        line(formula, "FALSE", "s0");
        assertFalse(new ModalLinearity().isValid(formula), "a formula is assumed, not a relation");

        var notFalse = proof("p");
        assumeRelation(notFalse, "s1 <= s0");
        line(notFalse, "p", "s1");
        assertFalse(new ModalLinearity().isValid(notFalse), "the case does not end in FALSE");
    }

    // ---------------------------------------------------------------- -U

    @Test
    void aNegatedUntilExpands() {
        var proof = proof("- (p U q)", "p U q");

        applies(proof, new ModalNegatedUntil(1), "([] (- q)) | ((- q) U ((- p) & (- q)))", "s0");
        assertEquals("-U [1]", proof.getSteps().getLast().getProof().toString());
        assertFalse(new ModalNegatedUntil(2).isValid(proof), "not negated");
        assertFalse(new ModalNegatedUntil(3).isValid(proof), "not a negated Until");
    }

    // ---------------------------------------------------------------- UW, UB, UA

    @Test
    void anUntilHasAWitness() {
        var proof = proof("p U q", "- q");

        var witness = new ModalUntilWitness(1, "s1");
        assertTrue(witness.isValid(proof));
        witness.apply(proof);
        assertEquals(new LessEqual("s0", "s1"), proof.getSteps().getLast().getStep());
        assertEquals("UW [1]", proof.getSteps().getLast().getProof().toString());

        applies(proof, new ModalUntilWitnessRight(3), "q", "s1");
        assertEquals("UB [3]", proof.getSteps().getLast().getProof().toString());

        assertFalse(new ModalUntilWitnessLeft(3, "s0").isValid(proof), "s0 might be the witness itself");
        // -q in s0: the witness is later (s0+1 <= s1)
        assumeRelation(proof, "s1 <= s0");
        applies(proof, new ModalEqualState(4, "s0"), "q", "s0");
        new ModalFI(6, 2).apply(proof);
        new ModalLinearity().apply(proof);
        assertEquals(new LessEqual("s0+1", "s1"), proof.getSteps().getLast().getStep());

        applies(proof, new ModalUntilWitnessLeft(3, "s0"), "p", "s0");
        assertEquals("UA [3, 8]", proof.getSteps().getLast().getProof().toString());
        assertFalse(new ModalUntilWitnessLeft(3, "s1").isValid(proof), "the witness is not before itself");
        assertFalse(new ModalUntilWitnessLeft(3, null).isValid(proof));
        assertFalse(new ModalUntilWitnessLeft(1, "s0").isValid(proof), "line 1 is not a UW line");
        assertFalse(new ModalUntilWitnessRight(1).isValid(proof), "line 1 is not a UW line");
    }

    @Test
    void theWitnessIsANewState() {
        var proof = proof("p U q", "r");
        assumeRelation(proof, "s0 <= s1");

        assertFalse(new ModalUntilWitness(1, "s1").isValid(proof), "s1 is used");
        assertFalse(new ModalUntilWitness(1, "s0").isValid(proof), "s0 is the state of the Until");
        assertFalse(new ModalUntilWitness(1, "s2+1").isValid(proof), "not a base");
        assertFalse(new ModalUntilWitness(2, "s2").isValid(proof), "r is not an Until");
        var any = new ModalUntilWitness(1, null);
        assertTrue(any.isValid(proof));
        any.apply(proof);
        assertEquals(new LessEqual("s0", "s2"), proof.getSteps().getLast().getStep(), "the first unused base");
    }

    @Test
    void aWitnessCannotBeGeneralizedOver() {
        var proof = proof("p U q");
        new ModalUntilWitness(1, "s1").apply(proof);
        new ModalUntilWitnessRight(2).apply(proof);
        // []I on s1 would say that q holds in every state after s0
        assumeRelation(proof, "s0 <= s1");
        line(proof, "q", "s1");

        assertFalse(new ModalBoxI().isValid(proof), "s1 is the witness, not a new state");
    }

    // ---------------------------------------------------------------- reading the rules back

    @Test
    void theRulesAreReadBackFromTheirText() {
        for (String text : List.of("Ord", "Ord [1, 2]", "Eq [3, 1, 2]", "Lin [2-5]", "UW [1]", "UB [2]", "UA [2, 3, 4]", "-U [1]")) {
            assertEquals(text, ParseModalNextUntilAction.parseReason(text).toString());
        }
        assertThrows(IllegalArgumentException.class, () -> ParseModalNextUntilAction.parseReason("Lin [2-5-7]"));
    }

    @Test
    void aProofWithTheRulesIsReplayed() {
        var proof = proof("p U q", "- q");
        new ModalUntilWitness(1, "s1").apply(proof);
        new ModalUntilWitnessRight(3).apply(proof);
        assumeRelation(proof, "s1 <= s0");
        new ModalEqualState(4, "s0").apply(proof);
        new ModalFI(6, 2).apply(proof);
        new ModalLinearity().apply(proof);
        new ModalUntilWitnessLeft(3, "s0").apply(proof);
        new ModalOrder(parse("s0 <= s1+1")).apply(proof);
        assertTrue(proof.getSteps().stream().allMatch(step -> step.getProof() != null));

        var actions = proof.parse();
        var replayed = proof("p U q", "- q");
        for (int i = 2; i < actions.size(); i++) {
            assertTrue(actions.get(i).isValid(replayed), proof.getSteps().get(i).getProof().toString());
            actions.get(i).apply(replayed);
        }
        assertEquals(proof.getSteps().stream().map(Object::toString).toList(),
                replayed.getSteps().stream().map(Object::toString).toList());
    }

    @Test
    void theRulesAreEqualWhenTheyAreTheSame() {
        assertEquals(new ModalOrder(parse("s0 <= s1")), new ModalOrder(parse("s0 <= s1")));
        assertEquals(new ModalOrder(parse("s0 <= s1")).hashCode(), new ModalOrder(parse("s0 <= s1")).hashCode());
        assertNotEquals(new ModalOrder(parse("s0 <= s1")), new ModalOrder(List.of(1), parse("s0 <= s1")));
        assertEquals(new ModalEqualState(1, "s1"), new ModalEqualState(1, "s1"));
        assertEquals(new ModalEqualState(1, "s1").hashCode(), new ModalEqualState(1, "s1").hashCode());
        assertNotEquals(new ModalEqualState(1, "s1"), new ModalEqualState(1, "s2"));
        assertEquals(new ModalLinearity(), new ModalLinearity());
        assertEquals(new ModalLinearity().hashCode(), new ModalLinearity().hashCode());
        assertEquals(new ModalUntilWitness(1, "s1"), new ModalUntilWitness(1, "s1"));
        assertEquals(new ModalUntilWitness(1, "s1").hashCode(), new ModalUntilWitness(1, "s1").hashCode());
        assertNotEquals(new ModalUntilWitness(1, "s1"), new ModalUntilWitness(2, "s1"));
        assertEquals(new ModalUntilWitnessLeft(1, "s1"), new ModalUntilWitnessLeft(1, "s1"));
        assertEquals(new ModalUntilWitnessLeft(1, "s1").hashCode(), new ModalUntilWitnessLeft(1, "s1").hashCode());
        assertNotEquals(new ModalUntilWitnessLeft(1, "s1"), new ModalUntilWitnessLeft(1, "s2"));
        assertEquals(new ModalUntilWitnessRight(1), new ModalUntilWitnessRight(1));
        assertEquals(new ModalNegatedUntil(1), new ModalNegatedUntil(1));
    }
}
