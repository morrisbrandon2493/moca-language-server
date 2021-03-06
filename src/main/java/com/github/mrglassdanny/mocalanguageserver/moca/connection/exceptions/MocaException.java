package com.github.mrglassdanny.mocalanguageserver.moca.connection.exceptions;

public class MocaException extends Exception {

    private static final long serialVersionUID = 1L;
    private int sts;

    public MocaException(String msg, int sts) {
        super(msg);
        this.sts = sts;
    }

    public int getStatus() {
        return this.sts;
    }
}