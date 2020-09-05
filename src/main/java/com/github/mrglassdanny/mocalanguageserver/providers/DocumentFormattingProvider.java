package com.github.mrglassdanny.mocalanguageserver.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.github.mrglassdanny.mocalanguageserver.moca.lang.MocaCompiler;
import com.github.mrglassdanny.mocalanguageserver.moca.lang.antlr.MocaLexer;

import com.github.mrglassdanny.mocalanguageserver.moca.lang.format.MocaFormatter;

import com.github.mrglassdanny.mocalanguageserver.moca.lang.sql.format.MocaSqlFormatter;
import com.github.mrglassdanny.mocalanguageserver.moca.lang.sql.util.MocaSqlLanguageUtils;
import com.github.mrglassdanny.mocalanguageserver.util.lsp.Positions;

import org.antlr.v4.runtime.Token;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

public class DocumentFormattingProvider {

        public static CompletableFuture<List<? extends TextEdit>> provideDocumentFormatting(
                        DocumentFormattingParams params, String textDocumentContents, MocaCompiler mocaCompiler) {

                // Check to see if file extension is marked as read only. If so, do not attempt
                // to format.
                // This will be the case for any files created by command lookup functionality
                // in the client.
                String uriStr = params.getTextDocument().getUri();
                String uriExtStr = uriStr.substring(uriStr.lastIndexOf("."));
                if (uriExtStr.compareToIgnoreCase(".readonly") == 0) {
                        return CompletableFuture.completedFuture(new ArrayList<>());
                }

                return CompletableFuture.completedFuture(DocumentFormattingProvider
                                .processDocumentFormatting(textDocumentContents, mocaCompiler));

        }

        // Range formatting will be exact same as doc formatting.
        public static CompletableFuture<List<? extends TextEdit>> provideDocumentRangeFormatting(
                        DocumentRangeFormattingParams params, String textDocumentContents, MocaCompiler mocaCompiler) {

                // Check to see if file extension is marked as read only. If so, do not attempt
                // to format.
                // This will be the case for any files created by command lookup functionality
                // in the client.
                String uriStr = params.getTextDocument().getUri();
                String uriExtStr = uriStr.substring(uriStr.lastIndexOf("."));
                if (uriExtStr.compareToIgnoreCase(".readonly") == 0) {
                        return CompletableFuture.completedFuture(new ArrayList<>());
                }

                return CompletableFuture.completedFuture(DocumentFormattingProvider
                                .processDocumentFormatting(textDocumentContents, mocaCompiler));
        }

        public static ArrayList<TextEdit> processDocumentFormatting(String textDocumentContents,
                        MocaCompiler mocaCompiler) {
                ArrayList<TextEdit> edits = new ArrayList<>();

                String formattedTextDocumentContents = textDocumentContents;

                // Process sql & groovy:
                for (Token mocaToken : mocaCompiler.mocaTokens) {
                        if (mocaToken.getType() == MocaLexer.SINGLE_BRACKET_STRING) {

                                // Make sure we are dealing with actual sql string.
                                String tokenText = mocaToken.getText();
                                if (MocaSqlLanguageUtils.isMocaTokenValueSqlScript(tokenText)) {
                                        // Remove brackets for formatting.
                                        String sqlScript = tokenText.substring(1, tokenText.length() - 1);

                                        String formattedSqlScript = MocaSqlFormatter.format(sqlScript);

                                        if (formattedSqlScript != null) {
                                                // In order for the entire sql script to be indented correctly
                                                // after formatting, we need to get the char num from the single bracket
                                                // string token and add that amount spaces/tabs to each newline in
                                                // formatted sql script.
                                                StringBuilder indentBuf = new StringBuilder(
                                                                mocaToken.getCharPositionInLine());
                                                for (int i = 0; i < mocaToken.getCharPositionInLine(); i++) {
                                                        indentBuf.append(' ');
                                                }
                                                formattedSqlScript = formattedSqlScript.replace("\n",
                                                                "\n" + indentBuf.toString());

                                                // Add to formatted text doc.
                                                // Dont forget to add brackets back!
                                                formattedTextDocumentContents = formattedTextDocumentContents.replace(
                                                                mocaToken.getText(), "[" + formattedSqlScript + "]");
                                        }
                                }

                        } else if (mocaToken.getType() == MocaLexer.DOUBLE_BRACKET_STRING) {
                                // TODO: format groovy!
                        }
                }

                String formattedMocaScript = MocaFormatter.format(formattedTextDocumentContents);

                if (formattedMocaScript != null) {
                        // If successful, replace formatted text doc with formatted moca script.
                        // Otherwise, we still want previous sql/groovy edits to go through.
                        formattedTextDocumentContents = formattedMocaScript;
                }

                // Add to text doc edits and return!
                edits.add(new TextEdit(
                                new Range(new Position(0, 0),
                                                Positions.getPosition(textDocumentContents,
                                                                textDocumentContents.length())),
                                formattedTextDocumentContents));
                return edits;
        }

}