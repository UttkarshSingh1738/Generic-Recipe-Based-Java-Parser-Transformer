package com.yourorganization.maven_sample;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

public class GuavaSymbolSolverInspector {

    public static void main(String[] args) throws IOException {
        Path guavaSrc = Paths.get("src", "main", "resources", "input", "guava", "src");
        CombinedTypeSolver solver = new CombinedTypeSolver(
            new ReflectionTypeSolver(),
            new JavaParserTypeSolver(guavaSrc)
        );
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(solver);
        StaticJavaParser.getParserConfiguration().setSymbolResolver(symbolSolver);

        Path symbolOut = Paths.get("output", "SymbolSolver", "symbol_log.txt");
        Path astOut = Paths.get("output", "SymbolSolver", "ast_tree.txt");
        Files.createDirectories(symbolOut.getParent());

        try (
            BufferedWriter symbolWriter = Files.newBufferedWriter(symbolOut);
            BufferedWriter astWriter = Files.newBufferedWriter(astOut)
        ) {
            try (Stream<Path> stream = Files.walk(guavaSrc)) {
                List<Path> javaFiles = stream.filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList());

                for (Path file : javaFiles) {
                    symbolWriter.write("\n======== File: " + file.getFileName() + " ========\n");
                    astWriter.write("\n======== File: " + file.getFileName() + " ========\n");

                    CompilationUnit cu;
                    try {
                        cu = StaticJavaParser.parse(file);
                    } catch (Exception e) {
                        symbolWriter.write("[ParseError] " + e.getMessage() + "\n");
                        continue;
                    }

                    dumpAstTree(cu, astWriter, 0);

                    cu.walk(Node.TreeTraversal.POSTORDER, node -> {
                        try {
                            switch (node) {
                                case VariableDeclarator vd -> {
                                    ResolvedType type = JavaParserFacade.get(solver).getType(vd);
                                    symbolWriter.write("[Var] " + vd.getName() + " ⇒ " + type.describe() + "\n");
                                }
                                case MethodCallExpr mce -> {
                                    ResolvedType type = JavaParserFacade.get(solver).getType(mce);
                                    symbolWriter.write("[Call] " + mce + " ⇒ " + type.describe() + "\n");
                                }
                                case ObjectCreationExpr oce -> {
                                    ResolvedType type = JavaParserFacade.get(solver).getType(oce);
                                    symbolWriter.write("[New] " + oce + " ⇒ " + type.describe() + "\n");
                                }
                                case FieldAccessExpr fae -> {
                                    ResolvedType type = JavaParserFacade.get(solver).getType(fae);
                                    symbolWriter.write("[Field] " + fae + " ⇒ " + type.describe() + "\n");
                                }
                                case Expression expr -> {
                                    ResolvedType type = JavaParserFacade.get(solver).getType(expr);
                                    symbolWriter.write("[Expr] " + expr + " ⇒ " + type.describe() + "\n");
                                }
                                default -> {
                                }
                            }
                        } catch (UnsolvedSymbolException use) {
                            try {
                                symbolWriter.write("[Unresolved] " + node + " ⇒ " + use.getName() + "\n");
                            } catch (IOException ioException) {
                                throw new RuntimeException(ioException);
                            }
                        } catch (Exception e) {
                            try {
                                symbolWriter.write("[Error] " + node + " ⇒ " + e.getMessage() + "\n");
                            } catch (IOException ioException) {
                                throw new RuntimeException(ioException);
                            }
                        }
                    });
                }
            }
        }

        System.out.println("Logs written to 'output/SymbolSolver/'");
    }

    private static void dumpAstTree(Node node, BufferedWriter writer, int indent) {
        try {
            writer.write("  ".repeat(indent) + "- " + node.getClass().getSimpleName());
            if (node instanceof NameExpr || node instanceof SimpleName) {
                writer.write(": " + node.toString());
            }
            writer.write("\n");
            for (Node child : node.getChildNodes()) {
                dumpAstTree(child, writer, indent + 1);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
