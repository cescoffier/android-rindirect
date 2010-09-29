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
import java.util.Properties;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RTest {

    public static final File ROOT = new File("target/test-classes/hello-with-R");

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
        verifier = new Verifier( ROOT.getAbsolutePath() );
        verifier.deleteArtifact( Constants.TEST_GROUP_ID, Constants.TEST_ARTIFACT_ID, Constants.TEST_VERSION, "apk" );

        verifier.setSystemProperties(Constants.getSystemProperties());
        verifier.executeGoal( "clean" );
    }


    @Test
    public void testWithExistingR() throws VerificationException, FileNotFoundException, IOException {
        Verifier verifier  = new Verifier( ROOT.getAbsolutePath() );

        Properties props = Constants.getSystemProperties();
        props.put("rindirect.R", "src/main/android/r/R.java");
        verifier.setSystemProperties(props);
        verifier.setLogFileName("log-wit-existing-R.txt");

        /*
         * The Command Line Options (CLI) are passed to the
         * verifier as a list. This is handy for things like
         * redefining the local repository if needed. In
         * this case, we use the -N flag so that Maven won't
         * recurse. We are only installing the parent pom to
         * the local repo here.
         */
        verifier.executeGoal( "package" );

        verifier.verifyTextInLog("BUILD SUCCESSFUL");

        verifier.verifyTextInLog("rindirect:generate"); // This check that rindirect was executed.

        verifier.resetStreams();

        // Check existency
        File result = new File(ROOT + Constants.GENERATE_FOLDER + "/my/application/R.java"); // Expected name
        Assert.assertTrue(result.exists());

        String clazz = Helper.readInputStream(new FileInputStream(result));

        Helper.assertContains(clazz, "package my.application;");
        Helper.assertContains(clazz, "public final class R {");
        Helper.assertContains(clazz, "public static final class string {");

        Helper.assertNotContains(clazz, "public static final class attr {");
        Assert.assertEquals(5, Helper.getNumberOfOccurence(clazz, "R."));
    }

    @Test
    public void testWithMissingR() throws VerificationException, FileNotFoundException, IOException {
        Verifier verifier  = new Verifier( ROOT.getAbsolutePath() );

        Properties props = Constants.getSystemProperties();
        props.put("rindirect.R", "src/main/android/r/MissingR.java");
        verifier.setSystemProperties(props);

        /*
         * The Command Line Options (CLI) are passed to the
         * verifier as a list. This is handy for things like
         * redefining the local repository if needed. In
         * this case, we use the -N flag so that Maven won't
         * recurse. We are only installing the parent pom to
         * the local repo here.
         */
        try {
            verifier.executeGoal( "package" );
            Assert.fail("Undetected missing R file");
        } catch (VerificationException e) {
            // OK
        }
    }

}
