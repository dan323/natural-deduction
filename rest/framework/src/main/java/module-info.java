module rest.framework {
    requires use.cases;
    requires spring.web;
    requires deduction.model;
    requires javax.inject;
    requires org.slf4j;
    requires framework.deduction;

    exports com.dan323.controller to spring.web;
}