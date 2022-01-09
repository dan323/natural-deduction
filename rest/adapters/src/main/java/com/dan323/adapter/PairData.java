package com.dan323.adapter;

import java.util.Map;

public class PairData implements Map.Entry<RuleSubscriber.Action, String> {

    private final RuleSubscriber.Action key;
    private volatile String value;

    public PairData(RuleSubscriber.Action key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public RuleSubscriber.Action getKey() {
        return key;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String setValue(String value) {
        String oldValue = this.value;
        this.value = value;
        return oldValue;
    }
}
