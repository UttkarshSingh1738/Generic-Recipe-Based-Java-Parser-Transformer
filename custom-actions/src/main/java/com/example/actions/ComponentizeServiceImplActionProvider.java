package com.example.actions;

import java.util.Map;

import gst.engine.actions.Action;
import gst.engine.actions.spi.ActionProvider;

public class ComponentizeServiceImplActionProvider implements ActionProvider {
    @Override
    public String getActionName() {
        return "componentizeService";
    }

    @Override
    public Action create(Map<String, String> params) {
        return new ComponentizeServiceImplAction();
    }
}
