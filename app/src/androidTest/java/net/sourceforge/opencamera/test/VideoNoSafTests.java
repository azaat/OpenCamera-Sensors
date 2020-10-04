package net.sourceforge.opencamera.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class VideoNoSafTests {
    public static Test suite() {
        TestSuite suite = new TestSuite(MainTests.class.getName());

        suite.addTest(TestSuite.createTest(MainActivityTest.class, "testVideoImuInfo"));

        suite.addTest(TestSuite.createTest(MainActivityTest.class, "testTakeVideo"));
        if( !MainActivityTest.test_camera2 ) {
            suite.addTest(TestSuite.createTest(MainActivityTest.class, "testTakeVideoAudioControl"));
        }
        if( !MainActivityTest.test_camera2 ) {
            suite.addTest(TestSuite.createTest(MainActivityTest.class, "testTakeVideoSubtitles"));
        }
        suite.addTest(TestSuite.createTest(MainActivityTest.class, "testIntentVideo"));
        suite.addTest(TestSuite.createTest(MainActivityTest.class, "testIntentVideoDurationLimit"));
        suite.addTest(TestSuite.createTest(MainActivityTest.class, "testTakeVideoStabilization"));
        suite.addTest(TestSuite.createTest(MainActivityTest.class, "testTakeVideoExposureLock"));
        suite.addTest(TestSuite.createTest(MainActivityTest.class, "testTakeVideoSettings"));
        suite.addTest(TestSuite.createTest(MainActivityTest.class, "testTakeVideoMacro"));
        suite.addTest(TestSuite.createTest(MainActivityTest.class, "testTakeVideoSnapshot"));
        if( !MainActivityTest.test_camera2 ) {
            suite.addTest(TestSuite.createTest(MainActivityTest.class, "testTakeVideoFlashVideo"));
        }
        suite.addTest(TestSuite.createTest(MainActivityTest.class, "testVideoTimerInterrupt"));
        suite.addTest(TestSuite.createTest(MainActivityTest.class, "testVideoPopup"));
        suite.addTest(TestSuite.createTest(MainActivityTest.class, "testVideoTimerPopup"));

        // tests for video log profile (but these don't actually record video)
        if( MainActivityTest.test_camera2 ) {
            suite.addTest(TestSuite.createTest(MainActivityTest.class, "testLogProfile1"));
            suite.addTest(TestSuite.createTest(MainActivityTest.class, "testLogProfile2"));
            suite.addTest(TestSuite.createTest(MainActivityTest.class, "testLogProfile3"));
            suite.addTest(TestSuite.createTest(MainActivityTest.class, "testLogProfile1_extra_strong"));
            suite.addTest(TestSuite.createTest(MainActivityTest.class, "testLogProfile2_extra_strong"));
            suite.addTest(TestSuite.createTest(MainActivityTest.class, "testLogProfile3_extra_strong"));
        }
        return suite;
    }
}
