package com.github.fourier.ctt.configuration;

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
import java.util.List;

/**
 * Created by alexeyv on 04/10/14.
 */
public class Configuration {

    private ArrayList<TestRun> mTestRuns;

    public Configuration(String xmlConfigFile) throws IOException, SAXException, ParserConfigurationException {
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

    public List<TestRun> getTestRuns() {
        return mTestRuns;
    }


    private static ArrayList<TestRun> parseTestRunsNode(Node node) throws ParserConfigurationException, IOException, SAXException {

        // Iterating through the nodes and extracting the data.
        ArrayList<TestRun> testRuns = new ArrayList<TestRun>();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); ++ i) {
            Node cNode = nodeList.item(i);
            if (cNode instanceof Element) {
                // found TestRun element, let's create and set the instance
                // of TestRun class
                if (cNode.getNodeName().equals("test-run")) {
                    testRuns.add(new TestRun(cNode));
                }
            }
        }
        return testRuns;
    }
}
