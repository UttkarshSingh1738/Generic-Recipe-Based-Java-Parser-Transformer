package com.example.providers;

import java.util.Map;

import com.example.actions.AutowireQualifierFieldAction;

import gst.engine.actions.Action;
import gst.engine.actions.spi.ActionProvider;

public class AutowireQualifierFieldActionProvider implements ActionProvider {
    @Override
    public String getActionName() {
        return "autowireQualifierField";
    }

    @Override
    public Action create(Map<String, String> params) {
        return new AutowireQualifierFieldAction();
    }
}
