package gst.engine.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Test;

public class ActionFactoryTest {

    @Test
    public void getKnownActionNames_containsAddComment() {
        assertTrue(ActionFactory.getKnownActionNames().contains("addComment"));
    }

    @Test
    public void isKnownAction_addComment_returnsTrue() {
        assertTrue(ActionFactory.isKnownAction("addComment"));
    }

    @Test
    public void isKnownAction_unknown_returnsFalse() {
        assertTrue(!ActionFactory.isKnownAction("nonexistentAction"));
    }

    @Test
    public void create_addComment_returnsAction() {
        Action a = ActionFactory.create("addComment", Map.of("comment", "test"));
        assertNotNull(a);
    }

    @Test
    public void create_unknownAction_throwsWithClearMessage() {
        try {
            ActionFactory.create("notAnAction", Map.of());
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Unknown action"));
            assertTrue(e.getMessage().contains("notAnAction"));
            assertTrue(e.getMessage().contains("single-key"));
        }
    }
}
