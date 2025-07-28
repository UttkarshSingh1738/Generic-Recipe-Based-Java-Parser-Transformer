package gst.engine.matcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MatchResult {
    private final boolean matched;
    private final List<String> failureReasons;

    public MatchResult(boolean matched, List<String> failureReasons) {
        this.matched = matched;
        this.failureReasons = List.copyOf(failureReasons);
    }

    public static MatchResult success() {
        return new MatchResult(true, Collections.emptyList());
    }

    public static MatchResult failure(String reason) {
        List<String> reasons = new ArrayList<>();
        reasons.add(reason);
        return new MatchResult(false, reasons);
    }

    public static MatchResult combine(MatchResult a, MatchResult b) {
        if (a.matched && b.matched) return success();
        List<String> combined = new ArrayList<>(a.failureReasons);
        combined.addAll(b.failureReasons);
        return new MatchResult(false, combined);
    }

    public boolean matched() {
        return matched;
    }

    // In the failure case, the list of all reasons why the node didn’t match.
    public List<String> getFailureReasons() {
        return failureReasons;
    }
}
