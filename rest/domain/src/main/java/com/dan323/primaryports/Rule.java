package com.dan323.primaryports;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Rule<T>(@JsonProperty("input") List<Assumption<T>> input,@JsonProperty("goal") Goal<T> goal,@JsonProperty("name") String name) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1524625345L;

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("#NAME#\n");
        stringBuilder.append(name);
        stringBuilder.append("#ASSMS#\n");
        input.forEach(assms -> stringBuilder.append(assms).append("\n"));
        stringBuilder.append("#GOAL#\n");
        stringBuilder.append(goal).append("\n");
        stringBuilder.append("#END#");
        return stringBuilder.toString();
    }
}
