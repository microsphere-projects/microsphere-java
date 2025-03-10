package io.microsphere.management;


import io.microsphere.logging.Logger;
import io.microsphere.util.BaseUtils;

import java.lang.management.RuntimeMXBean;

import static io.microsphere.constants.SymbolConstants.AT;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.management.JmxUtils.getRuntimeMXBean;
import static io.microsphere.reflect.FieldUtils.getFieldValue;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static io.microsphere.util.StringUtils.substringBefore;
import static java.lang.Integer.parseInt;

/**
 * Management Utility class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see ManagementUtils
 * @since 1.0.0
 */
public abstract class ManagementUtils extends BaseUtils {

    private static final Logger logger = getLogger(ManagementUtils.class);

    static final int UNKNOWN_PROCESS_ID = -1;

    /**
     * "jvm" Field name
     */
    static final String JVM_FIELD_NAME = "jvm";

    /**
     * "getProcessId" Method name
     *
     * @see sun.management.VMManagementImpl#getProcessId()
     */
    final static String GET_PROCESS_ID_METHOD_NAME = "getProcessId";

    /**
     * {@link RuntimeMXBean}
     */
    final static RuntimeMXBean runtimeMXBean = getRuntimeMXBean();

    /**
     * sun.management.ManagementFactory.jvm
     */
    final static Object jvm = findJvm();

    private static Object findJvm() {
        Object jvm = null;
        if (runtimeMXBean != null) {
            try {
                jvm = getFieldValue(runtimeMXBean, JVM_FIELD_NAME);
            } catch (Throwable e) {
                logger.error("The Field[name : '{}'] can't be found in RuntimeMXBean class : '{}'!", JVM_FIELD_NAME, runtimeMXBean.getClass(), e);
            }
        }
        return jvm;
    }

    /**
     * Get the process ID of current JVM
     *
     * @return If can't get the process ID , return <code>-1</code>
     */
    public static int getCurrentProcessId() {
        Integer processId = getNativeCurrentPID();
        if (processId == null) {
            // no guarantee
            processId = resolveCurrentPID();
        }
        return processId;
    }

    static Integer getNativeCurrentPID() {
        Integer processId = null;
        try {
            processId = invokeMethod(jvm, GET_PROCESS_ID_METHOD_NAME);
            if (logger.isTraceEnabled()) {
                logger.trace("The PID was resolved from the native method 'sun.management.VMManagementImpl#getProcessId()' : {}", processId);
            }
        } catch (Throwable e) {
            logger.warn("It's failed to invoke the native method 'sun.management.VMManagementImpl#getProcessId()'", e);
        }
        return processId;
    }

    static int resolveCurrentPID() {
        String name = runtimeMXBean.getName();
        int processId = UNKNOWN_PROCESS_ID;
        try {
            String processIdValue = substringBefore(name, AT);
            processId = parseInt(processIdValue);
            if (logger.isTraceEnabled()) {
                logger.trace("The PID was resolved from the method 'java.lang.management.RuntimeMXBean#getName()' : {}", processId);
            }
        } catch (Throwable e) {
            logger.error("The Process ID can't be based !", e);
        }
        return processId;
    }

}
