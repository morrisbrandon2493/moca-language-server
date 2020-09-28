package com.github.mrglassdanny.mocalanguageserver.hover;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.github.mrglassdanny.mocalanguageserver.MocaServices;
import com.github.mrglassdanny.mocalanguageserver.moca.cache.MocaCache;
import com.github.mrglassdanny.mocalanguageserver.moca.cache.MocaCommand;
import com.github.mrglassdanny.mocalanguageserver.moca.cache.MocaFunction;
import com.github.mrglassdanny.mocalanguageserver.moca.cache.mocasql.Table;
import com.github.mrglassdanny.mocalanguageserver.moca.lang.MocaCompilationResult;
import com.github.mrglassdanny.mocalanguageserver.moca.lang.MocaLanguageContext;
import com.github.mrglassdanny.mocalanguageserver.moca.lang.groovy.GroovyCompilationResult;
import com.github.mrglassdanny.mocalanguageserver.moca.lang.groovy.ast.GroovyASTNodeVisitor;
import com.github.mrglassdanny.mocalanguageserver.moca.lang.groovy.util.GroovyASTUtils;
import com.github.mrglassdanny.mocalanguageserver.moca.lang.groovy.util.GroovyNodeToStringUtils;
import com.github.mrglassdanny.mocalanguageserver.moca.lang.mocasql.MocaSqlCompilationResult;
import com.github.mrglassdanny.mocalanguageserver.moca.lang.util.MocaLanguageUtils;
import com.github.mrglassdanny.mocalanguageserver.util.lsp.PositionUtils;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Variable;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;

public class HoverProvider {

    public static CompletableFuture<Hover> provideHover(TextDocumentIdentifier textDocument, Position position,
            String textDocumentContents) {

        Hover hover = new Hover();
        // Placeholder contents until we set due to analysis.
        hover.setContents(new MarkupContent(MarkupKind.PLAINTEXT, ""));

        // Analyze context id for position.
        MocaLanguageContext mocaLanguageContext = MocaLanguageUtils.getMocaLanguageContextFromPosition(position,
                MocaServices.mocaCompilationResult);

        switch (mocaLanguageContext.id) {
            case Moca:

                MocaCompilationResult mocaCompilationResult = MocaServices.mocaCompilationResult;

                if (mocaCompilationResult == null) {
                    return CompletableFuture.completedFuture(hover);
                }

                String mocaWord = PositionUtils.getWordAtPosition(textDocumentContents, position, "([a-zA-Z_0-9.])");

                if (mocaWord != null) {

                    mocaWord = mocaWord.toLowerCase();

                    // First check if this is a moca function.
                    MocaFunction mocaFunction = MocaCache.getGlobalMocaCache().functions.get(mocaWord);
                    if (mocaFunction != null) {

                        hover.setContents(new MarkupContent(MarkupKind.MARKDOWN, mocaFunction.getMarkdownStr()));

                        return CompletableFuture.completedFuture(hover);
                    }

                    // Get current moca token at position.
                    org.antlr.v4.runtime.Token curMocaToken = MocaLanguageUtils
                            .getMocaTokenAtPosition(textDocumentContents, position, mocaCompilationResult);

                    // Validate curMocaToken.
                    if (curMocaToken == null) {
                        return CompletableFuture.completedFuture(hover);
                    }

                    // Get verb noun clause current moca token is in.
                    StringBuilder verbNounClause = null;
                    for (Map.Entry<StringBuilder, ArrayList<org.antlr.v4.runtime.Token>> entry : mocaCompilationResult.mocaParseTreeListener.verbNounClauses
                            .entrySet()) {

                        // Checking for begin/end match since token objects parsed and lexed will not be
                        // the same objects.
                        for (org.antlr.v4.runtime.Token verbNounClauseToken : entry.getValue()) {
                            if (verbNounClauseToken.getStartIndex() == curMocaToken.getStartIndex()
                                    // No need to adjust stop index here!
                                    && verbNounClauseToken.getStopIndex() == curMocaToken.getStopIndex()
                                    && verbNounClauseToken.getType() == curMocaToken.getType()) {

                                verbNounClause = entry.getKey();

                                ArrayList<MocaCommand> mcmds = MocaCache.getGlobalMocaCache().commands
                                        .get(verbNounClause.toString());
                                if (mcmds != null) {
                                    String content = MocaCommand.getMarkdownStr(verbNounClause.toString(), mcmds);

                                    hover.setContents(new MarkupContent(MarkupKind.MARKDOWN, content));
                                    return CompletableFuture.completedFuture(hover);
                                }
                            }
                        }
                    }

                }

                break;
            case MocaSql:

                MocaSqlCompilationResult mocaSqlCompilationResult = MocaServices.mocaCompilationResult.mocaSqlCompilationResults
                        .get(mocaLanguageContext.rangeIdx);

                // Tables, views, aliases, and subqueries - oh my!
                String mocaSqlWord = PositionUtils.getWordAtPosition(textDocumentContents, position, "([a-zA-Z_0-9])");

                if (mocaSqlWord != null) {
                    // Convert to lowercase since repo is in lowercase.
                    mocaSqlWord = mocaSqlWord.toLowerCase();

                    // Check first to see if mocasql word is table/view in database.
                    Table table = MocaCache.getGlobalMocaCache().mocaSqlCache.tables.get(mocaSqlWord);
                    if (table != null) {
                        hover.setContents(new MarkupContent(MarkupKind.MARKDOWN, table.getMarkdownStr()));
                        return CompletableFuture.completedFuture(hover);
                    }

                    Table view = MocaCache.getGlobalMocaCache().mocaSqlCache.views.get(mocaSqlWord);
                    if (view != null) {
                        hover.setContents(new MarkupContent(MarkupKind.MARKDOWN, view.getMarkdownStr()));
                        return CompletableFuture.completedFuture(hover);
                    }

                    // If not, check aliased tables/views/subqueries.
                    if (mocaSqlCompilationResult != null && mocaSqlCompilationResult.mocaSqlParseTreeListener != null
                            && mocaSqlCompilationResult.mocaSqlParseTreeListener.aliasedTableNames != null
                            && mocaSqlCompilationResult.mocaSqlParseTreeListener.aliasedTableNames
                                    .containsKey(mocaSqlWord)) {
                        hover.setContents(new MarkupContent(MarkupKind.MARKDOWN, Table.getMarkdownStrForAlias(
                                mocaSqlCompilationResult.mocaSqlParseTreeListener.aliasedTableNames.get(mocaSqlWord))));
                        return CompletableFuture.completedFuture(hover);
                    }
                    if (mocaSqlCompilationResult != null && mocaSqlCompilationResult.mocaSqlParseTreeListener != null
                            && mocaSqlCompilationResult.mocaSqlParseTreeListener.subqueries != null
                            && mocaSqlCompilationResult.mocaSqlParseTreeListener.subqueries.containsKey(mocaSqlWord)) {
                        hover.setContents(
                                new MarkupContent(MarkupKind.MARKDOWN, Table.getMarkdownStrForSubquery(mocaSqlWord)));
                        return CompletableFuture.completedFuture(hover);
                    }

                    // Check if this is a moca function -- moca functions are valid in mocasql
                    // context.
                    MocaFunction mocaFunction = MocaCache.getGlobalMocaCache().functions.get(mocaSqlWord);
                    if (mocaFunction != null) {

                        hover.setContents(new MarkupContent(MarkupKind.MARKDOWN, mocaFunction.getMarkdownStr()));

                        return CompletableFuture.completedFuture(hover);
                    }

                }

                break;
            case Groovy:

                GroovyCompilationResult groovyCompilationResult = MocaServices.mocaCompilationResult.groovyCompilationResults
                        .get(mocaLanguageContext.rangeIdx);

                if (groovyCompilationResult.astVisitor == null) {
                    // This shouldn't happen, but let's avoid an exception if something
                    // goes terribly wrong.
                    return CompletableFuture.completedFuture(hover);
                }

                ASTNode offsetNode = groovyCompilationResult.astVisitor.getNodeAtLineAndColumn(position.getLine(),
                        position.getCharacter(),
                        MocaServices.mocaCompilationResult.groovyRanges.get(mocaLanguageContext.rangeIdx));

                ASTNode definitionNode = GroovyASTUtils.getDefinition(offsetNode, false,
                        groovyCompilationResult.astVisitor);
                if (definitionNode == null) {
                    return CompletableFuture.completedFuture(hover);
                }

                String content = getGroovyMarkdown(groovyCompilationResult.astVisitor, definitionNode);
                if (content == null) {
                    return CompletableFuture.completedFuture(hover);
                }

                hover.setContents(new MarkupContent(MarkupKind.MARKDOWN, content));
                return CompletableFuture.completedFuture(hover);
        }

        return CompletableFuture.completedFuture(hover);
    }

    private static String getGroovyMarkdown(GroovyASTNodeVisitor ast, ASTNode hoverNode) {
        if (hoverNode instanceof ClassNode) {
            ClassNode classNode = (ClassNode) hoverNode;
            return String.format("```groovy\n%s\n```", GroovyNodeToStringUtils.classToString(classNode, ast));
        } else if (hoverNode instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) hoverNode;
            return String.format("```groovy\n%s\n```", GroovyNodeToStringUtils.methodToString(methodNode, ast));
        } else if (hoverNode instanceof Variable) {
            Variable varNode = (Variable) hoverNode;
            return String.format("```groovy\n%s\n```", GroovyNodeToStringUtils.variableToString(varNode, ast));
        }
        return null;
    }
}
