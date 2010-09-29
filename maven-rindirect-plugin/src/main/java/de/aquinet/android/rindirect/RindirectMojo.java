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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import de.akquinet.android.rindirect.RIndirect;

/**
 * Generates the Rindirect S file.
 *
 * @goal generate
 *
 * @phase generate-sources
 */
public class RindirectMojo  extends AbstractMojo {

    private static final List<String> SUPPORTED_PACKAGING = new ArrayList<String>();

    static {
        SUPPORTED_PACKAGING.add("apk");
    }

    /**
     * The output directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private String m_target;

    /**
     * The destination package
     *
     * @parameter alias="package"
     * @required
     */
    private String m_package;

    /**
     * Enables the verbose mode
     *
     * @parameter alias="verbose" default-value="false"
     */
    private boolean m_verbose;

    /**
     * Skips the generation.
     *
     * @parameter alias="skip" default-value="false"
     */
    private boolean m_skip;

    /**
     * The generated class name
     *
     * @parameter alias="classname" default-value="R"
     */
    private String m_className;

    /**
     * the input R file
     *
     * @parameter alias="R"
     */
    private String m_r;

    /**
     * The Maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject m_project;

    public void execute()
        throws MojoExecutionException {

        // Check project type
        if (! SUPPORTED_PACKAGING.contains(m_project.getPackaging())) {
            info("The maven-rindirect-plugin does not support " + m_project.getPackaging() +", nothing done");
            return;
        }

        if (m_skip) {
            info("Rindirect generation skipped");
            return;
        } else {
            // Check that package is set
            if (m_package == null) {
                throw new MojoExecutionException("'package' is missing in the plugin configuration");
            }
        }

        info("Start Rindirect generation");

        File R = findRClass();
        info("R class found " + R.getAbsolutePath());

        File destination = new File(m_target, "generated-sources" + File.separator + "rindirect");
        destination.mkdirs();

        File packageFile = getPackageFile(destination);
        File classFile = new File(packageFile, m_className + ".java");

        info("Generated file " + classFile.getAbsolutePath());

        // Set the Rindirect Logger level
        if (m_verbose) {
            RIndirect.LOGGER.setLevel(Level.ALL);
        } else {
            RIndirect.LOGGER.setLevel(Level.WARNING);
        }

        RIndirect rindirect = new RIndirect(
                destination,
                m_package,
                packageFile,
                m_className,
                classFile,
                R);

        try {
            rindirect.generate();
        } catch (Exception e) {
            throw new MojoExecutionException("Error during generation", e);
        }

        info("Rindirect generation done");
        m_project.addCompileSourceRoot(destination.getAbsolutePath());
        info(destination.getAbsolutePath() + " added to source folders");
    }

    private File getPackageFile(File dest) throws MojoExecutionException {
         File packageFile = new File(dest, m_package.replace(".", "/"));
         if (! packageFile.exists()) {
             if (! packageFile.mkdirs()) {
                 throw new MojoExecutionException("Cannot create packages " + packageFile.getAbsolutePath());
             }
         } else if (! packageFile.isDirectory()) {
                 throw new MojoExecutionException(packageFile.getAbsolutePath() + " is not a directory");
         }
         return packageFile;
    }

    private File findRClass() throws MojoExecutionException {
        if (m_r != null) {
            File r = new File(m_project.getBasedir(), m_r);
            if (! r.exists()) {
                throw new MojoExecutionException("Cannot find R file - File does not exist" + r.getAbsolutePath());
            } else {
                return r;
            }
        }  else {
            File r = null;
            // First look in the generated-sources/r
            File root = new File(m_target + File.separator + "generated-sources" + File.separator + "r");
            info("Search R class in " + root.getAbsolutePath());
            if (root.exists()) {
                r = findR(root);
                if (r == null) {
                    root = new File(m_project.getBasedir(), "gen");
                    info("Search R class in " + root.getAbsolutePath());
                    if (root.exists()) {
                        r = findR(root);
                    }
                }
            }

            if (r == null) {
                throw new MojoExecutionException("Cannot find the R class");
            } else {
                return r;
            }
        }

    }

    /**
     * Traverse the given folder to find the R.java file.
     * @param root the gen folder
     * @return the R file of <code>null</code> if not found
     */
    private static File findR(File root) {
        File[] files = root.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                return findR(f);
            } else {
                if ("R.java".equals(f.getName())) {
                    return f;
                }
            }
        }
        return null;
    }

    private void info(String message) {
        if (m_verbose) {
            getLog().info(message);
        }
    }
}
