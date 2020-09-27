/**
 * Module of modal languages
 */
module language.implementation.modal {
    requires transitive language.base;
    requires javaluator;

    exports com.dan323.expresions.modal;
    exports com.dan323.expresions.relation;
    exports com.dan323.expresions;
}