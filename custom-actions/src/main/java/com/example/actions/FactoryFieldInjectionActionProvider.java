package com.example.actions;

import java.util.Map;

import gst.engine.actions.Action;
import gst.engine.actions.spi.ActionProvider;

public class FactoryFieldInjectionActionProvider implements ActionProvider {
    @Override
    public String getActionName() {
        return "factoryFieldInjection";
    }

    @Override
    public Action create(Map<String, String> params) {
        return new FactoryFieldInjectionAction();
    }
}
