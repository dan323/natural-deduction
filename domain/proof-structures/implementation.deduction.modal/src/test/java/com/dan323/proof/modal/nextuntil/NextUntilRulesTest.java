package com.dan323.proof.modal.nextuntil;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.relation.LessEqual;
import com.dan323.proof.modal.AbstractModalAction;
import com.dan323.proof.modal.ModalAssume;
import com.dan323.proof.modal.ModalBoxE;
import com.dan323.proof.modal.ModalBoxI;
import com.dan323.proof.modal.relational.Transitive;
import com.dan323.proof.modal.ModalDiaE;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NextUntilRulesTest {

    private static ModalOperation parse(String formula) {
        return ParseModalNextUntilAction.parseExpression(formula);
    }

    private static ModalLogicalOperation formula(String formula) {
        return (ModalLogicalOperation) parse(formula);
    }

    /** A proof whose premises, in s0, are {@code premises}. */
    private static ModalNextUntilNaturalDeduction proof(String... premises) {
        var proof = new ModalNextUntilNaturalDeduction("s0");
        proof.initializeProof(List.of(premises).stream().map(NextUntilRulesTest::parse).toList(), parse("p"));
        return proof;
    }

    private static void assume(ModalNaturalDeduction proof, String formula, String state) {
        new ModalAssume(formula(formula), state).apply(proof);
    }

    private static void assumeRelation(ModalNaturalDeduction proof, String relation) {
        new ModalAssume((LessEqual) parse(relation)).apply(proof);
    }

    /** Adds {@code formula} in {@code state} to the current subproof, as if a rule derived it. */
    private static void line(ModalNaturalDeduction proof, String formula, String state) {
        proof.getSteps().add(new ProofStepModal(state, proof.getSteps().getLast().getAssumptionLevel(), formula(formula),
                new ProofReason("TST", List.of(), List.of())));
    }

    private static void applies(ModalNaturalDeduction proof, AbstractModalAction action, String formula, String state) {
        assertTrue(action.isValid(proof), action.getClass().getSimpleName());
        action.apply(proof);
        var last = proof.getSteps().getLast();
        assertEquals(parse(formula), last.getStep());
        assertEquals(state, last.getState());
    }

    @Test
    void nextIntroductionGoesBackOneState() {
        var proof = proof();
        assume(proof, "p", "s0+2");

        applies(proof, new ModalNextI(1), "X p", "s0+1");
        applies(proof, new ModalNextI(2), "X X p", "s0");
        assertEquals("XI [2]", proof.getSteps().getLast().getProof().toString());
    }

    @Test
    void nextIntroductionNeedsASuccessorState() {
        var proof = proof("p");
        assume(proof, "q", "s1");
        assumeRelation(proof, "s0 <= s0+1");

        assertFalse(new ModalNextI(1).isValid(proof), "s0 has no written predecessor");
        assertFalse(new ModalNextI(2).isValid(proof), "s1 is a base, not the successor of s0");
        assertFalse(new ModalNextI(3).isValid(proof), "a relation is not a formula");
        assertFalse(new ModalNextI(4).isValid(proof), "no such line");
        assertFalse(new ModalNextI(0).isValid(proof), "no such line");
    }

    @Test
    void nextEliminationGoesForwardOneState() {
        var proof = proof("X p", "X X q");

        applies(proof, new ModalNextE(1), "p", "s0+1");
        applies(proof, new ModalNextE(2), "X q", "s0+1");
        applies(proof, new ModalNextE(4), "q", "s0+2");
        assertEquals("XE [4]", proof.getSteps().getLast().getProof().toString());
        assertFalse(new ModalNextE(3).isValid(proof), "p is not X p");
    }

    @Test
    void successorRelatesAStateWithTheNext() {
        var proof = proof("p");
        assume(proof, "q", "s0+1");

        assertTrue(new ModalSuccessor(2).isValid(proof));
        new ModalSuccessor(2).apply(proof);
        assertEquals(new LessEqual("s0+1", "s0+2"), proof.getSteps().getLast().getStep());
        assertEquals("Succ [2]", proof.getSteps().getLast().getProof().toString());
        assertFalse(new ModalSuccessor(3).isValid(proof), "a relation has no state");
        assertFalse(new ModalSuccessor(9).isValid(proof), "no such line");
    }

    @Test
    void untilIntroductions() {
        var proof = proof("q", "p", "X (p U q)");

        applies(proof, new ModalUntilI1(1, formula("r")), "r U q", "s0");
        assertEquals("UI [1]", proof.getSteps().getLast().getProof().toString());
        applies(proof, new ModalUntilI2(2, 3), "p U q", "s0");
        assertEquals("UI [2, 3]", proof.getSteps().getLast().getProof().toString());

        assertFalse(new ModalUntilI1(1, null).isValid(proof), "the left side is missing");
        assertFalse(new ModalUntilI2(1, 3).isValid(proof), "q is not the left side of p U q");
        assertFalse(new ModalUntilI2(2, 5).isValid(proof), "p U q is not X (p U q)");
        assertFalse(new ModalUntilI2(3, 2).isValid(proof), "p is not X (A U B)");
    }

    @Test
    void untilIntroductionLaterNeedsOneState() {
        var proof = proof("X (p U q)");
        assume(proof, "p", "s0+1");

        assertFalse(new ModalUntilI2(2, 1).isValid(proof), "p is in s0+1 and X (p U q) in s0");
    }

    @Test
    void untilEliminationGivesTheExpansion() {
        var proof = proof("p U q", "p");

        applies(proof, new ModalUntilE(1), "q | (p & X (p U q))", "s0");
        assertEquals("UE [1]", proof.getSteps().getLast().getProof().toString());
        assertFalse(new ModalUntilE(2).isValid(proof));
    }

    @Test
    void untilReachesItsGoal() {
        var proof = proof("p U q", "p");

        applies(proof, new ModalUntilSometime(1), "<> q", "s0");
        assertEquals("U<> [1]", proof.getSteps().getLast().getProof().toString());
        assertFalse(new ModalUntilSometime(2).isValid(proof));
    }

    @Test
    void induction() {
        var proof = proof("p", "[] (p -> X p)", "[] (q -> X q)", "[] (p -> p)");

        applies(proof, new ModalInduction(1, 2), "[] p", "s0");
        assertEquals("Ind [1, 2]", proof.getSteps().getLast().getProof().toString());
        assertFalse(new ModalInduction(1, 3).isValid(proof), "the step is about q");
        assertFalse(new ModalInduction(1, 4).isValid(proof), "the step does not go to the next state");
        assertFalse(new ModalInduction(2, 1).isValid(proof));
    }

    @Test
    void inductionNeedsOneState() {
        var proof = proof("[] (p -> X p)");
        assume(proof, "p", "s0+1");

        assertFalse(new ModalInduction(2, 1).isValid(proof));
    }

    @Test
    void aDisabledLineCannotBeUsed() {
        var proof = proof();
        assume(proof, "X p", "s0");
        proof.getSteps().getFirst().disable();

        assertFalse(new ModalNextE(1).isValid(proof));
        assertFalse(new ModalSuccessor(1).isValid(proof));
    }

    @Test
    void aSuccessorIsNotFresh() {
        var proof = proof("p");
        assume(proof, "q", "s1+1");

        assertTrue(proof.stateIsUsedBefore("s0+3", 1), "s0 is the initial state");
        assertTrue(proof.stateIsUsedBefore("s1", 2), "s1+1 uses s1");
        assertFalse(proof.stateIsUsedBefore("s1", 1));
        assertFalse(proof.isFreshState("s0+1", "s0", 2), "s0+1 is the successor of s0");
        assertFalse(proof.isFreshState("s1", "s0", 2), "s1 is used");
        assertFalse(proof.isFreshState("s2", "s2+1", 2), "s2+1 <= s2 cannot have a fresh s2");
        assertFalse(proof.isFreshState("s 2", "s0", 2), "not a state");
        assertTrue(proof.isFreshState("s2", "s0", 2));
        assertTrue(proof.isFreshState("s2", "s0+5", 2));
    }

    @Test
    void relationsUseBothBases() {
        var proof = proof();
        assumeRelation(proof, "s1 <= s1+1");
        assumeRelation(proof, "s0 <= s2+3");

        assertTrue(proof.stateIsUsedBefore("s1", 2));
        assertTrue(proof.stateIsUsedBefore("s2", 2));
        assertFalse(proof.stateIsUsedBefore("s3", 2));
        assertEquals("s3", proof.newState());
    }

    @Test
    void boxIntroductionNeedsAFreshBase() {
        // s0 <= s0+1 is not a fresh state: []I would turn p in s0+1 into [] p in s0.
        var proof = proof("X p");
        assumeRelation(proof, "s0 <= s0+1");
        new ModalNextE(1).apply(proof);

        assertFalse(new ModalBoxI().isValid(proof));

        var fresh = proof("p");
        assumeRelation(fresh, "s0 <= s1");
        line(fresh, "q", "s1");

        assertTrue(new ModalBoxI().isValid(fresh));

        var afterItself = proof("p");
        assumeRelation(afterItself, "s1+1 <= s1");
        line(afterItself, "q", "s1");

        assertFalse(new ModalBoxI().isValid(afterItself), "the fresh state is on both sides");
    }

    @Test
    void diamondEliminationCanConcludeInASuccessorOfAnOldState() {
        // <> q, and in the fresh s1 derive something about s0+1: the conclusion is not about s1, so <>E closes.
        var proof = proof("<> q", "X r");
        assumeRelation(proof, "s0 <= s1");
        assume(proof, "q", "s1");
        new ModalNextE(2).apply(proof);

        assertTrue(new ModalDiaE(1).isValid(proof));
        new ModalDiaE(1).apply(proof);
        assertEquals("s0+1", proof.getSteps().getLast().getState());
    }

    @Test
    void diamondEliminationCannotConcludeAboutTheFreshState() {
        var proof = proof("<> q", "[] r");
        assumeRelation(proof, "s0 <= s1");
        assume(proof, "q", "s1");
        new ModalSuccessor(4).apply(proof);

        assertFalse(new ModalDiaE(1).isValid(proof), "s1 <= s1+1 is about the fresh s1");

        new Transitive(3, 5).apply(proof);
        new ModalBoxE(2, 6).apply(proof);

        assertEquals("s1+1", proof.getSteps().getLast().getState());
        assertFalse(new ModalDiaE(1).isValid(proof), "s1+1 is about the fresh s1");
    }

    @Test
    void theGoalMustBeInTheInitialState() {
        var proof = new ModalNextUntilNaturalDeduction("s0");
        proof.initializeProof(List.of(parse("X p")), parse("p"));
        new ModalNextE(1).apply(proof);

        assertEquals(parse("p"), proof.getSteps().getLast().getStep());
        assertFalse(proof.isDone(), "p holds in s0+1, not in s0");

        var modal = new ModalNaturalDeduction("s0");
        modal.initializeProof(List.of(parse("X p")), parse("p"));
        new ModalAssume(formula("p"), "s1").apply(modal);
        assertFalse(modal.isDone(), "an assumption is not at the top level");

        var now = new ModalNextUntilNaturalDeduction("s0");
        now.initializeProof(List.of(parse("q")), parse("p U q"));
        new ModalUntilI1(1, formula("p")).apply(now);
        assertTrue(now.isDone());

        var relation = new ModalNextUntilNaturalDeduction("s0");
        relation.initializeProof(List.of(parse("p")), parse("s0 <= s0+1"));
        new ModalSuccessor(1).apply(relation);
        assertTrue(relation.isDone());
    }

    @Test
    void thereIsNoSolver() {
        assertThrows(UnsupportedOperationException.class, () -> proof("p").automate());
    }
}
