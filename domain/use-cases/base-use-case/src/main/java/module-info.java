module use.cases {
    requires framework.deduction;
    requires language.base;
    requires spring.context;
    requires org.slf4j;
    requires deduction.model;
    requires language.implementation.classical;
    requires javaluator;

    exports com.dan323.uses;
}