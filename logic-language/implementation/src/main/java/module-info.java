/**
 * Module of classic languages
 */
module language.implementation.classical{
    requires transitive language.base;
    requires javaluator;
    exports com.dan323.expresions.classical;
    exports com.dan323.expresions.classical.exceptions;
}