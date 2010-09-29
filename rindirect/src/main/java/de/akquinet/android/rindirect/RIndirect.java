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
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * RIndirect main class.
 */
public class RIndirect {

    /**
     * The logger.
     */
    public static final Logger LOGGER = Logger.getLogger("RIndirect");

    /**
     * The destination root.
     */
    private final File m_destination;

    /**
     * The package name where the file will be generated.
     */
    private final String m_packageName;

    /**
     * The package file where the file will be generated.
     */
    private final File m_packageFile;

    /**
     * The class name.
     */
    private final String m_className;

    /**
     * The class file
     */
    private final File m_classFile;

    /**
     * The R file
     */
    private final File m_R;

    /**
     * The computed R model
     */
    private RModel m_model;

    /**
     * Creates a RIndirect.
     * @param dest
     * @param packageName
     * @param packageFile
     * @param className
     * @param classFile
     * @param rFile
     */
    public RIndirect(File dest, String packageName, File packageFile,
            String className, File classFile, File rFile) {
        m_destination = dest;
        m_packageName = packageName;
        m_packageFile = packageFile;
        m_className = className;
        m_classFile = classFile;
        m_R = rFile;
    }

    /**
     * Generates the R indirection class.
     * It first visit the R file and then retrieve the
     * model. Once done, it launches the Velocity engine
     * to generate the file.
     * @throws IOException if the file cannot be generated
     */
    public void generate() throws Exception {
        LOGGER.info("Lauch R visit");
        RVisitor visitor = new RVisitor();
        try {
            visitor.invokeProcessor(m_R);
        } catch (Exception e) {
            // Compilation error
            LOGGER.severe("Cannot compile the R file : " + e.getMessage());
            throw e;
        }
        LOGGER.info("R visit done");
        m_model = visitor.getStructure();

        if (m_model == null) {
            throw new Exception("The model was not computed correctly," +
                    " the given file is probably not a valid Android R file");
        }

        LOGGER.info("Loading template");
        Template template = loadTemplate();

        if (template == null) {
            throw new Exception("Internal error - Cannot load the template file");
        }

        VelocityContext context = new VelocityContext();
        context.put("original", m_R.getAbsoluteFile());
        context.put("package", m_packageName);
        context.put("R_class", m_model.getRClass());
        context.put("className", m_className);
        context.put("model", m_model.getResources());

        FileWriter fw = new FileWriter(m_classFile);
        LOGGER.info("Start merging ...");

        template.merge( context, fw );
        fw.flush();
        fw.close();

        LOGGER.info("Start merging done");

    }

    /**
     * configures the velocity engine and loads the template.
     * @return the Template.
     */
    private Template loadTemplate() {
        try {
           // resource.loader = class
           //class.resource.loader.description = Velocity Classpath Resource Loader
           //class.resource.loader.class = org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
            Properties props = new Properties();
            props.setProperty("resource.loader", "class");
            props.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader");
            props.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

           Velocity.init(props);


           return Velocity.getTemplate("/templates/R.vm");
        } catch( Exception e ) {
            RIndirect.LOGGER.severe("Cannot find the template : " + e.getMessage());
        }
        return null;
    }

}
