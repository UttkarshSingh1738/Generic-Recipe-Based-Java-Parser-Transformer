package com.example.providers;

import java.util.Map;

import com.example.actions.RenameMethodAction;

import gst.engine.actions.Action;
import gst.engine.actions.spi.ActionProvider;

public class RenameMethodActionProvider implements ActionProvider{
    @Override public String getActionName() { return "renameMethod"; }
    @Override public Action create(Map<String,String> params) {
        return new RenameMethodAction(params);
    }
}
