package monitoramento;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AgentMonitor {
    private static AgentMonitor instance;
    private ScheduledExecutorService scheduler;
    private static final SystemInfo systemInfo = new SystemInfo();

    private AgentMonitor() {
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public static AgentMonitor getInstance() {
        if (instance == null) {
            instance = new AgentMonitor();
        }
        return instance;
    }

    private static double getCpuUsage() {
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long[] currTicks = processor.getSystemCpuLoadTicks();

        long totalCpuTime = 0;
        long idleCpuTime = 0;
        for (int i = 0; i < currTicks.length; i++) {
            totalCpuTime += (currTicks[i] - prevTicks[i]);
            if (i == CentralProcessor.TickType.IDLE.getIndex()) {
                idleCpuTime += (currTicks[i] - prevTicks[i]);
            }
        }
        return 100.0 * (totalCpuTime - idleCpuTime) / totalCpuTime;
    }

    public void stopMonitoring() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            System.out.println("Monitoramento encerrado.");
        }
    }

    private static double getRamUsage() {
        GlobalMemory memory = systemInfo.getHardware().getMemory();
        long totalMemory = memory.getTotal();
        long usedMemory = totalMemory - memory.getAvailable();
        return ((double) usedMemory / totalMemory) * 100;
    }

    private static String getStorageInfo() {
        StringBuilder storageDetails = new StringBuilder();
        FileSystem fileSystem = systemInfo.getOperatingSystem().getFileSystem();

        for (OSFileStore fileStore : fileSystem.getFileStores()) {
            long totalSpace = fileStore.getTotalSpace() / (1024 * 1024 * 1024); // GB
            long usableSpace = fileStore.getUsableSpace() / (1024 * 1024 * 1024); // GB
            long usedSpace = totalSpace - usableSpace;

            storageDetails.append(String.format(
                    "Disco: %s, Usado: %dGB, Disponível: %dGB, Total: %dGB\n",
                    fileStore.getName(), usedSpace, usableSpace, totalSpace));
        }
        return storageDetails.toString();
    }

    public void startMonitoring(SystemData systemData) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Coleta de informações em tempo real
                String cpuData = String.format("%.1f%%", getCpuUsage());
                String ramData = String.format("%.1f%%", getRamUsage());
                String storageInfo = getStorageInfo();
                String gpuData = "Indisponível"; // Placeholder para GPU

                // Atualiza os dados no SystemData
                systemData.updateData(cpuData, gpuData, ramData, storageInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 5, TimeUnit.SECONDS); // A cada 5 segundos
    }
}