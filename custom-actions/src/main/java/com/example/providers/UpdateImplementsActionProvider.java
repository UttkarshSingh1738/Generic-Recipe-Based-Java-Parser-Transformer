package com.example.providers;

import java.util.Map;

import com.example.actions.UpdateImplementsAction;

import gst.engine.actions.Action;
import gst.engine.actions.spi.ActionProvider;

public class UpdateImplementsActionProvider implements ActionProvider {
    @Override public String getActionName() { return "updateImplements"; }
    @Override public Action create(Map<String,String> params) {
        return new UpdateImplementsAction(params);
    }
}
