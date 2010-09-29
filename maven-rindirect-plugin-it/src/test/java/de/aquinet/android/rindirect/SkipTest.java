/*
 * Copyright 2010 akquinet
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.aquinet.android.rindirect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SkipTest {

    public static final File ROOT_SKIP = new File("target/test-classes/skip");
    public static final File ROOT_NO_SKIP = new File("target/test-classes/no-skip");

    @Before
    public void setUp() throws VerificationException, IOException {
        Verifier verifier;

        /*
         * We must first make sure that any artifact created
         * by this test has been removed from the local
         * repository. Failing to do this could cause
         * unstable test results. Fortunately, the verifier
         * makes it easy to do this.
         */
        verifier = new Verifier( ROOT_SKIP.getAbsolutePath() );
        verifier.deleteArtifact( Constants.TEST_GROUP_ID, Constants.TEST_ARTIFACT_ID, Constants.TEST_VERSION, "apk" );

        verifier.setSystemProperties(Constants.getSystemProperties());
        verifier.executeGoal( "clean" );

        verifier.resetStreams();

        verifier = new Verifier( ROOT_NO_SKIP.getAbsolutePath() );
        verifier.deleteArtifact( Constants.TEST_GROUP_ID, Constants.TEST_ARTIFACT_ID, Constants.TEST_VERSION, "apk" );

        verifier.setSystemProperties(Constants.getSystemProperties());
        verifier.executeGoal( "clean" );
    }


    @Test
    public void testSkip() throws VerificationException, FileNotFoundException, IOException {
        Verifier verifier  = new Verifier( ROOT_SKIP.getAbsolutePath() );
        verifier.setSystemProperties(Constants.getSystemProperties());
        verifier.setLogFileName("log-skip");

        verifier.executeGoal( "package" );

        verifier.verifyTextInLog("BUILD SUCCESSFUL");

        verifier.verifyTextInLog("rindirect:generate"); // This check that rindirect was executed.

        verifier.resetStreams();

        // Check existency
        File result = new File(ROOT_SKIP + Constants.GENERATE_FOLDER + "/my/application/R.java"); // Expected name
        Assert.assertFalse(result.exists()); // Skipped
    }

    @Test
    public void testNoSkip() throws VerificationException, FileNotFoundException, IOException {
        Verifier verifier  = new Verifier( ROOT_NO_SKIP.getAbsolutePath() );
        verifier.setLogFileName("log-noskip");
        verifier.setSystemProperties(Constants.getSystemProperties());


        verifier.executeGoal( "package" );

        verifier.verifyTextInLog("BUILD SUCCESSFUL");

        verifier.verifyTextInLog("rindirect:generate"); // This check that rindirect was executed.

        verifier.resetStreams();

        // Check existency
        File result = new File(ROOT_NO_SKIP + Constants.GENERATE_FOLDER + "/my/application/R.java"); // Expected name
        Assert.assertTrue(result.exists());

        String clazz = Helper.readInputStream(new FileInputStream(result));

        Helper.assertContains(clazz, "package my.application;");
        Helper.assertContains(clazz, "public final class R {");
        Helper.assertContains(clazz, "public static final class string {");

        Helper.assertNotContains(clazz, "public static final class attr {");
        Assert.assertEquals(5, Helper.getNumberOfOccurence(clazz, "R."));
    }

}
