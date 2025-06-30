package com.example.actions;

import java.util.Map;

import gst.engine.actions.Action;
import gst.engine.actions.spi.ActionProvider;

public class MyCustomActionProvider implements ActionProvider {
    @Override
    public String getActionName() {
        return "myCustom";
    }

    @Override
    public Action create(Map<String, String> params) {
        return new MyCustomAction(params);
    }
}
