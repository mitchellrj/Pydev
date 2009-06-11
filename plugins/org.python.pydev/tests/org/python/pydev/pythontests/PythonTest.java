package org.python.pydev.pythontests;

import java.io.File;

import org.python.pydev.core.REF;
import org.python.pydev.core.TestDependent;
import org.python.pydev.core.Tuple;
import org.python.pydev.core.docutils.StringUtils;
import org.python.pydev.runners.SimplePythonRunner;

public class PythonTest extends AbstractBasicRunTestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PythonTest.class);
    }

    
    protected Throwable exec(File f) {
        System.out.println(StringUtils.format("Running: %s", f));
        Tuple<String, String> output = new SimplePythonRunner().runAndGetOutput(new String[] {
                TestDependent.PYTHON_EXE, "-u", REF.getFileAbsolutePath(f) }, f.getParentFile(), null, null);
        
        System.out.println(StringUtils.format("stdout:%s\nstderr:%s", output.o1, output.o2));
        
        if(output.o2.toLowerCase().indexOf("failed") != -1 || output.o2.toLowerCase().indexOf("traceback") != -1){
            throw new AssertionError(output.toString());
        }
        return null;
    }

    
    /**
     * Runs the python tests available in this plugin and in the debug plugin.
     */
    public void testPythonTests() throws Exception {
        execAllAndCheckErrors("test", new File[]{
                new File(TestDependent.TEST_PYDEV_PLUGIN_LOC+"pysrc/tests"),
                new File(TestDependent.TEST_PYDEV_DEBUG_PLUGIN_LOC+"pysrc/tests"),
                new File(TestDependent.TEST_PYDEV_DEBUG_PLUGIN_LOC+"pysrc/tests_python"),
            }
        );
    }
}


