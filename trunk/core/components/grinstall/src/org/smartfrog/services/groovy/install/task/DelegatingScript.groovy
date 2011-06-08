/*
 * Adapted from:
 * http://grepcode.com/file/repo1.maven.org/maven2/org.spockframework/spock-core/0.4-groovy-1.6/org/spockframework/builder/DelegatingScript.java
 *
 * this is Apache 2.0 Licensed.
 *
 */


package org.smartfrog.services.groovy.install.task

import org.smartfrog.services.groovy.install.Component
import org.codehaus.groovy.runtime.InvokerHelper
import org.smartfrog.sfcore.common.SmartFrogCoreKeys
import org.smartfrog.sfcore.common.SmartFrogExtractedException
import org.smartfrog.sfcore.logging.LogFactory
import org.smartfrog.sfcore.logging.LogSF

public abstract class DelegatingScript extends Script {

    private LogSF sfLog = LogFactory.sfGetProcessLog()

    private Component component
    private Helper helper

    public void setComponent(Component comp) {
        if (comp) {
            component = comp
            try {
                sfLog = LogFactory.getLog(component.sfResolve(SmartFrogCoreKeys.SF_APP_LOG_NAME, "DelegatingScript", true))
            } catch (Exception e) {
                sfLog.error(e.message)
                throw new SmartFrogExtractedException(SmartFrogExtractedException.convert(e))
            }
        }

        // ScriptHelper needs component to bind it within task scripts
        helper = new Helper(comp)
    }

    @Override
    public Object invokeMethod(String name, Object args) {
        try {
            if (component.metaClass.methods*.name.contains(name)) {
                sfLog.debug("Delegating call to $name with $args to Component")
                return InvokerHelper.invokeMethod(component, name, args)
            }
            if (helper.metaClass.methods*.name.contains(name)) {
                sfLog.debug("Delegating call to $name with $args to Helper")
                return InvokerHelper.invokeMethod(helper, name, args)
            }
            //if we get here, nothing was found
            sfLog.error("Method $name not found, trying super class")
            return super.invokeMethod(name, args);

        } catch (MissingMethodException e) {
            sfLog.error("Method $name not found, trying super class: " +e, e)
            return super.invokeMethod(name, args);
        }
    }
}