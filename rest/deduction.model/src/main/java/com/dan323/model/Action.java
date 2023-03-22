package com.dan323.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import com.dan323.classical.proof.ParseClassicalAction;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.proof.modal.proof.ParseModalAction;

/**
 * @author danco
 */
public class Action<T extends Serializable> implements Serializable {

    @Serial
    private static final long serialVersionUID = 9213445L;
    private String name;
    private List<Integer> sources;
    private T extraInformation;

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

    public T getExtraInformation() {
        return extraInformation;
    }

    public void setExtraInformation(T extraInformation) {
        this.extraInformation = extraInformation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Action)) return false;
        Action<?> action = (Action<?>) o;
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
            return ParseClassicalAction.parseAction(name,sources, (ClassicalLogicOperation) extraInformation);
        } else if ("modal".equals(logic)) {
            var extra = (ModalExtra<?>) extraInformation;
            return ParseModalAction.parseAction(name, sources, extra.expression, extra.state);
        }
        throw new IllegalArgumentException();
    }

    private static class ModalExtra<T> {
        private T state;
        private ModalLogicalOperation expression;
    }
}
