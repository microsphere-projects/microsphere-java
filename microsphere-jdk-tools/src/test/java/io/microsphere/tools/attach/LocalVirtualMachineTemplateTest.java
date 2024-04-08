/**
 * Confucius commmons project
 */
package io.microsphere.tools.attach;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.Connector;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.spi.AttachProvider;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link LocalVirtualMachineTemplate} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see LocalVirtualMachineTemplate
 * @since 1.0.0
 */
public class LocalVirtualMachineTemplateTest {

    @Test
    public void testNew() {
        LocalVirtualMachineTemplate localVirtualMachineTemplate = new LocalVirtualMachineTemplate();
        String processId = localVirtualMachineTemplate.getProcessId();
        assertNotNull(processId);
        assertTrue(Integer.parseInt(processId) > -1);
    }

    @Test
    public void testExecute() throws IOException, AttachNotSupportedException {
        LocalVirtualMachineTemplate localVirtualMachineTemplate = new LocalVirtualMachineTemplate();

        AttachProvider result = localVirtualMachineTemplate.execute((HotSpotVirtualMachineCallback<AttachProvider>) virtualMachine -> {
            AttachProvider attachProvider = virtualMachine.provider();
            return attachProvider;
        });

        assertNotNull(result);
    }

    @Test
    public void test() {
        VirtualMachineManager virtualMachineManager = Bootstrap.virtualMachineManager();
        List<Connector> allConnectors = virtualMachineManager.allConnectors();
        List<VirtualMachine> connectedVirtualMachines = virtualMachineManager.connectedVirtualMachines();
        assertFalse(allConnectors.isEmpty());
        assertTrue(connectedVirtualMachines.isEmpty());
    }

}
