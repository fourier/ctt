package com.github.fourier.ctt;

import com.github.fourier.ctt.configuration.TestFile;
import com.github.fourier.ctt.configuration.TestRun;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Created by alexeyv on 12/10/14.
 */
public class TestRunner {
    private TestRun mTestRun;
    private String mError;
    private int mErrorCode = -1;
    private String mOutput;
    private String mExecPath;

    private ArrayList<FileReport> mReports = new ArrayList<FileReport>();

    public static class FileReport {
        public static int REPORT_SAME = 0;
        public static int REPORT_GENERATED_ABSENT = 1;
        public static int REPORT_EXPECTED_ABSENT = 2;
        public static int REPORT_BOTH_ABSENT = 3;
        public static int REPORT_NOT_SAME = 4;

        private int mStatus = 0;
        private String mGenerated;
        private String mExpected;

        FileReport(String generated, String expected) {
            mGenerated = generated;
            mExpected = expected;
        }

        public String getGenerated() {
            return mGenerated;
        }

        public String getExpected() {
            return mExpected;
        }

        public int getStatus() {
            return mStatus;
        }

        void setStatus(int status) {
            mStatus = status;
        }
    }

    public TestRunner(TestRun testRun) {
        mTestRun = testRun;
        mExecPath = new File(mTestRun.getCommandLine().getDirectory()).getAbsolutePath();
    }

    private void clearResults() {
        mError = null;
        mErrorCode = -1;
        mOutput = null;
        mReports.clear();
    }

    public TestRun getTestRun() {
        return mTestRun;
    }

    private File getFileForPath(String path) {
        File file = new File(path);
        if (!file.isAbsolute()) {
            file = new File(mExecPath, path);
        }
        return file;
    }

    private void removeGeneratedFiles() {
        for (TestFile testFile : mTestRun.getOutput()) {
            File file = getFileForPath(testFile.getGenerated());
            file.delete();
        }
    }

    private void compareFiles() {
        for (TestFile testFile : mTestRun.getOutput()) {
            File generatedFile = getFileForPath(testFile.getGenerated());
            File expectedFile = getFileForPath(testFile.getExpected());
            FileReport report = new FileReport(testFile.getGenerated(), testFile.getExpected());
            if (!generatedFile.exists()) {
                report.setStatus(FileReport.REPORT_GENERATED_ABSENT);
            }
            if (!expectedFile.exists()) {
                report.setStatus(report.getStatus() == FileReport.REPORT_GENERATED_ABSENT ?
                        FileReport.REPORT_BOTH_ABSENT :
                        FileReport.REPORT_EXPECTED_ABSENT);
            }
            if (report.getStatus() == FileReport.REPORT_SAME) {
                if (!compareContents(generatedFile, expectedFile)) {
                    report.setStatus(FileReport.REPORT_NOT_SAME);
                }
            }
            mReports.add(report);
        }
    }

    private boolean compareContents(File generatedFile, File expectedFile) {
        try {
            if (generatedFile.length() != expectedFile.length())
                return false;
            FileInputStream generated = new FileInputStream(generatedFile);
            FileInputStream expected = new FileInputStream(expectedFile);
            int c;
            while ((c = generated.read()) != -1) {
                if (expected.read() != c)
                    return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public void exec() {
        clearResults();
        try {
            removeGeneratedFiles();
            ProcessBuilder builder = new ProcessBuilder(mTestRun.getCommandLine().asList());
            builder.directory(new File(mTestRun.getCommandLine().getDirectory()).getAbsoluteFile());
            builder.redirectErrorStream(true);
            Process process = builder.start();

            Scanner s = new Scanner(process.getInputStream());
            StringBuilder text = new StringBuilder();
            while (s.hasNextLine()) {
                text.append(s.nextLine());
                text.append("\n");
            }
            s.close();
            mOutput = text.toString();
            mErrorCode = process.waitFor();

            compareFiles();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            String detail = e.getMessage();
            System.err.println(detail);
        }
    }
}
