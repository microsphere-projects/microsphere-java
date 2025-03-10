package io.microsphere.management;


import io.microsphere.logging.Logger;
import io.microsphere.process.ProcessIdResolver;
import io.microsphere.util.BaseUtils;
import io.microsphere.util.ServiceLoaderUtils;

import java.lang.management.RuntimeMXBean;
import java.util.List;

import static io.microsphere.constants.SymbolConstants.AT;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.management.JmxUtils.getRuntimeMXBean;
import static io.microsphere.reflect.FieldUtils.getFieldValue;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.util.ClassLoaderUtils.getClassLoader;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static io.microsphere.util.ServiceLoaderUtils.loadServicesList;
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

    static final long currentProcessId = resolveCurrentProcessId();

    private static long resolveCurrentProcessId() {
        List<ProcessIdResolver> resolvers = loadServicesList(ProcessIdResolver.class);
        Long processId = null;
        for (ProcessIdResolver resolver : resolvers) {
            if ((processId = resolver.current()) != null) {
                break;
            }
        }
        return processId == null ? UNKNOWN_PROCESS_ID : processId;
    }

    /**
     * Get the process ID of current JVM
     *
     * @return If can't get the process ID , return <code>-1</code>
     */
    public static long getCurrentProcessId() {
        return currentProcessId;
    }

}
