package com.dan323.uses.modal.nextuntil;

import com.dan323.uses.LogicalExercises;
import com.dan323.uses.LogicalGetActions;
import com.dan323.uses.ProofParser;
import com.dan323.uses.Transformer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@code modal-next-until}: modal logic over discrete states, where every state {@code s} has a successor {@code s+1},
 * with Next ({@code X A}: {@code A} holds in {@code s+1}) and Until ({@code A U B}: {@code B} holds in some
 * {@code s+k}, and {@code A} in every state before it). It reuses the modal language, proofs and rules; {@code "modal"}
 * itself does not change.
 */
@Configuration
public class ModalNextUntilConfiguration {

    /** The name of this logic in the URLs, {@code /logic/modal-next-until/...}. */
    public static final String LOGIC = "modal-next-until";

    @Bean
    public LogicalGetActions modalNextUntilActions() {
        return new ModalNextUntilGetActions();
    }

    @Bean
    public Transformer modalNextUntilTransformer() {
        return new ModalNextUntilProofTransformer();
    }

    @Bean
    public ProofParser modalNextUntilProofParser() {
        return new ModalNextUntilProofParser();
    }

    @Bean
    public LogicalExercises modalNextUntilExercises() {
        return new ModalNextUntilExercises();
    }
}
