package org.esa.nest.util;

import junit.framework.TestCase;

import java.io.File;

/**
 * DatUtils Tester.
 *
 * @author lveci
 */
public class TestDatUtils extends TestCase {

    public TestDatUtils(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testFindHomeFolder()
    {
        File file = DatUtils.findInHomeFolder(File.separator + "config" + File.separator + "settings.xml");

        assertTrue(file != null);
        assertTrue(file.exists());
    }
}