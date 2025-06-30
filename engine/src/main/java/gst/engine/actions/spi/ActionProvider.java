package gst.engine.actions.spi;

import java.util.Map;

import gst.engine.actions.Action;

public interface ActionProvider {
    String getActionName();

    Action create(Map<String,String> params);
}
