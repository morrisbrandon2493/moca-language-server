package com.github.mrglassdanny.mocalanguageserver.moca.lang.sql;

import java.util.List;

import com.github.mrglassdanny.mocalanguageserver.moca.lang.antlr.MocaSqlParser;
import com.github.mrglassdanny.mocalanguageserver.moca.lang.sql.ast.MocaSqlParseTreeListener;
import com.github.mrglassdanny.mocalanguageserver.moca.lang.sql.ast.MocaSqlSyntaxErrorListener;

import org.antlr.v4.runtime.Token;

public class MocaSqlCompilationResult {

    public List<? extends Token> sqlTokens; // From lexer.
    public MocaSqlParser sqlParser;
    public MocaSqlParseTreeListener sqlParseTreeListener;
    public MocaSqlSyntaxErrorListener sqlSyntaxErrorListener;

    public MocaSqlCompilationResult() {

        this.sqlTokens = null;
        this.sqlParser = null;
        this.sqlParseTreeListener = null;
        this.sqlSyntaxErrorListener = null;
    }

    public boolean hasSqlErrors() {
        return this.sqlSyntaxErrorListener != null && this.sqlSyntaxErrorListener.sqlSyntaxErrors.size() > 0;
    }
}