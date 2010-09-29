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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleElementVisitor6;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * R Visitor
 * This is a visitor inspecting the R file and generating the model.
 */
public class RVisitor  extends SimpleElementVisitor6<Void, Void> {

    /**
     * The computed model.
     */
    private RModel m_model;



    @Override
    public Void visitType(TypeElement e, Void p) {
      // We're hitting the first class, it must be R
      // So we create the model
      if ("R".equals(e.getSimpleName().toString())) {
          RIndirect.LOGGER.info("Processing " + e.getQualifiedName().toString());
          PackageElement pe = (PackageElement) e.getEnclosingElement();
          m_model = new RModel(pe.getQualifiedName().toString());
          for (Element clazz : e.getEnclosedElements()) {
              if (clazz.getKind() == ElementKind.CLASS) {
                  RIndirect.LOGGER.info("Nested class found " + clazz);
                  clazz.accept(this, null);
              }
          }
      }

      if (e.getQualifiedName().toString().contains(".R.")) {
          if (m_model == null) {
              throw new IllegalStateException("Visiting a R inner class before visting R");
          } else {
              RIndirect.LOGGER.info("Processing " + e.getQualifiedName().toString());
          }

          // It's an R inner class
          List<Element> elems = new ArrayList<Element>();
          // Get the resources
          List<? extends Element> list = e.getEnclosedElements();
          for (Element elem : list) {
              if (elem.getKind() == ElementKind.FIELD) {
                  elems.add(elem);
                  // To determine the type, we must visit the type declaration.
                  String type = elem.asType().accept(new RTypeVisitor(), null);
                  m_model.addResource(e.getSimpleName().toString(), elem.getSimpleName().toString(), type);
              }
          }
      }
      return super.visitType(e, p);
    }

    /**
     * Gets the model.
     * @return the model
     */
    public  RModel getStructure() {
        return m_model;
    }

    /**
     * Invoke the processor.
     * @param file the R file
     * @throws Exception if the compilation failed.
     */
    public void invokeProcessor(File file) throws Exception {
        // Gets the Java programming language compiler
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        // Get a new instance of the standard file manager implementation
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(
                null, null, null);
        // Get the valid source files as a list
        if (file != null  && file.exists()) {
            List<File> files = new ArrayList<File>(1);
            files.add(file);
            // Get the list of java file objects
            Iterable<? extends JavaFileObject> compilationUnits1 = fileManager
                    .getJavaFileObjectsFromFiles(files);
            // Create the compilation task
            CompilationTask task = compiler.getTask(null, fileManager, null,
                    null, null, compilationUnits1);
            // Get the list of annotation processors
            LinkedList<AbstractProcessor> processors = new LinkedList<AbstractProcessor>();
            processors.add(new RProcessor());
            task.setProcessors(processors);
            // Perform the compilation task.
            boolean result = task.call();
            try {
                fileManager.close();
            } catch (IOException e) {
                RIndirect.LOGGER.fine(e.getMessage());
            }

            if (! result) {
                throw new Exception("Class processing failed, probably a compilation error");
            } else {
                // Cleanup generated classed.
                File parent = file.getParentFile();
                if (parent != null) {
                    File[] classes = parent.listFiles();
                    String className = file.getName().substring(0, file.getName().lastIndexOf('.'));
                    for (File c : classes) {
                        if ((className + ".class").equals(c.getName())) {
                            // Main R file
                            c.delete();
                        } else if (c.getName().endsWith(".class")
                                && c.getName().startsWith(className + "$")) {
                            // Inner classes
                            c.delete();
                        }
                    }
                }
            }

        } else {
            throw new Exception("No valid source files to process.");
        }
    }

    @SupportedSourceVersion(SourceVersion.RELEASE_6)
    @SupportedAnnotationTypes("*")
    private class RProcessor extends AbstractProcessor {

        @Override
        public boolean process(Set<? extends TypeElement> annotations,
                RoundEnvironment roundEnvironment) {

            // We know we have only one element.
            for (Element e : roundEnvironment.getRootElements()) {
                e.accept(RVisitor.this, null);
            }

            return true;
        }

    }

    /**
     * We know the R format.
     * It can contains only int and int array.
     */
    private class RTypeVisitor implements TypeVisitor<String, Void> {

        public String visit(TypeMirror typemirror) { return null; }

        public String visit(TypeMirror arg0, Void arg1) { return null; }

        public String visitArray(ArrayType arg0, Void arg1) {
            return arg0.getComponentType() + "[]";
        }

        public String visitDeclared(DeclaredType arg0, Void arg1) { return null; }

        public String visitError(ErrorType arg0, Void arg1) { return null; }

        public String visitExecutable(ExecutableType arg0, Void arg1) { return null; }

        public String visitNoType(NoType arg0, Void arg1) { return null; }

        public String visitNull(NullType arg0, Void arg1) { return null; }

        public String visitPrimitive(PrimitiveType arg0, Void arg1) {
            return arg0.toString();
        }

        public String visitTypeVariable(TypeVariable arg0, Void arg1) { return null; }

        public String visitUnknown(TypeMirror arg0, Void arg1) { return null; }

        public String visitWildcard(WildcardType arg0, Void arg1) { return null; }

    }

}
