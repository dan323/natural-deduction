module rest.framework {
    requires use.cases;
    requires spring.web;
    requires spring.webmvc;
    requires deduction.model;
    requires rest.model;
    requires jakarta.inject;
    requires org.slf4j;
    requires framework.deduction;
    requires spring.context;

    exports com.dan323.controller to spring.web;
}