package com.example.actions;

import java.util.Map;

import gst.engine.actions.Action;
import gst.engine.actions.spi.ActionProvider;

public class FactoryLocalVarReplacementActionProvider implements ActionProvider {
    @Override
    public String getActionName() {
        return "factoryLocalVarReplacement";
    }

    @Override
    public Action create(Map<String, String> params) {
        return new FactoryLocalVarReplacementAction();
    }
}
