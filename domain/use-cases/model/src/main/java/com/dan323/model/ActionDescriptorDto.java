package com.dan323.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Describes an action a logic offers: the {@code name} to send in {@link ActionDto#name()} and the inputs the action
 * needs, in order. The {@link ParamKind#INT} inputs are the {@link ActionDto#sources()}, in that order.
 */
public record ActionDescriptorDto(String name, List<ParamKind> params) implements Serializable {

    @Serial
    private static final long serialVersionUID = 6427182331L;

    public ActionDescriptorDto {
        params = params == null ? List.of() : List.copyOf(params);
    }

    public static ActionDescriptorDto of(String name, ParamKind... params) {
        return new ActionDescriptorDto(name, List.of(params));
    }
}
