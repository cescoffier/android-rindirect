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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Assert;

public class Helper {

     public static void delete(File file) {
            if (file.exists()) {
                if (file.isFile()) {
                    file.delete();
                } else {
                    File[] files = file.listFiles();
                    for (File f : files) {
                        delete(f);
                    }
                    file.delete();
                }
            }
        }

        public static final void copyInputStream(InputStream in, OutputStream out)
                throws IOException {
            byte[] buffer = new byte[1024];
            int len;

            while ((len = in.read(buffer)) >= 0)
                out.write(buffer, 0, len);

            in.close();
            out.close();
        }

        public static final String readInputStream(InputStream is) throws IOException {
            StringBuffer acc = new StringBuffer();
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) {
              for (int i = 0; i < len; i++) {
                acc.append((char) buf[i]);
              }
            }
            is.close();
            return acc.toString();

        }

        public static int getNumberOfOccurence(String txt, String fragment) {
            int lastIndex = 0;
            int count = 0;

            while(lastIndex != -1){
                   lastIndex = txt.indexOf(fragment,lastIndex + 1);
                   if( lastIndex != -1){
                         count ++;
                  }
            }
            return count;
        }

        public static void assertContains(String txt, String fragment) {
            if (txt.lastIndexOf(fragment) == -1) {
                Assert.fail(" The text does not contain " + fragment);
            }
        }

        public static void assertNotContains(String txt, String fragment) {
            if (txt.lastIndexOf(fragment) != -1) {
                Assert.fail(" The text does contain " + fragment);
            }
        }

}
