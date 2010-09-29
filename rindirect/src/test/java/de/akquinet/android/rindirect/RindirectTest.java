package de.akquinet.android.rindirect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.cli.ParseException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.akquinet.android.rindirect.Main;


public class RindirectTest {

    @BeforeClass
    public static void configure() throws FileNotFoundException, IOException {
        // We copy the template to target/test-classes
        File template = new File("src/main/resources/templates/R.vm");
        File out = new File("target/test-classes/templates");
        out.mkdirs();
        out = new File("target/test-classes/templates/R.vm");
        copyInputStream(new FileInputStream(template),
                new FileOutputStream(out));
    }

    @Before
    public void setUp() {
        cleanup();
    }

    @After
    public void tearDown() {
        System.out.println("Tear down...");
        cleanup();
    }


    @Test
    public void testOnBigRFile() throws ParseException, Exception {
        String p = "de.akquinet.android";
        // Default r
        // Default d
        // i
        String i = "src/test/resources/rindirect/r1/R.java";

        Main.main(new String[] {"-P", p, "-I", i});

        File out = new File("src/de/akquinet/android/R.java");
        Assert.assertTrue(out.exists());
        String clazz = readInputStream(new FileInputStream(out));
        // Header
        System.out.println("Check header");
        assertContains(clazz, "package de.akquinet.android;");
        assertContains(clazz, "public final class R {");

        // Id
        System.out.println("Check id");
        assertContains(clazz, "public static final class id {");
        assertContains(clazz, "public static final int BookmarkAdd = rindirect.r1.R.id.BookmarkAdd;");

        // Style
        System.out.println("Check style");
        assertContains(clazz, "public static final class style {");
        assertContains(clazz, "public static final int SettingsItem = rindirect.r1.R.style.SettingsItem;");
        Assert.assertEquals(2, getNumberOfOccurence(clazz, "R.style."));

        // Color
        System.out.println("Check color");
        assertContains(clazz, "public static final class color {");
        assertContains(clazz, "public static final int background = rindirect.r1.R.color.background;");
        Assert.assertEquals(7, getNumberOfOccurence(clazz, "R.color."));

        // String
        System.out.println("Check string");
        assertContains(clazz, "public static final class string {");
        assertContains(clazz, "public static final int buttonLabelOk = rindirect.r1.R.string.buttonLabelOk;");

        // Menu
        System.out.println("Check menu");
        assertContains(clazz, "public static final class menu {");
        assertContains(clazz, "// menu_main");
        assertContains(clazz, "public static final int menu_main = rindirect.r1.R.menu.menu_main;");
        Assert.assertEquals(1, getNumberOfOccurence(clazz, "R.menu."));

        // Layout
        System.out.println("Check layout");
        assertContains(clazz, "public static final class layout {");

        // Styleable
        System.out.println("Check styleable");
        assertContains(clazz, "public static final class styleable {");
        assertContains(clazz, "public static final int[] VerifyablePreference = rindirect.r1.R.styleable.VerifyablePreference;");

        // Drawable, XML, attr, array
        System.out.println("Check others");
        assertContains(clazz, "public static final class drawable {");
        assertContains(clazz, "public static final class xml {");
        assertContains(clazz, "public static final class attr {");
        assertContains(clazz, "public static final class array {");

    }

    @Test
    public void testOnHelloWorld() throws ParseException, Exception {
        String p = "de.akquinet.android";
        // Default r
        // Default d
        // i
        String i = "src/test/resources/rindirect/r2/R.java";

        Main.main(new String[] {"-I", i, "-P", p});

        File out = new File("src/de/akquinet/android/R.java");
        Assert.assertTrue(out.exists());
        String clazz = readInputStream(new FileInputStream(out));
        // Header
        System.out.println("Check header");
        assertContains(clazz, "package de.akquinet.android;");
        assertContains(clazz, "public final class R {");

        assertNotContains(clazz, "public static final class attr {");
        Assert.assertEquals(5, getNumberOfOccurence(clazz, "R."));
    }

    @Test
    public void testOnHelloWorldInVerboseMode() throws ParseException, Exception {
        String p = "de.akquinet.android";
        // Default r
        // Default d
        // i
        String i = "src/test/resources/rindirect/r2/R.java";

        Main.main(new String[] {"-I", i, "-P", p, "-V"});

        File out = new File("src/de/akquinet/android/R.java");
        Assert.assertTrue(out.exists());
        String clazz = readInputStream(new FileInputStream(out));
        // Header
        System.out.println("Check header");
        assertContains(clazz, "package de.akquinet.android;");
        assertContains(clazz, "public final class R {");

        assertNotContains(clazz, "public static final class attr {");
        Assert.assertEquals(5, getNumberOfOccurence(clazz, "R."));
    }

    @Test
    public void testOnHelloWorldToAnotherDestination() throws ParseException, Exception {
        String p = "de.akquinet.android";
        // Default r
        String d = "target/rindirect/src";
        // i
        String i = "src/test/resources/rindirect/r2/R.java";

        Main.main(new String[] {"-I", i, "-D", d, "-P", p});

        File out = new File("target/rindirect/src/de/akquinet/android/R.java");
        Assert.assertTrue(out.exists());
        String clazz = readInputStream(new FileInputStream(out));
        // Header
        System.out.println("Check header");
        assertContains(clazz, "package de.akquinet.android;");
        assertContains(clazz, "public final class R {");

        assertNotContains(clazz, "public static final class attr {");
        Assert.assertEquals(5, getNumberOfOccurence(clazz, "R."));
    }


    @Test
    public void testBadModel() throws ParseException, Exception {
        String p = "de.akquinet.android";
        // Default r
        // Default D
        // i
        String i = "src/test/resources/rindirect/r3/MyR.java";

        try {
            Main.main(new String[] {"-I", i, "-P", p});
            Assert.fail("The model cannot be computed on this file");
        } catch (Exception e) {
            // OK
            assertContains(e.getMessage(), "The model was not computed correctly");
        }

        File out = new File("src/de/akquinet/android/S.java");
        Assert.assertFalse(out.exists());
    }

    @Test
    public void testCompilationError() throws ParseException, Exception {
        String p = "de.akquinet.android";
        // Default r
        // Default D
        // i
        String i = "src/test/resources/rindirect/r3/MyR2.java";

        try {
            Main.main(new String[] {"-I", i, "-P", p});
            Assert.fail("The model cannot be computed on this file");
        } catch (Exception e) {
            // OK
            assertContains(e.getMessage(), "Class processing failed");
        }

        File out = new File("src/de/akquinet/android/R.java");
        Assert.assertFalse(out.exists());
    }


    @Test
    public void testWitDifferentOutputPackage() throws ParseException, Exception {
        String p = "de.a.test";
        // Default R
        // Default D
        // I
        String i = "src/test/resources/rindirect/r2/R.java";

        Main.main(new String[] {"-I", i, "-P", p});

        File out = new File("src/de/a/test/R.java");

        String clazz = readInputStream(new FileInputStream(out));
        // Header
        System.out.println("Check header");
        assertContains(clazz, "package de.a.test;");
        assertContains(clazz, "public final class R {");

        Assert.assertTrue(out.exists());
    }

    @Test
    public void testWithDifferentOutputFile() throws ParseException, Exception {
        String p = "de.akquinet.android";
        String R = "MyS";
        // Default D
        // I
        String i = "src/test/resources/rindirect/r2/R.java";

        Main.main(new String[] {"-R", R, "-I", i, "-P", p});

        File out = new File("src/de/akquinet/android/MyS.java");
        Assert.assertTrue(out.exists());
        String clazz = readInputStream(new FileInputStream(out));
        // Header
        System.out.println("Check header");
        assertContains(clazz, "package de.akquinet.android;");
        assertContains(clazz, "public final class MyS {");

        assertNotContains(clazz, "public static final class attr {");
        Assert.assertEquals(5, getNumberOfOccurence(clazz, "R."));
    }

    @Test
    public void testWithDifferentOutputFileAndPackage() throws ParseException, Exception {
        String p = "de.a.test";
        String r = "MyS";
        // Default D
        // I
        String i = "src/test/resources/rindirect/r2/R.java";

        Main.main(new String[] {"-R", r, "-P", p, "-I", i});

        File out = new File("src/de/a/test/MyS.java");
        Assert.assertTrue(out.exists());
        String clazz = readInputStream(new FileInputStream(out));
        // Header
        System.out.println("Check header");
        assertContains(clazz, "package de.a.test;");
        assertContains(clazz, "public final class MyS {");

        assertNotContains(clazz, "public static final class attr {");
        Assert.assertEquals(5, getNumberOfOccurence(clazz, "R."));
    }

    @Test
    public void testOnMissingRFile() throws ParseException, Exception {
         String p = "de.akquinet.android";
        // Default S
        // Default D
        // i
        String i = "src/test/resources/rindirect/missing/R.java";
        try {
            Main.main(new String[] {"-I", i, "-P", p});
            Assert.fail("Missing R not detected");
        } catch (ParseException e) {
            // OK
            assertContains(e.getMessage(), "missing/R.java");
        }

        File out = new File("src/de/akquinet/android/R.java");
        Assert.assertFalse(out.exists());
    }

    @Test
    public void testOnNotJavaFile() throws ParseException, Exception {
        String p = "de.akquinet.android";
        // Default S
        // Default D
        // i
        String i = "src/test/resources/rindirect/corrupted/R.java";
        try {
            Main.main(new String[] {"-I", i, "-P", p});
            Assert.fail("Not a Java file !");
        } catch (Exception e) {
            // OK
            assertContains(e.getMessage(), "Class processing failed");
        }

        File out = new File("src/de/akquinet/android/R.java");
        Assert.assertFalse(out.exists());
    }

    @Test
    public void testOnAJavaFileWhichIsNotARFile() throws ParseException, Exception {
        String p = "de.akquinet.android";
        // Default S
        // Default D
        // i
        String i = "src/test/resources/rindirect/corrupted/MyActivity.java";
        try {
            Main.main(new String[] {"-I", i, "-P", p});
            Assert.fail("Not a R file !");
        } catch (Exception e) {
            // OK
            assertContains(e.getMessage(), "Class processing failed");
        }

        File out = new File("src/de/akquinet/android/R.java");
        Assert.assertFalse(out.exists());
    }

    @Test
    public void testWhenTheDestinationCannotBeWrite() throws ParseException, Exception {
        String p = "de.akquinet.android";
        // Default S
        String d = "pom.xml";
        // i
        String i =  "src/test/resources/rindirect/r2/R.java";

        try {
            Main.main(new String[] {"-I", i, "-P", p, "-D", d});
            Assert.fail("Wrong destination not detected");
        } catch (ParseException e) {
            // OK
        }
    }

    @Test(expected=ParseException.class)
    public void testWhenTheOutputPackageCannotBeCreated() throws ParseException, Exception {
        String p = "de.a.test";
        String r = "MyS";
        // Default D
        // I
        String i = "src/test/resources/rindirect/r2/R.java";

        // Create an empty file.
        File dummy = new File("src/de/a");
        dummy.mkdirs();
        dummy = new File("src/de/a/test");
        dummy.createNewFile();
        Main.main(new String[] {"-R", r, "-P", p, "-I", i});
    }

    @Test(expected=ParseException.class)
    public void testWhenTheOutputClassCannotBeCreated() throws ParseException, Exception {
        String p = "de.akquinet.android";
        // Default R
        // Default D
        // I
        String i = "src/test/resources/rindirect/r2/R.java";

        File dummy = new File("src/de/akquinet/android/R.java");
        dummy.mkdirs();

        Main.main(new String[] {"-I", i, "-P", p});
    }

    @Test
    public void testGenLookup() throws ParseException, Exception {
        // Create the gen folder and add a file
        File gen = new File("gen/my/application");
        gen.mkdirs();
        File R = new File(gen, "R.java");
        File in = new File("src/test/resources/rindirect/r2/R.java");
        copyInputStream(new FileInputStream(in), new FileOutputStream(R));

        String p = "de.akquinet.android";
        Main.main(new String[] {"-P", p});

        File out = new File("src/de/akquinet/android/R.java");
        Assert.assertTrue(out.exists());
        String clazz = readInputStream(new FileInputStream(out));
        // Header
        System.out.println("Check header");
        assertContains(clazz, "package de.akquinet.android;");
        assertContains(clazz, "public final class R {");

        assertNotContains(clazz, "public static final class attr {");
        Assert.assertEquals(5, getNumberOfOccurence(clazz, "R."));
    }

    @Test(expected=ParseException.class)
    public void testGenLookupFailedWithExistingGen() throws ParseException, Exception {
        File gen = new File("gen/my/application");
        gen.mkdirs();

        String p = "de.akquinet.android";
        Main.main(new String[] {"-P", p});
    }

    @Test(expected=ParseException.class)
    public void testGenLookupFailed() throws ParseException, Exception {
        String p = "de.akquinet.android";
        Main.main(new String[] {"-P", p});
    }


    @Test(expected=ParseException.class)
    public void testGenLookupFailedWithANonRFile() throws ParseException, Exception {
        // Create the gen folder and add a file
        File gen = new File("gen/my/application");
        gen.mkdirs();
        File R = new File(gen, "R2.java");
        File in = new File("src/test/resources/rindirect/r2/R.java");
        copyInputStream(new FileInputStream(in), new FileOutputStream(R));

        String p = "de.akquinet.android";
        Main.main(new String[] {"-P", p});
    }

    @Test(expected=Exception.class)
    public void testGenLookupFailedWithANonJavaRFile() throws ParseException, Exception {
        // Create the gen folder and add a file
        File gen = new File("gen/my/application");
        gen.mkdirs();
        File R = new File(gen, "R.java");
        File in = new File("src/test/resources/rindirect/corrupted/R.java");
        copyInputStream(new FileInputStream(in), new FileOutputStream(R));

        String p = "de.akquinet.android";
        Main.main(new String[] {"-P", p});
    }

    @Test(expected=Exception.class)
    public void testGenLookupFailedWithANonValidRFile() throws ParseException, Exception {
        // Create the gen folder and add a file
        File gen = new File("gen/my/application");
        gen.mkdirs();
        File R = new File(gen, "R.java");
        File in = new File("src/test/resources/rindirect/corrupted/MyActivity.java");
        copyInputStream(new FileInputStream(in), new FileOutputStream(R));

        String p = "de.akquinet.android";
        Main.main(new String[] {"-P", p});
    }

    @Test
    public void testThatAllClassesAreDeleted() {
        File root = new File("src/test/resources/rindirect");
        assertNoClasses(root);
    }

    private void assertNoClasses(File root) {
        File[] subDirs = root.listFiles();
        for (File s : subDirs) {
            if (s.isDirectory()) {
                assertNoClasses(s);
            } else {
                if (s.getName().endsWith(".class")) {
                    Assert.fail("Find a class file : " + s.getAbsolutePath());
                }
            }
        }
    }



    private void cleanup() {
        // Delete src/DEFAULT_PACKAGE
        delete(new File("src/de"));
        delete(new File("gen"));
        delete(new File("target/rindirect"));
    }

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
