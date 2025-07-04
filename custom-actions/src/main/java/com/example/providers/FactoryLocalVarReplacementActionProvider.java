package com.example.providers;

import java.util.Map;

import com.example.actions.FactoryLocalVarReplacementAction;

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
