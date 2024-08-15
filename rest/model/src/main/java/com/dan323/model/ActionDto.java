package com.dan323.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author danco
 */
public record ActionDto(String name, List<Integer> sources, Map<String,String> extraParameters) implements Serializable {

    @Serial
    private static final long serialVersionUID = 9213445L;


}
