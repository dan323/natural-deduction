package com.dan323.uses.modal.until;

import com.dan323.uses.LogicalExercises;
import com.dan323.uses.LogicalGetActions;
import com.dan323.uses.ProofParser;
import com.dan323.uses.Transformer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Modal logic with Until ({@code A U B}): the modal language, proofs and rules, plus the Until operator and its rules.
 * {@code "modal"} itself does not change.
 */
@Configuration
public class ModalUntilConfiguration {

    /** The name of this logic in the URLs, {@code /logic/modal-until/...}. */
    public static final String LOGIC = "modal-until";

    @Bean
    public LogicalGetActions modalUntilActions() {
        return new ModalUntilGetActions();
    }

    @Bean
    public Transformer modalUntilTransformer() {
        return new ModalUntilProofTransformer();
    }

    @Bean
    public ProofParser modalUntilProofParser() {
        return new ModalUntilProofParser();
    }

    @Bean
    public LogicalExercises modalUntilExercises() {
        return new ModalUntilExercises();
    }
}
