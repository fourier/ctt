/*
 * Copyright (c) 2014 by Veroveli AB
 *   This file and its contents are Confidential./>
 *       
 */
package com.github.fourier.ctt.configuration;


import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * Generated from example.xml on 4/10/2014
 */
public class CommandLine {

    /*
     * private fields 
     */
    private String mExecutable;
    private String mDirectory;
    private java.util.ArrayList<String> mArguments;

    public CommandLine(Node node) {
        setExecutable(node.getAttributes().getNamedItem("executable").getNodeValue());
        setDirectory(node.getAttributes().getNamedItem("directory").getNodeValue());

        ArrayList<String> arguments = new ArrayList<String>();
        NodeList cmdLineChildren = node.getChildNodes();
        for (int i = 0; i < cmdLineChildren.getLength(); ++ i) {
            Node cmdLineChild = cmdLineChildren.item(i);
            if (cmdLineChild instanceof Element) {
                if (cmdLineChild.getNodeName().equals("argument")) {
                    arguments.add(cmdLineChild.getLastChild().getTextContent().trim());
                }
            }
        }
        setArguments(arguments);
    }

    /*
     * Getters 
     */
    public String getExecutable() {
        return this.mExecutable;
    }

    public String getDirectory() {
        return this.mDirectory;
    }

    public List<String> getArguments() {
        return this.mArguments;
    }

    /*
     * Setters 
     */
    public CommandLine setExecutable(String executable) {
        this.mExecutable = executable;
        return this;
    }

    public CommandLine setDirectory(String directory) {
        this.mDirectory = directory;
        return this;
    }

    public CommandLine setArguments(java.util.ArrayList<String> arguments) {
        this.mArguments = arguments;
        return this;
    }

    public List<String> asList() {
        ArrayList<String> commandLine = new ArrayList<String>();
        commandLine.add(getExecutable());
        commandLine.addAll(getArguments());
        return commandLine;
    }

}
