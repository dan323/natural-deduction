package com.dan323.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Describes an action a logic offers: the {@code name} to send in {@link ActionDto#name()} and the inputs the action
 * needs, in order. The {@link ParamKind#INT} inputs are the {@link ActionDto#sources()}, in that order.
 * <p>
 * The remaining fields only help a client to present the action and are all optional ({@code null}, or empty for
 * {@code paramLabels}, when a logic does not provide them): a human {@code label} ("Modus ponens"), the {@code symbol}
 * the rule is shown with in a proof ("→E"), its {@code category}, a one-sentence {@code description} and a label per
 * input in {@code paramLabels}, in the order of {@code params}. When present, {@code paramLabels} has as many entries
 * as {@code params}.
 */
public record ActionDescriptorDto(String name, List<ParamKind> params, String label, String symbol,
                                  ActionCategory category, String description,
                                  List<String> paramLabels) implements Serializable {

    @Serial
    private static final long serialVersionUID = 6427182332L;

    public ActionDescriptorDto {
        params = params == null ? List.of() : List.copyOf(params);
        paramLabels = paramLabels == null ? List.of() : List.copyOf(paramLabels);
        if (!paramLabels.isEmpty() && paramLabels.size() != params.size()) {
            throw new IllegalArgumentException("Action " + name + " has " + params.size() + " params but "
                    + paramLabels.size() + " param labels");
        }
    }

    public ActionDescriptorDto(String name, List<ParamKind> params) {
        this(name, params, null, null, null, null, null);
    }

    public static ActionDescriptorDto of(String name, ParamKind... params) {
        return new ActionDescriptorDto(name, List.of(params));
    }
}
