package gst.engine.actions;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public interface Action {
    /**
     * Apply this action to `node` within its containing `CompilationUnit`.
     * May mutate the AST and/or record side-effects in context.
     */
    void apply(Node node, CompilationUnit cu, TxContext context, JavaSymbolSolver solver);
}
