module rest.adapters {
    requires rest.domain;
    requires spring.context;
    requires spring.beans;
    requires spring.web;
    requires spring.webflux;
    requires reactor.core;
    requires org.reactivestreams;
    requires framework.deduction;
    requires language.base;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
}