package com.dan323.main;

import com.dan323.uses.classical.ClassicalConfiguration;
import com.dan323.uses.intuitionistic.IntuitionisticConfiguration;
import com.dan323.uses.internal.ActionsUseCaseConfiguration;
import com.dan323.uses.modal.ModalConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Wires the REST controllers and every logic. A new logic only needs its own {@code *Configuration} listed in the
 * {@link Import}: {@link ActionsUseCaseConfiguration} collects the beans of all logics.
 */
@Configuration
@ComponentScan("com.dan323.controller")
@Import({ActionsUseCaseConfiguration.class, ClassicalConfiguration.class, IntuitionisticConfiguration.class, ModalConfiguration.class})
public class ApplicationConfiguration {
}
