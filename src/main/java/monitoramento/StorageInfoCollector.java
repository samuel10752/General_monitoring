package monitoramento;

import oshi.SystemInfo;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;

public class StorageInfoCollector {
    private final FileSystem fileSystem;

    public StorageInfoCollector() {
        SystemInfo systemInfo = new SystemInfo();
        this.fileSystem = systemInfo.getOperatingSystem().getFileSystem();
    }

    public String collectStorageInfo() {
        StringBuilder storageDetails = new StringBuilder();
        int diskCount = 0; // Contador de discos

        for (OSFileStore fileStore : fileSystem.getFileStores()) {
            diskCount++;
            // Conversão para TB (se necessário)
            long totalSpace = fileStore.getTotalSpace() / (1024L * 1024 * 1024); // Total em GB
            long usableSpace = fileStore.getUsableSpace() / (1024L * 1024 * 1024); // Espaço livre em GB
            long usedSpace = totalSpace - usableSpace; // Espaço usado

            String sizeUnit = "GB";
            if (totalSpace >= 1024) {
                totalSpace /= 1024; // Converte para TB
                usableSpace /= 1024;
                usedSpace /= 1024;
                sizeUnit = "TB";
            }

            // Exibe a identificação e os detalhes do disco
            storageDetails.append(String.format("Disco %d: %s, Usado: %d%s, Disponível: %d%s, Total: %d%s\n",
                    diskCount, fileStore.getName(), usedSpace, sizeUnit, usableSpace, sizeUnit, totalSpace, sizeUnit));
        }

        // Caso o sistema não detecte discos
        if (diskCount == 0) {
            storageDetails.append("Nenhum disco detectado.\n");
        }

        return storageDetails.toString();
    }
}