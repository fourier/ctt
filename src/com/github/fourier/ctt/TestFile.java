/*
 * Copyright (c) 2014 by Veroveli AB
 *   This file and its contents are Confidential./>
 *       
 */
package com.github.fourier.ctt;


import org.w3c.dom.Node;

/**
 * Generated from example.xml on 4/10/2014
 */
public class TestFile {

    /*
     * private fields 
     */
    private String mGenerated;
    private String mExpected;

    public TestFile(Node node) {
        setGenerated(node.getAttributes().getNamedItem("generated").getNodeValue());
        setExpected(node.getAttributes().getNamedItem("expected").getNodeValue());
    }

    /*
     * Getters 
     */
    public String getGenerated() {
        return this.mGenerated;
    }

    public String getExpected() {
        return this.mExpected;
    }

    /*
     * Setters 
     */
    public TestFile setGenerated(String generated) {
        this.mGenerated = generated;
        return this;
    }

    public TestFile setExpected(String expected) {
        this.mExpected = expected;
        return this;
    }

}
