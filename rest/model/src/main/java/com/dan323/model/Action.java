package com.dan323.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import com.dan323.classical.proof.ParseClassicalAction;
import com.dan323.expressions.ModalLogicParser;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.expressions.classical.ClassicalParser;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.proof.modal.proof.ParseModalAction;

/**
 * @author danco
 */
public class Action implements Serializable {

    @Serial
    private static final long serialVersionUID = 9213445L;
    private String name;
    private List<Integer> sources;
    private Extra extraInformation;

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public List<Integer> getSources() {
        return sources;
    }

    public void setSources(List<Integer> sources) {
        this.sources = sources;
    }

    public Extra getExtraInformation() {
        return extraInformation;
    }

    public void setExtraInformation(Extra extraInformation) {
        this.extraInformation = extraInformation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Action action)) return false;
        return Objects.equals(name, action.name) &&
                Objects.equals(sources, action.sources) &&
                Objects.equals(extraInformation, action.extraInformation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, sources, extraInformation);
    }

    public com.dan323.proof.generic.Action toDomain(String logic){
        if ("classical".equals(logic)) {
            var parser = new ClassicalParser();
            return ParseClassicalAction.parseAction(name,sources, parser.evaluate(extraInformation.getExpression()));
        } else if ("modal".equals(logic)) {
            var extra = extraInformation;
            var parser = new ModalLogicParser();
            return ParseModalAction.parseAction(name, sources, parser.evaluate(extra.getExpression()), extra.getState());
        }
        throw new IllegalArgumentException();
    }
}
