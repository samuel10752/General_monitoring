package monitoramento;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;

import java.util.List;

public class SystemInfoFetcher {
    public static String getSystemInfoJson() {
        SystemInfo systemInfo = new SystemInfo();
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        GlobalMemory memory = systemInfo.getHardware().getMemory();
        List<HWDiskStore> diskStores = systemInfo.getHardware().getDiskStores();

        StringBuilder storageBuilder = new StringBuilder();
        for (HWDiskStore disk : diskStores) {
            if (storageBuilder.length() > 0) {
                storageBuilder.append(", ");
            }
            storageBuilder.append(String.format(
                    "{ \"name\": \"%s\", \"size\": %d }",
                    disk.getName(),
                    disk.getSize() / (1024 * 1024) // Convertendo para MB
            ));
        }

        return String.format(
                "{\n" +
                        "    \"processor\": {\n" +
                        "        \"name\": \"%s\",\n" +
                        "        \"physicalCores\": %d,\n" +
                        "        \"logicalCores\": %d\n" +
                        "    },\n" +
                        "    \"memory\": {\n" +
                        "        \"total\": %d,\n" +
                        "        \"available\": %d\n" +
                        "    },\n" +
                        "    \"storage\": [%s]\n" +
                        "}",
                processor.getProcessorIdentifier().getName(),
                processor.getPhysicalProcessorCount(),
                processor.getLogicalProcessorCount(),
                memory.getTotal() / (1024 * 1024), // Convertendo para MB
                memory.getAvailable() / (1024 * 1024), // Convertendo para MB
                storageBuilder.toString()
        );
    }
}