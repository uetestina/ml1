package com.cloud.vm;

import com.cloud.legacymodel.storage.VirtualMachineTemplate;
import com.cloud.legacymodel.to.DiskTO;
import com.cloud.legacymodel.user.Account;
import com.cloud.legacymodel.vm.BootloaderType;
import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.model.enumeration.VirtualMachineType;
import com.cloud.offering.ServiceOffering;

import java.util.List;
import java.util.Map;

/**
 * VirtualMachineProfile describes one virtual machine. This object
 * on what the virtual machine profile should look like before it is
 * actually started on the hypervisor.
 */
public interface VirtualMachineProfile {

    List<String[]> getVmData();

    void setVmData(List<String[]> vmData);

    String getConfigDriveLabel();

    void setConfigDriveLabel(String configDriveLabel);

    String getConfigDriveIsoRootFolder();

    void setConfigDriveIsoRootFolder(String configDriveIsoRootFolder);

    String getConfigDriveIsoFile();

    void setConfigDriveIsoFile(String isoFile);

    String getHostName();

    String getInstanceName();

    Account getOwner();

    /**
     * @return the virtual machine that backs up this profile.
     */
    VirtualMachine getVirtualMachine();

    /**
     * @return service offering for this virtual machine.
     */
    ServiceOffering getServiceOffering();

    /**
     * @return parameter specific for this type of virtual machine.
     */
    Object getParameter(Param name);

    /**
     * @return the hypervisor type needed for this virtual machine.
     */
    HypervisorType getHypervisorType();

    /**
     * @return template the virtual machine is based on.
     */
    VirtualMachineTemplate getTemplate();

    /**
     * @return the template id
     */
    long getTemplateId();

    /**
     * @return the service offering id
     */
    long getServiceOfferingId();

    /**
     * @return virtual machine id.
     */
    long getId();

    /**
     * @return virtual machine uuid.
     */
    String getUuid();

    List<NicProfile> getNics();

    List<DiskTO> getDisks();

    void addNic(int index, NicProfile nic);

    void addDisk(int index, DiskTO disk);

    StringBuilder getBootArgsBuilder();

    void addBootArgs(String... args);

    String getBootArgs();

    void addNic(NicProfile nic);

    void addDisk(DiskTO disk);

    VirtualMachineType getType();

    void setParameter(Param name, Object value);

    BootloaderType getBootLoaderType();

    void setBootLoaderType(BootloaderType bootLoader);

    Map<Param, Object> getParameters();

    Float getCpuOvercommitRatio();

    Float getMemoryOvercommitRatio();

    class Param {

        public static final Param VmPassword = new Param("VmPassword");
        public static final Param VmSshPubKey = new Param("VmSshPubKey");
        public static final Param ControlNic = new Param("ControlNic");
        public static final Param ReProgramGuestNetworks = new Param("RestartNetwork");
        public static final Param HaTag = new Param("HaTag");
        public static final Param HaOperation = new Param("HaOperation");

        private final String name;

        public Param(final String name) {
            synchronized (Param.class) {
                this.name = name;
            }
        }

        @Override
        public int hashCode() {
            return this.getName() != null ? this.getName().hashCode() : 0;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Param other = (Param) obj;
            return (other.getName().equals(this.getName()));
        }
    }
}
