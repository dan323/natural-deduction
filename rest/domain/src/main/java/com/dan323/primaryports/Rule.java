package com.dan323.primaryports;

import com.dan323.proof.generic.bean.Construct;
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
public record Rule<T>(@JsonProperty("input") List<Assumption<T>> input,
                      @JsonProperty("goal") Goal<T> goal,
                      @JsonProperty("displayName") String displayName,
                      @JsonProperty("ruleName") String ruleName,
                      @JsonProperty("numImp") int inputs,
                      @JsonProperty("extra") boolean extra,
                      @JsonProperty("expression") boolean expression) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1524625345L;

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (goal.extraInformation() != null){
            stringBuilder.append("#CLASS#\n");
            stringBuilder.append(goal.extraInformation().getClass());
        } else {
            stringBuilder.append("#CLASS#\n");
            stringBuilder.append(Void.class);
        }
        stringBuilder.append("#DISPLAYNAME#\n");
        stringBuilder.append(displayName);
        stringBuilder.append("#RULENAME#\n");
        stringBuilder.append(ruleName);
        stringBuilder.append("#ASSMS#\n");
        input.forEach(assms -> stringBuilder.append(assms).append("\n"));
        stringBuilder.append("#GOAL#\n");
        stringBuilder.append(goal).append("\n");
        stringBuilder.append("#END#");
        return stringBuilder.toString();
    }

    public static <T> Rule<T> toRule(Construct<?,?,?> construct){
        return new Rule<>(List.of(),null, construct.getName(), construct.name(), construct.getInputs(), construct.isNeedsExpr(), construct.isNeedsExtra());
    }

}
