package com.github.fourier.ctt;

import com.github.fourier.ctt.configuration.Configuration;
import com.github.fourier.ctt.configuration.TestRun;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

public class Application {
    private static void usage() {
        System.out.println("Usage: " + Application.class.getName() + " config_file.xml");
        System.out.println("Where config_file.xml is any ctt configuration file");
    }

    public static void main(String[] args) {
        try {
            if (args.length < 1)
                usage();
            else {
                Configuration cfg = new Configuration(args[0]);
                List<TestRun> testRuns = cfg.getTestRuns();
                for (TestRun tr : testRuns) {
                    System.out.println("Test Run: " + tr.getName());
                    TestRunner runner = new TestRunner(tr);
                    runner.exec();
                }
                System.out.println("Done");
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (java.lang.IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
