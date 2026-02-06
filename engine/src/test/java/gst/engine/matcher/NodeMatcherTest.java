package gst.engine.matcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import org.junit.Test;

import gst.api.Match;

public class NodeMatcherTest {

    private static final CombinedTypeSolver TYPE_SOLVER = new CombinedTypeSolver(new ReflectionTypeSolver());

    @Test
    public void findCandidates_classDeclaration_returnsOneNode() {
        CompilationUnit cu = StaticJavaParser.parse("public class Foo { }");
        java.util.List<Node> candidates = NodeMatcher.findCandidates(cu, "ClassOrInterfaceDeclaration");
        assertEquals(1, candidates.size());
    }

    @Test
    public void matches_namePattern_matchesClassNamedFoo() {
        CompilationUnit cu = StaticJavaParser.parse("public class Foo { }");
        java.util.List<Node> candidates = NodeMatcher.findCandidates(cu, "ClassOrInterfaceDeclaration");
        assertEquals(1, candidates.size());
        Match m = new Match();
        m.nodeType = "ClassOrInterfaceDeclaration";
        m.namePattern = "Foo";
        MatchResult result = NodeMatcher.matches(candidates.get(0), m, TYPE_SOLVER);
        assertTrue("expected match: " + result.getFailureReasons(), result.matched());
    }

    @Test
    public void matches_namePattern_noMatchWhenNameDoesNotMatch() {
        CompilationUnit cu = StaticJavaParser.parse("public class Bar { }");
        java.util.List<Node> candidates = NodeMatcher.findCandidates(cu, "ClassOrInterfaceDeclaration");
        Match m = new Match();
        m.nodeType = "ClassOrInterfaceDeclaration";
        m.namePattern = "Foo";
        MatchResult result = NodeMatcher.matches(candidates.get(0), m, TYPE_SOLVER);
        assertTrue(!result.matched());
    }
}
