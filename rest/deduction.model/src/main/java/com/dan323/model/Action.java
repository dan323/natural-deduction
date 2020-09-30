package com.dan323.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @author danco
 */
public class Action<T extends Serializable> implements Serializable {

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
}
