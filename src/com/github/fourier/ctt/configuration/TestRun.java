/*
 * Copyright (c) 2014 by Veroveli AB
 *   This file and its contents are Confidential./>
 *       
 */
package com.github.fourier.ctt.configuration;


import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

    public TestRun(Node node) {
        setName(node.getAttributes().getNamedItem("name").getNodeValue());
        // 2. Iterate through all children to get remaining fields
        NodeList childNodes = node.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); j++) {
            Node cNode = childNodes.item(j);
            if (cNode instanceof Element) {
                if (cNode.getNodeName().equals("command-line")) {
                    setCommandLine(new CommandLine(cNode));
                } else if (cNode.getNodeName().equals("output")) {
                    setOutput(parseOutputNode(cNode));
                }
            }
        }
    }


    private static ArrayList<TestFile> parseOutputNode(Node node) {
        ArrayList<TestFile> output = new ArrayList<TestFile>();
        NodeList outputChildren = node.getChildNodes();
        for (int k = 0; k < outputChildren.getLength(); ++k) {
            Node fileNode = outputChildren.item(k);
            if (fileNode instanceof Element && fileNode.getNodeName().equals("file"))
                output.add(new TestFile(fileNode));
        }
        return output;
    }

    /*
     * Getters 
     */
    public String getName() {
        return this.mName;
    }

    public CommandLine getCommandLine() {
        return this.mCommandLine;
    }

    public List<TestFile> getOutput() {
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