package com.github.fourier.ctt;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by alexeyv on 04/10/14.
 */
public class Configuration {

    private ArrayList<TestRun> mTestRuns;

    Configuration(String xmlConfigFile) throws IOException, SAXException, ParserConfigurationException {
        // Get the DOM Builder Factory
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Get the DOM Builder
        DocumentBuilder builder = null;

        builder = factory.newDocumentBuilder();
        // Load and Parse the XML document
        // document contains the complete XML as a Tree.
        InputStream fileInputStream = new FileInputStream(xmlConfigFile);
        Document document = builder.parse(fileInputStream);

        mTestRuns = parseTestRunsNode(document.getDocumentElement());
        fileInputStream.close();
    }

    public static TestFile parseTestFileNode(Node node) {
        TestFile testFile = new TestFile();
        testFile.setGenerated(node.getAttributes().getNamedItem("generated").getNodeValue());
        testFile.setExpected(node.getAttributes().getNamedItem("expected").getNodeValue());
        return testFile;
    }

    public static CommandLine parseCommandLineNode(Node node) {
        CommandLine cmdLine = new CommandLine();
        cmdLine.setExecutable(node.getAttributes().getNamedItem("executable").getNodeValue());

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
        cmdLine.setArguments(arguments);
        return cmdLine;
    }

    public static ArrayList<TestFile> parseOutputNode(Node node) {
        ArrayList<TestFile> output = new ArrayList<TestFile>();
        NodeList outputChildren = node.getChildNodes();
        for (int k = 0; k < outputChildren.getLength(); ++ k) {
            Node fileNode = outputChildren.item(k);
            if (fileNode instanceof Element && fileNode.getNodeName().equals("file"))
                output.add(parseTestFileNode(fileNode));
        }
        return output;
    }

    public static TestRun parseTestRunNode(Node node) {
        TestRun testRun = new TestRun();
        // 1. Get the name attribute
        testRun.setName(node.getAttributes().getNamedItem("name").getNodeValue());
        // 2. Iterate through all children to get remaining fields
        NodeList childNodes = node.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); j++) {
            Node cNode = childNodes.item(j);
            if (cNode instanceof Element) {
                if (cNode.getNodeName().equals("command-line")) {
                    testRun.setCommandLine(parseCommandLineNode(cNode));
                } else if (cNode.getNodeName().equals("output")) {
                    testRun.setOutput(parseOutputNode(cNode));
                }
            }
        }
        return testRun;
    }

    public static ArrayList<TestRun> parseTestRunsNode(Node node) throws ParserConfigurationException, IOException, SAXException {

        // Iterating through the nodes and extracting the data.
        ArrayList<TestRun> testRuns = new ArrayList<TestRun>();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); ++ i) {
            Node cNode = nodeList.item(i);
            if (cNode instanceof Element) {
                // found TestRun element, let's create and set the instance
                // of TestRun class
                if (cNode.getNodeName().equals("test-run")) {
                    testRuns.add(parseTestRunNode(cNode));
                }
            }
        }
        return testRuns;
    }
}
