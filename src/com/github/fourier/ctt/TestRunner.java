package com.github.fourier.ctt;

import com.github.fourier.ctt.configuration.TestFile;
import com.github.fourier.ctt.configuration.TestRun;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by alexeyv on 12/10/14.
 */
public class TestRunner {
    private TestRun mTestRun;

    public TestRunner(TestRun testRun) {
        mTestRun = testRun;
    }

    public TestRun getTestRun() {
        return mTestRun;
    }

    private void removeOld () {
        for (TestFile testFile : mTestRun.getOutput()) {
            File file = new File(testFile.getGenerated());
            file.delete();
        }
    }

    public int exec() throws IOException {
        removeOld();
        ProcessBuilder builder = new ProcessBuilder(mTestRun.getCommandLine().asList());
        builder.directory( new File(mTestRun.getCommandLine().getDirectory()).getAbsoluteFile() );
        builder.redirectErrorStream(true);
        Process process =  builder.start();

        Scanner s = new Scanner(process.getInputStream());
        StringBuilder text = new StringBuilder();
        while (s.hasNextLine()) {
            text.append(s.nextLine());
            text.append("\n");
        }
        s.close();

        //System.out.println(text);
        int result = -1;
        try {
            result = process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }
}
