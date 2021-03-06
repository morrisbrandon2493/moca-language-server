package com.github.mrglassdanny.mocalanguageserver.moca.lang.mocasql;

import java.util.List;

import com.github.mrglassdanny.mocalanguageserver.moca.lang.antlr.MocaSqlParser;
import com.github.mrglassdanny.mocalanguageserver.moca.lang.mocasql.ast.MocaSqlParseTreeListener;
import com.github.mrglassdanny.mocalanguageserver.moca.lang.mocasql.ast.MocaSqlSyntaxErrorListener;

import org.antlr.v4.runtime.Token;
import org.eclipse.lsp4j.Range;

public class MocaSqlCompilationResult {

    public List<? extends Token> mocaSqlTokens;
    public MocaSqlParser mocaSqlParser;
    public MocaSqlParseTreeListener mocaSqlParseTreeListener;
    public MocaSqlSyntaxErrorListener mocaSqlSyntaxErrorListener;
    public Range range;

    public MocaSqlCompilationResult() {

        this.mocaSqlTokens = null;
        this.mocaSqlParser = null;
        this.mocaSqlParseTreeListener = null;
        this.mocaSqlSyntaxErrorListener = null;
        this.range = null;
    }

    public boolean hasSqlErrors() {
        return this.mocaSqlSyntaxErrorListener != null
                && this.mocaSqlSyntaxErrorListener.mocaSqlSyntaxErrors.size() > 0;
    }
}