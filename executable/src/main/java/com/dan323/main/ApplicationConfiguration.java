package com.dan323.main;

import com.dan323.uses.classical.ClassicalConfiguration;
import com.dan323.uses.modal.ModalConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("com.dan323.controller")
@ComponentScan("com.dan323.uses.*")
@Import({ModalConfiguration.class, ClassicalConfiguration.class})
public class ApplicationConfiguration {
}
