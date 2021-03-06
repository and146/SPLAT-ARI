/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.tools.ant;

import org.apache.tools.ant.input.DefaultInputHandler;
import org.apache.tools.ant.input.InputHandler;
import org.apache.tools.ant.input.PropertyFileInputHandler;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.apache.tools.ant.types.*;

import java.io.File;
import junit.framework.TestCase;


/**
 * Very limited test class for Project. Waiting to be extended.
 *
 */
public class ProjectTest extends TestCase {

    private Project p;
    private String root;
    private MockBuildListener mbl;

    public ProjectTest(String name) {
        super(name);
    }

    public void setUp() {
        p = new Project();
        p.init();
        root = new File(File.separator).getAbsolutePath().toUpperCase();
        mbl = new MockBuildListener(p);
    }

    public void testDataTypes() throws BuildException {
        assertNull("dummy is not a known data type",
                   p.createDataType("dummy"));
        Object o = p.createDataType("fileset");
        assertNotNull("fileset is a known type", o);
        assertTrue("fileset creates FileSet", o instanceof FileSet);
        assertTrue("PatternSet",
               p.createDataType("patternset") instanceof PatternSet);
        assertTrue("Path", p.createDataType("path") instanceof Path);
    }

    /**
     * This test has been a starting point for moving the code to FileUtils.
     */
    public void testResolveFile() {
        if (Os.isFamily("netware") || Os.isFamily("dos")) {
            assertEqualsIgnoreDriveCase(localize(File.separator),
                p.resolveFile("/", null).getPath());
            assertEqualsIgnoreDriveCase(localize(File.separator),
                p.resolveFile("\\", null).getPath());
            /*
             * throw in drive letters
             */
            String driveSpec = "C:";
            String driveSpecLower = "c:";
            
            assertEqualsIgnoreDriveCase(driveSpecLower + "\\",
                         p.resolveFile(driveSpec + "/", null).getPath());
            assertEqualsIgnoreDriveCase(driveSpecLower + "\\",
                         p.resolveFile(driveSpec + "\\", null).getPath());
            assertEqualsIgnoreDriveCase(driveSpecLower + "\\",
                         p.resolveFile(driveSpecLower + "/", null).getPath());
            assertEqualsIgnoreDriveCase(driveSpecLower + "\\",
                         p.resolveFile(driveSpecLower + "\\", null).getPath());
            /*
             * promised to eliminate consecutive slashes after drive letter.
             */
            assertEqualsIgnoreDriveCase(driveSpec + "\\",
                         p.resolveFile(driveSpec + "/////", null).getPath());
            assertEqualsIgnoreDriveCase(driveSpec + "\\",
                         p.resolveFile(driveSpec + "\\\\\\\\\\\\", null).getPath());
        } else {
            /*
             * Start with simple absolute file names.
             */
            assertEquals(File.separator,
                         p.resolveFile("/", null).getPath());
            assertEquals(File.separator,
                         p.resolveFile("\\", null).getPath());
            /*
             * drive letters are not used, just to be considered as normal
             * part of a name
             */
            String driveSpec = "C:";
            String udir = System.getProperty("user.dir") + File.separatorChar;
            assertEquals(udir + driveSpec,
                         p.resolveFile(driveSpec + "/", null).getPath());
            assertEquals(udir + driveSpec,
                         p.resolveFile(driveSpec + "\\", null).getPath());
            String driveSpecLower = "c:";
            assertEquals(udir + driveSpecLower,
                         p.resolveFile(driveSpecLower + "/", null).getPath());
            assertEquals(udir + driveSpecLower,
                         p.resolveFile(driveSpecLower + "\\", null).getPath());
        }
        /*
         * Now test some relative file name magic.
         */
        assertEquals(localize("/1/2/3/4"),
                     p.resolveFile("4", new File(localize("/1/2/3"))).getPath());
        assertEquals(localize("/1/2/3/4"),
                     p.resolveFile("./4", new File(localize("/1/2/3"))).getPath());
        assertEquals(localize("/1/2/3/4"),
                     p.resolveFile(".\\4", new File(localize("/1/2/3"))).getPath());
        assertEquals(localize("/1/2/3/4"),
                     p.resolveFile("./.\\4", new File(localize("/1/2/3"))).getPath());
        assertEquals(localize("/1/2/3/4"),
                     p.resolveFile("../3/4", new File(localize("/1/2/3"))).getPath());
        assertEquals(localize("/1/2/3/4"),
                     p.resolveFile("..\\3\\4", new File(localize("/1/2/3"))).getPath());
        assertEquals(localize("/1/2/3/4"),
                     p.resolveFile("../../5/.././2/./3/6/../4", new File(localize("/1/2/3"))).getPath());
        assertEquals(localize("/1/2/3/4"),
                     p.resolveFile("..\\../5/..\\./2/./3/6\\../4", new File(localize("/1/2/3"))).getPath());

    }

    /**
     * adapt file separators to local conventions
     */
    private String localize(String path) {
        path = root + path.substring(1);
        return path.replace('\\', File.separatorChar).replace('/', File.separatorChar);
    }

    /**
     * convenience method
     * the drive letter is in lower case under cygwin
     * calling this method allows tests where FileUtils.normalize
     * is called via resolveFile to pass under cygwin
     */
    private void assertEqualsIgnoreDriveCase(String s1, String s2) {
        if ((Os.isFamily("dos") || Os.isFamily("netware"))
            && s1.length() >= 1 && s2.length() >= 1) {
            StringBuffer sb1 = new StringBuffer(s1);
            StringBuffer sb2 = new StringBuffer(s2);
            sb1.setCharAt(0, Character.toUpperCase(s1.charAt(0)));
            sb2.setCharAt(0, Character.toUpperCase(s2.charAt(0)));
            assertEquals(sb1.toString(), sb2.toString());
        } else {
            assertEquals(s1, s2);
        }
    }

    private void assertTaskDefFails(final Class taskClass,
                                       final String message) {
        final String dummyName = "testTaskDefinitionDummy";
        try {
            mbl.addBuildEvent(message, Project.MSG_ERR);
            p.addTaskDefinition(dummyName, taskClass);
            fail("expected BuildException(\""+message+"\", Project.MSG_ERR) when adding task " + taskClass);
        }
        catch(BuildException e) {
            assertEquals(message, e.getMessage());
            mbl.assertEmpty();
            assertTrue(!p.getTaskDefinitions().containsKey(dummyName));
        }
    }

    public void testAddTaskDefinition() {
        p.addBuildListener(mbl);

        p.addTaskDefinition("Ok", DummyTaskOk.class);
        assertEquals(DummyTaskOk.class, p.getTaskDefinitions().get("Ok"));
        p.addTaskDefinition("OkNonTask", DummyTaskOkNonTask.class);
        assertEquals(DummyTaskOkNonTask.class, p.getTaskDefinitions().get("OkNonTask"));
        mbl.assertEmpty();

        assertTaskDefFails(DummyTaskPrivate.class,   DummyTaskPrivate.class   + " is not public");

        assertTaskDefFails(DummyTaskProtected.class,
                           DummyTaskProtected.class + " is not public");

        assertTaskDefFails(DummyTaskPackage.class,   DummyTaskPackage.class   + " is not public");

        assertTaskDefFails(DummyTaskAbstract.class,  DummyTaskAbstract.class  + " is abstract");
        assertTaskDefFails(DummyTaskInterface.class, DummyTaskInterface.class + " is abstract");

        assertTaskDefFails(DummyTaskWithoutDefaultConstructor.class, "No public no-arg constructor in " + DummyTaskWithoutDefaultConstructor.class);
        assertTaskDefFails(DummyTaskWithoutPublicConstructor.class,  "No public no-arg constructor in " + DummyTaskWithoutPublicConstructor.class);

        assertTaskDefFails(DummyTaskWithoutExecute.class,       "No public execute() in " + DummyTaskWithoutExecute.class);
        assertTaskDefFails(DummyTaskWithNonPublicExecute.class, "No public execute() in " + DummyTaskWithNonPublicExecute.class);

        mbl.addBuildEvent("return type of execute() should be void but was \"int\" in " + DummyTaskWithNonVoidExecute.class, Project.MSG_WARN);
        p.addTaskDefinition("NonVoidExecute", DummyTaskWithNonVoidExecute.class);
        mbl.assertEmpty();
        assertEquals(DummyTaskWithNonVoidExecute.class, p.getTaskDefinitions().get("NonVoidExecute"));
    }

    public void testInputHandler() {
        InputHandler ih = p.getInputHandler();
        assertNotNull(ih);
        assertTrue(ih instanceof DefaultInputHandler);
        InputHandler pfih = new PropertyFileInputHandler();
        p.setInputHandler(pfih);
        assertSame(pfih, p.getInputHandler());
    }

    public void testTaskDefinitionContainsKey() {
        assertTrue(p.getTaskDefinitions().containsKey("echo"));
    }

    public void testTaskDefinitionContains() {
        assertTrue(p.getTaskDefinitions().contains(org.apache.tools.ant.taskdefs.Echo.class));
    }

    public void testDuplicateTargets() {
        // fail, because buildfile contains two targets with the same name
        try {
            BFT bft = new BFT("", "core/duplicate-target.xml");
        } catch (BuildException ex) {
            assertEquals("specific message",
                         "Duplicate target 'twice'",
                         ex.getMessage());
            return;
        }
        fail("Should throw BuildException about duplicate target");
    }

    public void testDuplicateTargetsImport() {
        // overriding target from imported buildfile is allowed
        BFT bft = new BFT("", "core/duplicate-target2.xml");
        bft.expectLog("once", "once from buildfile");
    }


    private class DummyTaskPrivate extends Task {
        public DummyTaskPrivate() {}
        public void execute() {}
    }

    protected class DummyTaskProtected extends Task {
        public DummyTaskProtected() {}
        public void execute() {}
    }

    private class BFT extends org.apache.tools.ant.BuildFileTest {
        BFT(String name, String buildfile) {
            super(name);
            this.buildfile = buildfile;
            setUp();
        }

        // avoid multiple configurations
        boolean isConfigured = false;

        // the buildfile to use
        String buildfile = "";

        public void setUp() {
            if (!isConfigured) {
                configureProject("src/etc/testcases/"+buildfile);
                isConfigured = true;
            }
        }

        public void tearDown() { }

        // call a target
        public void doTarget(String target) {
            if (!isConfigured) setUp();
            executeTarget(target);
        }

        public org.apache.tools.ant.Project getProject() {
            return super.getProject();
        }
    }//class-BFT

}

class DummyTaskPackage extends Task {
    public DummyTaskPackage() {}
    public void execute() {}
}
