/*
 * Copyright (c) 2014 by Veroveli AB
 *   This file and its contents are Confidential./>
 *       
 */
package com.github.fourier.ctt;



/**
 * Generated from example.xml on 4/10/2014
 */
public class TestRun {

    /*
     * private fields 
     */
    private String mName;
    private CommandLine mCommandLine;
    private java.util.ArrayList<TestFile> mOutput;

    /*
     * Getters 
     */
    public String getName() {
        return this.mName;
    }

    public CommandLine getCommandLine() {
        return this.mCommandLine;
    }

    public java.util.ArrayList<TestFile> getOutput() {
        return this.mOutput;
    }

    /*
     * Setters 
     */
    public TestRun setName(String name) {
        this.mName = name;
        return this;
    }

    public TestRun setCommandLine(CommandLine commandLine) {
        this.mCommandLine = commandLine;
        return this;
    }

    public TestRun setOutput(java.util.ArrayList<TestFile> output) {
        this.mOutput = output;
        return this;
    }

}
