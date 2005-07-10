/*
 * Author: atotic
 * Created: Aug 26, 2003
 * License: Common Public License v1.0
 */
package org.python.pydev.debug.ui.launching;

import org.python.pydev.debug.core.Constants;


public class CoverageLaunchShortcut extends AbstractLaunchShortcut{

    protected String getLaunchConfigurationType() {
        return Constants.ID_PYTHON_COVERAGE_LAUNCH_CONFIGURATION_TYPE;
    }
    
}
