package com.github.mrglassdanny.mocalanguageserver.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.github.mrglassdanny.mocalanguageserver.MocaLanguageServer;
import com.github.mrglassdanny.mocalanguageserver.moca.lang.MocaCompiler;
import com.github.mrglassdanny.mocalanguageserver.moca.lang.antlr.MocaLexer;
import com.github.mrglassdanny.mocalanguageserver.moca.lang.antlr.MocaParser;
import com.github.mrglassdanny.mocalanguageserver.moca.lang.antlr.MocaSqlLexer;
import com.github.mrglassdanny.mocalanguageserver.moca.lang.antlr.MocaSqlParser;
import com.github.mrglassdanny.mocalanguageserver.moca.lang.sql.MocaSqlCompilationResult;
import com.github.mrglassdanny.mocalanguageserver.moca.lang.sql.util.MocaSqlLanguageUtils;
import com.github.mrglassdanny.mocalanguageserver.moca.lang.util.MocaTokenUtils;
import com.github.mrglassdanny.mocalanguageserver.util.lsp.Positions;

import org.antlr.codebuff.Tool;
import org.antlr.codebuff.misc.LangDescriptor;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

public class DocumentFormattingProvider {

        private static final String NEWLINE = "\n";
        private static final String TAB = "\t";
        private static final String SPACE = " ";
        private static final String EMPTY = "";

        public static CompletableFuture<List<? extends TextEdit>> provideDocumentFormatting(
                        DocumentFormattingParams params, String textDocumentContents, MocaCompiler mocaCompiler) {

                // Check to see if file extension is marked as read only. If so, do not publish
                // any diagnostics.
                // This will be the case for any files created by command lookup functionality
                // in the client.
                String uriStr = params.getTextDocument().getUri();
                String uriExtStr = uriStr.substring(uriStr.lastIndexOf("."));
                if (uriExtStr.compareToIgnoreCase(".readonly") == 0) {
                        return CompletableFuture.completedFuture(new ArrayList<>());
                }

                ArrayList<TextEdit> edits = new ArrayList<>();

                // formatMoca(edits, mocaCompiler, textDocumentContents);

                try {
                        org.antlr.codebuff.Tool.format(new LangDescriptor("Moca",
                                        "C:\\Users\\dglass\\OneDrive - Longbow Advantage\\Desktop\\corpus\\moca",
                                        ".*\\.msql", MocaLexer.class, MocaParser.class, "moca_script", 4,
                                        MocaLexer.BLOCK_COMMENT),
                                        "C:\\Users\\dglass\\OneDrive - Longbow Advantage\\Desktop\\format-a.msql",
                                        "C:\\Users\\dglass\\OneDrive - Longbow Advantage\\Desktop\\format-b.msql");

                } catch (Exception e) {
                        MocaLanguageServer.languageClient
                                        .logMessage(new MessageParams(MessageType.Error, e.toString()));
                }

                return CompletableFuture.completedFuture(edits);

        }

        // Range formatting will be exact same as doc formatting.
        public static CompletableFuture<List<? extends TextEdit>> provideDocumentRangeFormatting(
                        DocumentRangeFormattingParams params, String textDocumentContents, MocaCompiler mocaCompiler) {

                // Check to see if file extension is marked as read only. If so, do not publish
                // any diagnostics.
                // This will be the case for any files created by command lookup functionality
                // in the client.
                String uriStr = params.getTextDocument().getUri();
                String uriExtStr = uriStr.substring(uriStr.lastIndexOf("."));
                if (uriExtStr.compareToIgnoreCase(".readonly") == 0) {
                        return CompletableFuture.completedFuture(new ArrayList<>());
                }

                ArrayList<TextEdit> edits = new ArrayList<>();

                // formatMoca(edits, mocaCompiler, textDocumentContents);

                return CompletableFuture.completedFuture(edits);

        }

        private static void formatMoca(ArrayList<TextEdit> edits, MocaCompiler mocaCompiler, String mocaScript) {
                // This is how we will know what range index we are in.
                // Starting at -1 because we will increment counter before we get any data from
                // counter.
                int sqlRangesVisited = -1, groovyRangesVisited = -1;
                int mocaTokenCount = mocaCompiler.mocaTokens.size();
                org.antlr.v4.runtime.Token curMocaToken = null, prevMocaToken = null, nextMocaToken = null;
                StringBuilder indentBuilder = new StringBuilder();
                int parenStack = 0;
                for (int i = 0; i < mocaTokenCount; i++) {

                        curMocaToken = mocaCompiler.mocaTokens.get(i);
                        int curMocaTokenStartIdx = curMocaToken.getStartIndex();
                        int curMocaTokenStopIdx = MocaTokenUtils
                                        .getAdjustedMocaTokenStopIndex(curMocaToken.getStopIndex());

                        if (i > 0) {
                                prevMocaToken = mocaCompiler.mocaTokens.get(i - 1);
                        } else {
                                prevMocaToken = null;
                        }

                        int prevMocaTokenStartIdx = curMocaTokenStartIdx;
                        int prevMocaTokenStopIdx = curMocaTokenStartIdx;
                        if (prevMocaToken != null) {
                                prevMocaTokenStartIdx = prevMocaToken.getStartIndex();
                                prevMocaTokenStopIdx = MocaTokenUtils
                                                .getAdjustedMocaTokenStopIndex(prevMocaToken.getStopIndex());
                        }

                        if (i < mocaTokenCount - 1) {
                                nextMocaToken = mocaCompiler.mocaTokens.get(i + 1);
                        } else {
                                nextMocaToken = null;
                        }

                        int nextMocaTokenStartIdx = curMocaTokenStopIdx;
                        int nextMocaTokenStopIdx = curMocaTokenStopIdx;
                        if (nextMocaToken != null) {
                                nextMocaTokenStartIdx = nextMocaToken.getStartIndex();
                                nextMocaTokenStopIdx = MocaTokenUtils
                                                .getAdjustedMocaTokenStopIndex(nextMocaToken.getStopIndex());
                        }

                        // Have to manually calculate begin whitespace.
                        int curMocaTokenBeginWhitespaceIdx = 0;
                        if (prevMocaToken != null) {
                                curMocaTokenBeginWhitespaceIdx = prevMocaTokenStopIdx;
                        }

                        switch (curMocaToken.getType()) {
                                case MocaLexer.SINGLE_BRACKET_STRING:
                                        // Dig into contents of bracket string.
                                        // Gonna be either sql or just a bracket string.
                                        String curMocaTokenValue = curMocaToken.getText();
                                        boolean isSql = false;

                                        // Making sure we are actually dealing with an sql statement before we
                                        // add this
                                        // range.
                                        if (MocaSqlLanguageUtils.isMocaTokenValueSqlScript(curMocaTokenValue)) {
                                                isSql = true;
                                                sqlRangesVisited++;
                                        }
                                        // Else, just a bracket string.

                                        if (isSql) {

                                                // TODO: Implement sql formatting.

                                        } else {
                                                // Just a bracket string; do nothing.
                                        }

                                        break;
                                case MocaLexer.DOUBLE_BRACKET_STRING:
                                        groovyRangesVisited++;
                                        Range groovyScriptRange = mocaCompiler.groovyRanges.get(groovyRangesVisited);

                                        // Remove '[[]]'.
                                        String groovyScript = curMocaToken.getText().replaceAll("(\\[\\[)|(\\]\\])",
                                                        "");

                                        // TODO: Implement groovy formatting.
                                        break;
                                case MocaLexer.SEMI_COLON:
                                        // Remove whitespace before.
                                        edits.add(new TextEdit(new Range(
                                                        Positions.getPosition(mocaScript,
                                                                        curMocaTokenBeginWhitespaceIdx),
                                                        Positions.getPosition(mocaScript, curMocaTokenStartIdx)),
                                                        EMPTY));

                                        // Add newline and indents after.
                                        edits.add(new TextEdit(new Range(
                                                        Positions.getPosition(mocaScript, curMocaTokenStopIdx),
                                                        // It is not a mistake that we are repeating
                                                        // curMocaTokenStopIdx -- it is needed for
                                                        // proper semicolon formatting!
                                                        Positions.getPosition(mocaScript, nextMocaTokenStartIdx)),
                                                        NEWLINE + indentBuilder.toString()));

                                        break;
                                case MocaLexer.LEFT_BRACE:
                                        // Newline before(only if prev did not add new line
                                        // (pipe/amp/semicolon/open_brace)).
                                        if (prevMocaToken != null) {
                                                if (prevMocaToken.getType() != MocaLexer.PIPE
                                                                && prevMocaToken.getType() != MocaLexer.AMPERSAND
                                                                && prevMocaToken.getType() != MocaLexer.SEMI_COLON
                                                                && prevMocaToken.getType() != MocaLexer.LEFT_BRACE) {
                                                        edits.add(new TextEdit(new Range(
                                                                        Positions.getPosition(mocaScript,
                                                                                        curMocaTokenBeginWhitespaceIdx),
                                                                        Positions.getPosition(mocaScript,
                                                                                        curMocaTokenStartIdx)),
                                                                        SPACE));
                                                }
                                        }

                                        indentBuilder.append(TAB);

                                        edits.add(new TextEdit(new Range(
                                                        Positions.getPosition(mocaScript, curMocaTokenStopIdx),
                                                        Positions.getPosition(mocaScript, nextMocaTokenStartIdx)),
                                                        NEWLINE + indentBuilder.toString()));

                                        break;
                                case MocaLexer.RIGHT_BRACE:

                                        if (indentBuilder.length() > 0) {
                                                indentBuilder.deleteCharAt(0);
                                        }
                                        // Newline before.
                                        edits.add(new TextEdit(new Range(
                                                        Positions.getPosition(mocaScript,
                                                                        curMocaTokenBeginWhitespaceIdx),
                                                        Positions.getPosition(mocaScript, curMocaTokenStartIdx)),
                                                        NEWLINE + indentBuilder.toString()));

                                        break;
                                case MocaLexer.PIPE:
                                        // Need to make sure we are not dealing with a double pipe.
                                        // We will do this by checking next token and previous token.
                                        if (nextMocaToken != null && nextMocaToken.getType() == MocaLexer.PIPE) {
                                                // Space at beginning and remove whitespace after.
                                                edits.add(new TextEdit(new Range(
                                                                Positions.getPosition(mocaScript,
                                                                                curMocaTokenBeginWhitespaceIdx),
                                                                Positions.getPosition(mocaScript,
                                                                                curMocaTokenStartIdx)),
                                                                SPACE));
                                                edits.add(new TextEdit(
                                                                new Range(Positions.getPosition(mocaScript,
                                                                                curMocaTokenStopIdx),
                                                                                Positions.getPosition(mocaScript,
                                                                                                nextMocaTokenStartIdx)),
                                                                EMPTY));
                                        } else if (prevMocaToken != null && prevMocaToken.getType() == MocaLexer.PIPE) {
                                                // Space in back - we are remove whitespace between pipes in cond above.
                                                edits.add(new TextEdit(
                                                                new Range(Positions.getPosition(mocaScript,
                                                                                curMocaTokenStopIdx),
                                                                                Positions.getPosition(mocaScript,
                                                                                                nextMocaTokenStartIdx)),
                                                                SPACE));
                                        } else {

                                                // Newline before and after.
                                                edits.add(new TextEdit(
                                                                new Range(Positions.getPosition(mocaScript,
                                                                                curMocaTokenBeginWhitespaceIdx),
                                                                                Positions.getPosition(mocaScript,
                                                                                                curMocaTokenStartIdx)),
                                                                NEWLINE + indentBuilder.toString()));

                                                edits.add(new TextEdit(new Range(
                                                                Positions.getPosition(mocaScript, curMocaTokenStopIdx),
                                                                Positions.getPosition(mocaScript,
                                                                                nextMocaTokenStartIdx)),
                                                                NEWLINE + indentBuilder.toString()));
                                        }
                                        break;
                                case MocaLexer.LEFT_PAREN:
                                        parenStack++;
                                        // Remove whitespace after.
                                        edits.add(new TextEdit(new Range(
                                                        Positions.getPosition(mocaScript, curMocaTokenStopIdx),
                                                        Positions.getPosition(mocaScript, nextMocaTokenStartIdx)),
                                                        EMPTY));
                                        break;
                                case MocaLexer.RIGHT_PAREN:
                                        parenStack--;
                                        // Remove whitespace before.
                                        edits.add(new TextEdit(new Range(
                                                        Positions.getPosition(mocaScript,
                                                                        curMocaTokenBeginWhitespaceIdx),
                                                        Positions.getPosition(mocaScript, curMocaTokenStartIdx)),
                                                        EMPTY));
                                        break;
                                case MocaLexer.IF:
                                        // Remove whitespace after.
                                        edits.add(new TextEdit(new Range(
                                                        Positions.getPosition(mocaScript, curMocaTokenStopIdx),
                                                        Positions.getPosition(mocaScript, nextMocaTokenStartIdx)),
                                                        EMPTY));
                                        break;
                                case MocaLexer.ELSE:
                                        // Space before.
                                        edits.add(new TextEdit(new Range(
                                                        Positions.getPosition(mocaScript,
                                                                        curMocaTokenBeginWhitespaceIdx),
                                                        Positions.getPosition(mocaScript, curMocaTokenStartIdx)),
                                                        SPACE));
                                        break;
                                case MocaLexer.DOUBLE_GREATER:
                                case MocaLexer.DOUBLE_PIPE:
                                case MocaLexer.EQUAL:
                                case MocaLexer.NOT_EQUAL:
                                case MocaLexer.LESS:
                                case MocaLexer.GREATER:
                                case MocaLexer.LESS_EQUAL:
                                case MocaLexer.GREATER_EQUAL:
                                case MocaLexer.LIKE:
                                case MocaLexer.OR:
                                case MocaLexer.NOT:
                                case MocaLexer.IS:
                                        // Space before and after.
                                        edits.add(new TextEdit(new Range(
                                                        Positions.getPosition(mocaScript,
                                                                        curMocaTokenBeginWhitespaceIdx),
                                                        Positions.getPosition(mocaScript, curMocaTokenStartIdx)),
                                                        SPACE));

                                        edits.add(new TextEdit(new Range(
                                                        Positions.getPosition(mocaScript, curMocaTokenStopIdx),
                                                        Positions.getPosition(mocaScript, nextMocaTokenStartIdx)),
                                                        SPACE));

                                        break;
                                case MocaLexer.WHERE:
                                        // Newline + tab before and add space after.
                                        edits.add(new TextEdit(new Range(
                                                        Positions.getPosition(mocaScript,
                                                                        curMocaTokenBeginWhitespaceIdx),
                                                        Positions.getPosition(mocaScript, curMocaTokenStartIdx)),
                                                        NEWLINE + TAB + indentBuilder.toString()));

                                        edits.add(new TextEdit(new Range(
                                                        Positions.getPosition(mocaScript, curMocaTokenStopIdx),
                                                        Positions.getPosition(mocaScript, nextMocaTokenStartIdx)),
                                                        SPACE));
                                        break;
                                case MocaLexer.AND:
                                        // Do not want to add newline if within parenthesis.
                                        if (parenStack > 0) {
                                                // Space before and after.
                                                edits.add(new TextEdit(new Range(
                                                                Positions.getPosition(mocaScript,
                                                                                curMocaTokenBeginWhitespaceIdx),
                                                                Positions.getPosition(mocaScript,
                                                                                curMocaTokenStartIdx)),
                                                                SPACE));
                                                edits.add(new TextEdit(
                                                                new Range(Positions.getPosition(mocaScript,
                                                                                curMocaTokenStopIdx),
                                                                                Positions.getPosition(mocaScript,
                                                                                                nextMocaTokenStartIdx)),
                                                                SPACE));
                                        } else {
                                                // Newline + tab + space before and add space after.
                                                edits.add(new TextEdit(
                                                                new Range(Positions.getPosition(mocaScript,
                                                                                curMocaTokenBeginWhitespaceIdx),
                                                                                Positions.getPosition(mocaScript,
                                                                                                curMocaTokenStartIdx)),
                                                                NEWLINE + TAB + indentBuilder.toString() + SPACE
                                                                                + SPACE));

                                                edits.add(new TextEdit(
                                                                new Range(Positions.getPosition(mocaScript,
                                                                                curMocaTokenStopIdx),
                                                                                Positions.getPosition(mocaScript,
                                                                                                nextMocaTokenStartIdx)),
                                                                SPACE));
                                        }

                                        break;
                                case MocaLexer.TRY:
                                        break;
                                case MocaLexer.CATCH:
                                        // Space before and remove whitespace after.
                                        edits.add(new TextEdit(new Range(
                                                        Positions.getPosition(mocaScript,
                                                                        curMocaTokenBeginWhitespaceIdx),
                                                        Positions.getPosition(mocaScript, curMocaTokenStartIdx)),
                                                        SPACE));

                                        edits.add(new TextEdit(new Range(
                                                        Positions.getPosition(mocaScript, curMocaTokenStopIdx),
                                                        Positions.getPosition(mocaScript, nextMocaTokenStartIdx)),
                                                        EMPTY));
                                        break;
                                case MocaLexer.FINALLY:
                                        break;
                                case MocaLexer.AMPERSAND:
                                        // Space before and newline after.
                                        edits.add(new TextEdit(new Range(
                                                        Positions.getPosition(mocaScript,
                                                                        curMocaTokenBeginWhitespaceIdx),
                                                        Positions.getPosition(mocaScript, curMocaTokenStartIdx)),
                                                        SPACE));

                                        edits.add(new TextEdit(new Range(
                                                        Positions.getPosition(mocaScript, curMocaTokenStopIdx),
                                                        Positions.getPosition(mocaScript, nextMocaTokenStartIdx)),
                                                        NEWLINE + indentBuilder.toString()));
                                        break;
                                case MocaLexer.COMMA:
                                        // Remove whitespace before and add space after.
                                        edits.add(new TextEdit(new Range(
                                                        Positions.getPosition(mocaScript,
                                                                        curMocaTokenBeginWhitespaceIdx),
                                                        Positions.getPosition(mocaScript, curMocaTokenStartIdx)),
                                                        EMPTY));

                                        edits.add(new TextEdit(new Range(
                                                        Positions.getPosition(mocaScript, curMocaTokenStopIdx),
                                                        Positions.getPosition(mocaScript, nextMocaTokenStartIdx)),
                                                        SPACE));
                                        break;
                                default:
                                        break;
                        }

                }
        }

}