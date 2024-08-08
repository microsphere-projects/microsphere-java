package io.microsphere.management;


import io.microsphere.logging.Logger;

import java.lang.management.RuntimeMXBean;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.FieldUtils.getFieldValue;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.util.StringUtils.substringBefore;
import static java.lang.Integer.parseInt;
import static java.lang.management.ManagementFactory.getRuntimeMXBean;

/**
 * Management Utility class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see ManagementUtils
 * @since 1.0.0
 */
public abstract class ManagementUtils {

    private static final Logger logger = getLogger(ManagementUtils.class);

    /**
     * {@link RuntimeMXBean}
     */
    private final static RuntimeMXBean runtimeMXBean = getRuntimeMXBean();

    /**
     * "jvm" Field name
     */
    private final static String JVM_FIELD_NAME = "jvm";
    /**
     * sun.management.ManagementFactory.jvm
     */
    final static Object jvm = initJvm();
    /**
     * "getProcessId" Method name
     *
     * @see sun.management.VMManagementImpl#getProcessId()
     */
    private final static String GET_PROCESS_ID_METHOD_NAME = "getProcessId";

    private static Object initJvm() {
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
        int processId = -1;
        Object result = null;

        try {
            result = invokeMethod(jvm, GET_PROCESS_ID_METHOD_NAME);
        } catch (Throwable e) {
            logger.error("The method 'sun.management.VMManagementImpl#getProcessId()' can't be invoked!", e);
        }

        if (result instanceof Integer) {
            processId = (Integer) result;
        } else {
            // no guarantee
            String name = runtimeMXBean.getName();
            String processIdValue = substringBefore(name, "@");
            processId = parseInt(processIdValue);
        }
        return processId;
    }

}
