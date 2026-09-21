package com.dan323.model;

/**
 * How a rule is grouped in a list of actions. Serialized by name, e.g. {@code "ELIMINATION"}.
 */
public enum ActionCategory {
    /** A rule that introduces a connective, e.g. {@code ∧I}. */
    INTRODUCTION,
    /** A rule that eliminates a connective, e.g. {@code →E}. */
    ELIMINATION,
    /** Anything else, e.g. an assumption or a repetition. */
    OTHER
}
