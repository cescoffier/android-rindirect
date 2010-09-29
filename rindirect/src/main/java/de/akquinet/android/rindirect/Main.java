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
package de.akquinet.android.rindirect;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * Main entry point of the rindirect command line.
 * @goal generate
 */
public class Main {

    /**
     * Rindirect logger.
     * All classes are using this one.
     * The logger is configure in this class.
     */
    private static final Logger LOGGER = Logger.getLogger("RIndirect");

    /**
     * Main method.
     * This methods defines the arguments, parse them and launch the R indirection
     * generation.
     * @param args the arguments.
     * @throws ParseException
     * @throws IOException
     */
    public static void main( String[] args ) throws ParseException, Exception    {
        LOGGER.setLevel(Level.WARNING);

        Options options = new Options();

        options
            .addOption("P", "package", true, "destination package (mandatory)")
            .addOption("R", "classname", true, "generated java file [R]")
            .addOption("D", "destination", true, "the root of the destination [src]")
            .addOption("I", "input", true, "R file [searched in the 'gen' folder]")
            .addOption("V", "verbose", false, "Enable verbose mode");

        CommandLineParser parser = new PosixParser();
        CommandLine cmd = parser.parse( options, args);

        RIndirect rindirect = configure(cmd);
        rindirect.generate();
    }

    /**
     * Configures the RIndirect instance.
     * @param cmd the command line
     * @return a configured rindirect instance
     * @throws ParseException if the command line parameter are incorrect
     */
    private static RIndirect configure(CommandLine cmd) throws ParseException {
        boolean verbose = cmd.hasOption('V');
        if (verbose) {
            LOGGER.setLevel(Level.ALL);
        }

        File dest = new File(cmd.getOptionValue('D', "src"));
        if (!dest.exists()) {
            if (! dest.mkdirs()) {
                throw new ParseException("Cannot create destination directory " + dest.getAbsolutePath());
            }
        } else if (! dest.isDirectory()) {
            throw new ParseException(dest.getAbsolutePath() + " is not a directory");
        }
        LOGGER.info("Destination root : " + dest.getAbsolutePath());

        String packageName = cmd.getOptionValue('P');

        if (packageName == null) {
            throw new ParseException("Missing destination package name");
        }

        File packageFile = new File(dest, packageName.replace(".", "/"));
        if (! packageFile.exists()) {
            if (! packageFile.mkdirs()) {
                throw new ParseException("Cannot create packages " + packageFile.getAbsolutePath());
            }
        } else if (! packageFile.isDirectory()) {
                throw new ParseException(packageFile.getAbsolutePath() + " is not a directory");
        }
        LOGGER.info("Package Name : " + packageName);
        LOGGER.info("Package File : " + packageFile.getAbsolutePath());

        String className = cmd.getOptionValue('R', "R");
        File classFile = new File(packageFile, className + ".java");
        LOGGER.info("Java Class Name : " + className);
        LOGGER.info("Java Class File : " + classFile.getAbsolutePath());

        if (classFile.exists()  &&  ! classFile.isFile()) {
            throw new ParseException(classFile.getAbsolutePath() + " is  a directory - cannot be written");
        }

        File rFile = null;
        if (cmd.hasOption('I')) {
             rFile = new File(cmd.getOptionValue('I'));
            if (! rFile.exists()) {
                throw new ParseException("The given R file (" + rFile.getAbsolutePath() + ") does not exist");
            }
        } else {
            LOGGER.fine("Traverse the 'gen' directory to find the R.java file");
            File gen = new File("gen");
            if (! gen.exists()) {
                throw new ParseException("Cannot find the 'gen' folder");
            } else {
                // Traverse gen to find R
                rFile = findR(gen);
                if (rFile == null) {
                    throw new ParseException("Cannot find the R file in the 'gen' folder");
                }
            }
        }
        LOGGER.info("Input R file : " + rFile.getAbsolutePath());

        return  new RIndirect(dest, packageName, packageFile, className, classFile, rFile);
    }


    /**
     * Traverse the 'gen' folder to find the R.java file.
     * @param gen the gen folder
     * @return the R file of <code>null</code> if not found
     */
    private static File findR(File gen) {
        File[] files = gen.listFiles();
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
}
