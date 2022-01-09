module rest.domain {
    exports com.dan323.primaryports;
    exports com.dan323.secondaryports;
    exports com.dan323.exception;

    requires spring.context;
    requires spring.beans;
    requires reactor.core;
    requires framework.deduction;
    requires language.base;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires spring.boot;
}