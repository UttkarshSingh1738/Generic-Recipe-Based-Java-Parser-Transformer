package com.example.providers;

import java.util.Map;

import com.example.actions.RemoveArgumentAction;

import gst.engine.actions.Action;
import gst.engine.actions.spi.ActionProvider;

public class RemoveArgumentActionProvider implements ActionProvider{
    @Override public String getActionName() { return "removeArgument"; }
    @Override public Action create(Map<String,String> params) {
        return new RemoveArgumentAction(params);
    }
}
