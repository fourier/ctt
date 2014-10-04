/*
 * Copyright (c) 2014 by Veroveli AB
 *   This file and its contents are Confidential./>
 *       
 */
package com.github.fourier.ctt;



/**
 * Generated from example.xml on 4/10/2014
 */
public class CommandLine {

    /*
     * private fields 
     */
    private String mExecutable;
    private java.util.ArrayList<String> mArguments;

    /*
     * Getters 
     */
    public String getExecutable() {
        return this.mExecutable;
    }

    public java.util.ArrayList<String> getArguments() {
        return this.mArguments;
    }

    /*
     * Setters 
     */
    public CommandLine setExecutable(String executable) {
        this.mExecutable = executable;
        return this;
    }

    public CommandLine setArguments(java.util.ArrayList<String> arguments) {
        this.mArguments = arguments;
        return this;
    }

}
