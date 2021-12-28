package com.dan323.primaryports;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public record Rule(List<String> input, String goal, String name) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1524625345L;

    @Override
    public String toString(){
        return String.join(",", input)+"\n"+goal+"\n"+name;
    }
}
