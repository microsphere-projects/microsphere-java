package io.microsphere.tools.attach;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * Local {@link VirtualMachineTemplate}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see LocalVirtualMachineTemplate
 * @since 1.0.0
 */
public class LocalVirtualMachineTemplate extends VirtualMachineTemplate {

    public LocalVirtualMachineTemplate() {
        super(String.valueOf(getCurrentProcessId()));
    }

    private static int getCurrentProcessId() {
        int processId = -1;
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        String name = runtimeMXBean.getName();
        int index = name.indexOf('@');
        if (index > -1) {
            String processIdValue = name.substring(0, index);
            processId = Integer.parseInt(processIdValue);
        }
        return processId;
    }
}
