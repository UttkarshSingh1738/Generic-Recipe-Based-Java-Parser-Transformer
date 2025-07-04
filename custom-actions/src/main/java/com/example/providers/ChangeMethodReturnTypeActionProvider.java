package com.example.providers;

import java.util.Map;

import com.example.actions.ChangeMethodReturnTypeAction;

import gst.engine.actions.Action;
import gst.engine.actions.spi.ActionProvider;

public class ChangeMethodReturnTypeActionProvider implements ActionProvider {
    @Override public String getActionName() { return "changeMethodReturnType"; }
    @Override public Action create(Map<String,String> params) {
        return new ChangeMethodReturnTypeAction(params);
    }
}
