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
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MissingPackageTest {

    public static final File ROOT = new File("target/test-classes/package-mandatory");

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
    public void testMissingPackage() throws VerificationException, FileNotFoundException, IOException {
        Verifier verifier  = new Verifier( ROOT.getAbsolutePath() );
        verifier.setSystemProperties(Constants.getSystemProperties());

        try {
            verifier.executeGoal( "package" );
            Assert.fail("No package, the execution was expected to fail");
        } catch (VerificationException e) {
            // Ok
        }

        verifier.verifyTextInLog("BUILD ERROR");

        verifier.verifyTextInLog("rindirect:generate"); // This check that rindirect was executed.

        verifier.verifyTextInLog("<package>VALUE</package>");

        verifier.resetStreams();

    }

}
