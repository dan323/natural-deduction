package com.dan323.model;

/**
 * The kind of value an action needs from the user. Serialized by name, e.g. {@code "INT"}.
 */
public enum ParamKind {
    /** A 1-based line number of the proof, sent in {@link ActionDto#sources()}. */
    INT,
    /** A formula, sent as {@code extraParameters.expression}. */
    EXPRESSION,
    /** A state (world) name, sent as {@code extraParameters.state}. Only used by modal logic. */
    STATE
}
